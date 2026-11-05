package com.example.bank_start.dto;

import java.util.List;

/**
 * DTO для ответа с списком всех динамических правил рекомендаций.
 *
 * <p>Используется в ответе на GET-запрос {@code GET /rule}.
 * Содержит массив всех сохранённых правил в базе данных.
 *
 * <h3>Поля:</h3>
 * <ul>
 *   <li><b>data</b> — массив объектов {@code DynamicRuleResponse},
 *       содержащий все динамические правила рекомендаций.</li>
 * </ul>
 */
public record RuleListResponse(
        List<DynamicRuleResponse> data
) {
}
