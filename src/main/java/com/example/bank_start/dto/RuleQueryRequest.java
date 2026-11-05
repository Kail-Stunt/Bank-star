package com.example.bank_start.dto;

import java.util.List;

/**
 * DTO для описания отдельного запроса внутри динамического правила.
 *
 * <p>Каждый запрос определяет одно условие, которое должно быть выполнено
 * для срабатывания правила.
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
