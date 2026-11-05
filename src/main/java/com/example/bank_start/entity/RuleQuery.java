package com.example.bank_start.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сущность JPA для хранения отдельного запроса внутри динамического правила.
 *
 * <p>Каждый запрос описывает одно условие, которое должно быть выполнено
 * для срабатывания правила. Запрос имеет тип, набор аргументов и модификатор
 * отрицания.
 *
 * <h3>Поддерживаемые типы запросов:</h3>
 * <ul>
 *   <li>{@code USER_OF} — проверяет наличие хотя бы одной транзакции
 *       по продуктам указанного типа.</li>
 *   <li>{@code ACTIVE_USER_OF} — проверяет наличие не менее пяти транзакций
 *       по продуктам указанного типа.</li>
 *   <li>{@code TRANSACTION_SUM_COMPARE} — сравнивает сумму транзакций
 *       с заданной константой.</li>
 *   <li>{@code TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW} — сравнивает
 *       сумму пополнений с суммой трат по продуктам указанного типа.</li>
 * </ul>
 *
 * <h3>Связи:</h3>
 * <ul>
 *   <li>ManyToOne с {@link DynamicRule} — запрос принадлежит одному правилу.</li>
 * </ul>
 */
@Entity
@Table(name = "RULE_QUERIES", schema = "PUBLIC")
public class RuleQuery {

    /**
     * Уникальный идентификатор запроса (UUID).
     * Генерируется автоматически при сохранении.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Тип запроса. Определяет, какую операцию необходимо выполнить.
     * Возможные значения: {@code USER_OF}, {@code ACTIVE_USER_OF},
     * {@code TRANSACTION_SUM_COMPARE}, {@code TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW}.
     */
    @Column(name = "query_type", nullable = false)
    private String queryType;

    /**
     * Аргументы запроса. Хранятся как строка, разделённая запятыми.
     * Содержание и количество аргументов зависят от типа запроса.
     */
    @Column(name = "arguments")
    private String arguments;

    /**
     * Модификатор отрицания. При значении {@code true} результат выполнения
     * запроса инвертируется: true становится false, и наоборот.
     */
    @Column(name = "negate", nullable = false)
    private Boolean negate = false;

    /**
     * Ссылка на родительское динамическое правило.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private DynamicRule dynamicRule;

    public RuleQuery() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    /**
     * Возвращает аргументы запроса в виде списка строк.
     *
     * @return список аргументов
     */
    public List<String> getArgumentsList() {
        if (arguments == null || arguments.isBlank()) {
            return new ArrayList<>();
        }
        return List.of(arguments.split(","));
    }

    public void setArguments(List<String> arguments) {
        if (arguments == null || arguments.isEmpty()) {
            this.arguments = null;
        } else {
            this.arguments = String.join(",", arguments);
        }
    }

    public Boolean getNegate() {
        return negate;
    }

    public void setNegate(Boolean negate) {
        this.negate = negate;
    }

    public DynamicRule getDynamicRule() {
        return dynamicRule;
    }

    public void setDynamicRule(DynamicRule dynamicRule) {
        this.dynamicRule = dynamicRule;
    }
}
