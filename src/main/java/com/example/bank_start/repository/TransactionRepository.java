package com.example.bank_start.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с данными о транзакциях пользователей.
 *
 * <p>Использует JdbcTemplate для выполнения SQL-запросов к основной базе данных.
 * Содержит три кеша Caffeine для оптимизации часто повторяющихся запросов:
 * <ul>
 *   <li>{@code productTypeCache} — кеш результатов проверки наличия продуктов типа.</li>
 *   <li>{@code transactionSumCache} — кеш результатов вычисления сумм транзакций.</li>
 *   <li>{@code transactionCountCache} — кеш результатов подсчёта количества транзакций.</li>
 * </ul>
 *
 * <h3>Ключи кешей:</h3>
 * <p>Каждый ключ составной и зависит от аргументов запроса:
 * <ul>
 *   <li>Для productTypeCache: {@code userId:productType}.</li>
 *   <li>Для transactionSumCache: {@code userId:productType:transactionType}.</li>
 *   <li>Для transactionCountCache: {@code userId:productType}.</li>
 * </ul>
 *
 * <h3>Таймауты:</h3>
 * <p>Все кеша имеют TTL 10 минут и максимальный размер 1000 записей.
 */
@Repository
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Кеш для результатов проверки наличия продуктов типа у пользователя.
     * Ключ: {@code userId:productType}, значение: {@code Boolean}.
     */
    private final Cache<String, Boolean> productTypeCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    /**
     * Кеш для результатов вычисления сумм транзакций.
     * Ключ: {@code userId:productType:transactionType}, значение: {@code BigDecimal}.
     */
    private final Cache<String, BigDecimal> transactionSumCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    /**
     * Кеш для результатов подсчёта количества транзакций.
     * Ключ: {@code userId:productType}, значение: {@code Integer}.
     */
    private final Cache<String, Integer> transactionCountCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    /**
     * Создаёт репозиторий с указанным JdbcTemplate.
     *
     * @param jdbcTemplate JdbcTemplate для выполнения SQL-запросов
     */
    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Проверяет, есть ли у пользователя хотя бы одна транзакция по продуктам указанного типа.
     *
     * <p>Результат кешируется с ключом {@code userId:productType}.
     *
     * @param userId       идентификатор пользователя
     * @param productType  тип продукта (DEBIT, CREDIT, INVEST, SAVING)
     * @return true, если у пользователя есть транзакции по продуктам данного типа
     */
    public boolean hasProductType(UUID userId, String productType) {
        String key = userId + ":" + productType;
        return productTypeCache.get(key, k -> {
            String sql = """
                SELECT COUNT(*) FROM TRANSACTIONS t
                JOIN PRODUCTS p ON t.PRODUCT_ID = p.ID
                WHERE t.USER_ID = ? AND p.TYPE = ?
                """;
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, productType);
            return count != null && count > 0;
        });
    }

    /**
     * Проверяет, нет ли у пользователя транзакций по продуктам указанного типа.
     *
     * @param userId       идентификатор пользователя
     * @param productType  тип продукта (DEBIT, CREDIT, INVEST, SAVING)
     * @return true, если у пользователя нет транзакций по продуктам данного типа
     */
    public boolean hasNoProductType(UUID userId, String productType) {
        return !hasProductType(userId, productType);
    }

    /**
     * Вычисляет сумму всех транзакций указанного типа по продуктам указанного типа.
     *
     * <p>Результат кешируется с ключом {@code userId:productType:transactionType}.
     *
     * @param userId        идентификатор пользователя
     * @param productType   тип продукта (DEBIT, CREDIT, INVEST, SAVING)
     * @param transactionType тип транзакции (DEPOSIT, WITHDRAW)
     * @return сумма транзакций (0, если транзакций нет)
     */
    public BigDecimal getSumByProductTypeAndTransactionType(UUID userId, String productType, String transactionType) {
        String key = userId + ":" + productType + ":" + transactionType;
        return transactionSumCache.get(key, k -> {
            String sql = """
                SELECT COALESCE(SUM(t.AMOUNT), 0) FROM TRANSACTIONS t
                JOIN PRODUCTS p ON t.PRODUCT_ID = p.ID
                WHERE t.USER_ID = ? AND p.TYPE = ? AND t.TYPE = ?
                """;
            return jdbcTemplate.queryForObject(sql, BigDecimal.class, userId, productType, transactionType);
        });
    }

    /**
     * Подсчитывает количество транзакций пользователя по продуктам указанного типа.
     *
     * <p>Результат кешируется с ключом {@code userId:productType}.
     * Используется для запроса {@code ACTIVE_USER_OF} (требуется >= 5 транзакций).
     *
     * @param userId       идентификатор пользователя
     * @param productType  тип продукта (DEBIT, CREDIT, INVEST, SAVING)
     * @return количество транзакций
     */
    public int countTransactionsByProductType(UUID userId, String productType) {
        String key = userId + ":" + productType;
        return transactionCountCache.get(key, k -> {
            String sql = """
                SELECT COUNT(*) FROM TRANSACTIONS t
                JOIN PRODUCTS p ON t.PRODUCT_ID = p.ID
                WHERE t.USER_ID = ? AND p.TYPE = ?
                """;
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, productType);
            return count != null ? count : 0;
        });
    }

    /**
     * Очищает все кеші при изменении данных о транзакциях.
     */
    public void clearAllCaches() {
        productTypeCache.invalidateAll();
        transactionSumCache.invalidateAll();
        transactionCountCache.invalidateAll();
    }

    /**
     * Получает список уникальных типов продуктов, по которым у пользователя есть транзакции.
     *
     * @param userId идентификатор пользователя
     * @return список типов продуктов
     */
    public List<String> getProductTypes(UUID userId) {
        String sql = """
            SELECT DISTINCT p.TYPE FROM TRANSACTIONS t
            JOIN PRODUCTS p ON t.PRODUCT_ID = p.ID
            WHERE t.USER_ID = ?
            """;
        return jdbcTemplate.queryForList(sql, String.class, userId);
    }

    /**
     * Ищет пользователя в базе данных по имени и фамилии.
     *
     * <p>Выполняет SQL-запрос к таблице USERS, ища пользователя,
     * у которого FIRST_NAME и LAST_NAME точно совпадают с указанными.
     *
     * @param firstName имя пользователя
     * @param lastName фамилия пользователя
     * @return UUID пользователя, если найден ровно один; null иначе
     */
    public UUID findUserByName(String firstName, String lastName) {
        String sql = """
            SELECT ID FROM USERS
            WHERE FIRST_NAME = ? AND LAST_NAME = ?
            LIMIT 1
            """;
        List<UUID> results = jdbcTemplate.queryForList(sql, UUID.class, firstName, lastName);
        return results.size() == 1 ? results.get(0) : null;
    }

    /**
     * Получает полное имя пользователя по его идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return строка "FirstName LastName", если пользователь найден; null иначе
     */
    public String getUserNameById(UUID userId) {
        String sql = """
            SELECT FIRST_NAME || ' ' || LAST_NAME FROM USERS
            WHERE ID = ?
            """;
        List<String> results = jdbcTemplate.queryForList(sql, String.class, userId);
        return results.isEmpty() ? null : results.get(0);
    }
}
