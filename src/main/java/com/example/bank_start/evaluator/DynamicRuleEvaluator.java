package com.example.bank_start.evaluator;

import com.example.bank_start.entity.DynamicRule;
import com.example.bank_start.entity.RuleQuery;
import com.example.bank_start.repository.RuleFiringStatsRepository;
import com.example.bank_start.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Оценщик динамических правил рекомендаций.
 *
 * <p>Выполняет проверку условий динамических правил для заданного пользователя.
 * Поддерживает четыре типа запросов:
 * <ul>
 *   <li>{@code USER_OF} — проверка наличия продуктов типа у пользователя.</li>
 *   <li>{@code ACTIVE_USER_OF} — проверка активности пользователя (>= 5 транзакций).</li>
 *   <li>{@code TRANSACTION_SUM_COMPARE} — сравнение суммы транзакций с константой.</li>
 *   <li>{@code TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW} — сравнение сумм депозитов и выводов.</li>
 * </ul>
 *
 * <h3>Логика выполнения:</h3>
 * <p>Правило считается выполненным, если все его запросы возвращают true
 * (с учётом модификатора {@code negate}). Логическая формула:
 * {@code RuleResult = Q1 && Q2 && ... && Qn}.
 *
 * <h3>Модификатор negate:</h3>
 * <p>Если {@code negate = true}, результат запроса инвертируется.
 * Например, запрос {@code USER_OF} с {@code negate = true} означает,
 * что у пользователя НЕ должно быть продуктов данного типа.
 *
 * <h3>Статистика срабатываний:</h3>
 * <p>При успешном выполнении правила автоматически увеличивается
 * счётчик срабатываний в {@link RuleFiringStatsRepository}.
 */
@Component
public class DynamicRuleEvaluator {

    private static final int ACTIVE_USER_THRESHOLD = 5;

    private final TransactionRepository transactionRepository;
    private final RuleFiringStatsRepository statsRepository;

    /**
     * Создаёт оценщик с указанными репозиториями.
     *
     * @param transactionRepository репозиторий для доступа к данным о транзакциях
     * @param statsRepository репозиторий для статистики срабатываний правил
     */
    public DynamicRuleEvaluator(TransactionRepository transactionRepository,
                                 RuleFiringStatsRepository statsRepository) {
        this.transactionRepository = transactionRepository;
        this.statsRepository = statsRepository;
    }

    /**
     * Проверяет, выполняется ли динамическое правило для заданного пользователя.
     *
     * <p>Правило считается выполненным, если все его запросы возвращают true
     * (с учётом модификатора {@code negate}).
     * При успешном выполнении увеличивается счётчик срабатываний правила.
     *
     * @param dynamicRule динамическое правило для проверки
     * @param userId      идентификатор пользователя
     * @return true, если правило выполняется для пользователя
     */
    public boolean evaluateRule(DynamicRule dynamicRule, UUID userId) {
        List<RuleQuery> queries = dynamicRule.getQueries();
        if (queries == null || queries.isEmpty()) {
            return false;
        }

        for (RuleQuery query : queries) {
            boolean queryResult = evaluateQuery(query, userId);
            if (query.getNegate() != null && query.getNegate()) {
                queryResult = !queryResult;
            }
            if (!queryResult) {
                return false;
            }
        }

        // Increment firing statistics on successful rule evaluation
        incrementFiringStats(dynamicRule.getId());
        return true;
    }

    /**
     * Увеличивает счётчик срабатываний правила.
     *
     * <p>Если статистика для правила ещё не создана, создаёт новую запись
     * со счётчиком 1.
     *
     * @param ruleId идентификатор правила
     */
    private void incrementFiringStats(UUID ruleId) {
        statsRepository.findByRuleId(ruleId).ifPresentOrElse(
                stats -> statsRepository.incrementFireCount(ruleId),
                () -> {
                    var stats = new com.example.bank_start.entity.RuleFiringStats();
                    stats.setRuleId(ruleId);
                    stats.setFireCount(1L);
                    statsRepository.save(stats);
                }
        );
    }

    /**
     * Выполняет отдельный запрос правила.
     *
     * <p>В зависимости от типа запроса ({@code queryType}) выполняет
     * соответствующую проверку данных о пользователе.
     *
     * @param query запрос правила
     * @param userId идентификатор пользователя
     * @return результат выполнения запроса
     * @throws IllegalArgumentException если тип запроса неизвестен или аргументы некорректны
     */
    private boolean evaluateQuery(RuleQuery query, UUID userId) {
        String queryType = query.getQueryType();
        List<String> arguments = query.getArgumentsList();

        return switch (queryType) {
            case "USER_OF" -> evaluateUserOf(userId, arguments);
            case "ACTIVE_USER_OF" -> evaluateActiveUserOf(userId, arguments);
            case "TRANSACTION_SUM_COMPARE" -> evaluateTransactionSumCompare(userId, arguments);
            case "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW" -> evaluateDepositWithdrawCompare(userId, arguments);
            default -> throw new IllegalArgumentException("Unknown query type: " + queryType);
        };
    }

