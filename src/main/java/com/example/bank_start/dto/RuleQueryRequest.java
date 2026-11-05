package com.example.bank_start.dto;

import java.util.List;

/**
 * DTO для описания отдельного запроса внутри динамического правила.
 *
 * <p>Каждый запрос определяет одно условие, которое должно быть выполнено
 * для срабатывания правила. Запрос имеет тип, набор аргументов и модификатор
 * отрицания.
 *
 * <h3>Поддерживаемые типы запросов:</h3>
 * <ul>
 *   <li>{@code USER_OF} — 1 аргумент: тип продукта.</li>
 *   <li>{@code ACTIVE_USER_OF} — 1 аргумент: тип продукта.</li>
 *   <li>{@code TRANSACTION_SUM_COMPARE} — 4 аргумента: тип продукта, тип транзакции,
 *       операция сравнения, константа.</li>
 *   <li>{@code TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW} — 2 аргумента:
 *       тип продукта, операция сравнения.</li>
 * </ul>
 *
 * <h3>Поля:</h3>
 * <ul>
 *   <li><b>query</b> — тип запроса.</li>
 *   <li><b>arguments</b> — массив аргументов запроса.</li>
 *   <li><b>negate</b> — модификатор отрицания (true/false).</li>
 * </ul>
 */
public record RuleQueryRequest(
        String query,
        List<String> arguments,
        Boolean negate
) {
}
