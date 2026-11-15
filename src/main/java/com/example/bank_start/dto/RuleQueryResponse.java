package com.example.bank_start.dto;

import java.util.List;

/**
 * DTO для представления запроса внутри динамического правила в ответах API.
 *
 * <p>Используется для сериализации запросов правил в JSON-ответы.
 * Структура идентична {@link RuleQueryRequest}, но с дополнительным полем id.
 *
 * <h3>Поля:</h3>
 * <ul>
 *   <li><b>query</b> — тип запроса.</li>
 *   <li><b>arguments</b> — массив аргументов запроса.</li>
 *   <li><b>negate</b> — модификатор отрицания.</li>
 * </ul>
 */
public record RuleQueryResponse(
        String query,
        List<String> arguments,
        Boolean negate
) {
}