    /**
     * Запрос USER_OF: проверяет, есть ли у пользователя хотя бы одна транзакция
     * по продуктам указанного типа.
     *
     * <h3>Аргументы:</h3>
     * <ul>
     *   <li>arguments[0] — тип продукта (DEBIT, CREDIT, INVEST, SAVING).</li>
     * </ul>
     *
     * @param userId   идентификатор пользователя
     * @param arguments список аргументов запроса
     * @return true, если у пользователя есть транзакции по продуктам данного типа
     */
    private boolean evaluateUserOf(UUID userId, List<String> arguments) {
        if (arguments == null || arguments.size() < 1) {
            throw new IllegalArgumentException("USER_OF requires 1 argument: product type");
        }
        String productType = arguments.get(0);
        return transactionRepository.hasProductType(userId, productType);
    }

    /**
     * Запрос ACTIVE_USER_OF: проверяет, является ли пользователь активным
     * пользователем продукта (имеет не менее 5 транзакций по продуктам данного типа).
     *
     * <h3>Аргументы:</h3>
     * <ul>
     *   <li>arguments[0] — тип продукта (DEBIT, CREDIT, INVEST, SAVING).</li>
     * </ul>
     *
     * @param userId   идентификатор пользователя
     * @param arguments список аргументов запроса
     * @return true, если у пользователя >= 5 транзакций по продуктам данного типа
     */
    private boolean evaluateActiveUserOf(UUID userId, List<String> arguments) {
        if (arguments == null || arguments.size() < 1) {
            throw new IllegalArgumentException("ACTIVE_USER_OF requires 1 argument: product type");
        }
        String productType = arguments.get(0);
        int count = transactionRepository.countTransactionsByProductType(userId, productType);
        return count >= ACTIVE_USER_THRESHOLD;
    }

    /**
     * Запрос TRANSACTION_SUM_COMPARE: сравнивает сумму транзакций указанного типа
     * по продуктам указанного типа с заданной константой.
     *
     * <h3>Аргументы:</h3>
     * <ul>
     *   <li>arguments[0] — тип продукта (DEBIT, CREDIT, INVEST, SAVING).</li>
     *   <li>arguments[1] — тип транзакции (DEPOSIT, WITHDRAW).</li>
     *   <li>arguments[2] — операция сравнения (&gt;, &lt;, =, &gt;=, &lt;=).</li>
     *   <li>arguments[3] — константа C (неотрицательное целое число, int).</li>
     * </ul>
     *
     * <h3>Пример:</h3>
     * <p>{@code ["DEBIT", "DEPOSIT", ">", "100000"]} означает:
     * сумма депозитов по дебетовым счетам должна быть строго больше 100000.
     *
     * @param userId   идентификатор пользователя
     * @param arguments список аргументов запроса
     * @return true, если сумма транзакций удовлетворяет условию сравнения
     */
    private boolean evaluateTransactionSumCompare(UUID userId, List<String> arguments) {
        if (arguments == null || arguments.size() < 4) {
            throw new IllegalArgumentException("TRANSACTION_SUM_COMPARE requires 4 arguments: productType, transactionType, operator, constant");
        }
        String productType = arguments.get(0);
        String transactionType = arguments.get(1);
        String operator = arguments.get(2);
        int constant = Integer.parseInt(arguments.get(3));

        BigDecimal sum = transactionRepository.getSumByProductTypeAndTransactionType(
                userId, productType, transactionType);

        return compare(sum.intValue(), constant, operator);
    }

    /**
     * Запрос TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW: сравнивает сумму всех
     * депозитов с суммой всех выводов по продуктам указанного типа.
     *
     * <h3>Аргументы:</h3>
     * <ul>
     *   <li>arguments[0] — тип продукта (DEBIT, CREDIT, INVEST, SAVING).</li>
     *   <li>arguments[1] — операция сравнения (&gt;, &lt;, =, &gt;=, &lt;=).</li>
     * </ul>
     *
     * <h3>Пример:</h3>
     * <p>{@code ["DEBIT", ">"]} означает:
     * сумма депозитов по дебетовым счетам должна быть строго больше суммы выводов.
     *
     * @param userId   идентификатор пользователя
     * @param arguments список аргументов запроса
     * @return true, если сумма депозитов удовлетворяет условию сравнения с суммой выводов
     */
    private boolean evaluateDepositWithdrawCompare(UUID userId, List<String> arguments) {
        if (arguments == null || arguments.size() < 2) {
            throw new IllegalArgumentException("TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW requires 2 arguments: productType, operator");
        }
        String productType = arguments.get(0);
        String operator = arguments.get(1);

        BigDecimal depositSum = transactionRepository.getSumByProductTypeAndTransactionType(
                userId, productType, "DEPOSIT");
        BigDecimal withdrawSum = transactionRepository.getSumByProductTypeAndTransactionType(
                userId, productType, "WITHDRAW");

        return compare(depositSum.intValue(), withdrawSum.intValue(), operator);
    }

    /**
     * Выполняет сравнение двух чисел с заданной операцией.
     *
     * @param actual   фактическое значение (сумма транзакций)
     * @param constant константа для сравнения
     * @param operator оператор сравнения (&gt;, &lt;, =, &gt;=, &lt;=)
     * @return true, если условие сравнения выполняется
     * @throws IllegalArgumentException если оператор неизвестен
     */
    private boolean compare(int actual, int constant, String operator) {
        return switch (operator) {
            case ">" -> actual > constant;
            case "<" -> actual < constant;
            case "=" -> actual == constant;
            case ">=" -> actual >= constant;
            case "<=" -> actual <= constant;
            default -> throw new IllegalArgumentException("Unknown comparison operator: " + operator);
        };
    }
}
