package com.example.bank_start.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO для представления динамического правила рекомендаций в ответах API.
 *
 * <p>Содержит полную информацию о правиле: идентификатор, данные о рекомендуемом
 * продукте и набор условий (запросов). Используется как в ответе на
 * POST {@code /rule}, так и в ответе на GET {@code /rule}.
 *
 * <h3>Пример JSON:</h3>
 * <pre>
 * {
 *   "id": "uuid",
 *   "product_name": "Простой кредит",
 *   "product_id": "ab138afb-f3ba-4a93-b74f-0fcee86d447f",
 *   "product_text": "Оформите кредит на выгодных условиях",
 *   "rule": [
 *     {
 *       "query": "USER_OF",
 *       "arguments": ["CREDIT"],
 *       "negate": true
 *     }
 *   ]
 * }
 * </pre>
 *
 * <h3>Поля:</h3>
 * <ul>
 *   <li><b>id</b> — уникальный идентификатор правила (UUID).</li>
 *   <li><b>product_name</b> — имя рекомендуемого продукта.</li>
 *   <li><b>product_id</b> — идентификатор продукта в базе данных.</li>
 *   <li><b>product_text</b> — текстовое описание рекомендации.</li>
 *   <li><b>rule</b> — массив условий (запросов), определяющих логику правила.</li>
 * </ul>
 */
public record DynamicRuleResponse(
        UUID id,
        String product_name,
        String product_id,
        String product_text,
        List<RuleQueryResponse> rule
) {
}
