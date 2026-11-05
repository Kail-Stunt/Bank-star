package com.example.bank_start.dto;

import java.util.List;

/**
 * DTO для ответа с списком всех динамических правил рекомендаций.
 *
 * <p>Используется в ответе на GET-запрос {@code GET /rule}.
 * Содержит массив всех сохранённых правил в базе данных.
 *
 * <h3>Пример JSON:</h3>
 * <pre>
 * {
 *   "data": [
 *     {
 *       "id": "uuid-1",
 *       "product_name": "Простой кредит",
 *       "product_id": "ab138afb-...",
 *       "product_text": "Оформите кредит на выгодных условиях",
 *       "rule": [...]
 *     },
 *     {
 *       "id": "uuid-2",
 *       "product_name": "Invest 500",
 *       "product_id": "d4abe90a-...",
 *       "product_text": "Откройте инвестиционный счёт",
 *       "rule": [...]
 *     }
 *   ]
 * }
 * </pre>
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
