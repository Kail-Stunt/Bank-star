package com.example.bank_start.dto;

import java.util.List;

/**
 * DTO для создания нового динамического правила рекомендаций.
 *
 * <p>Используется в POST-запросе {@code POST /rule} для создания
 * нового правила с условиями и рекомендуемым продуктом.
 *
 * <h3>Пример JSON:</h3>
 * <pre>
 * {
 *   "product_name": "Простой кредит",
 *   "product_id": "ab138afb-f3ba-4a93-b74f-0fcee86d447f",
 *   "product_text": "Оформите кредит на выгодных условиях",
 *   "rule": [
 *     {
 *       "query": "USER_OF",
 *       "arguments": ["CREDIT"],
 *       "negate": true
 *     },
 *     {
 *       "query": "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW",
 *       "arguments": ["DEBIT", ">"],
 *       "negate": false
 *     }
 *   ]
 * }
 * </pre>
 *
 * <h3>Поля:</h3>
 * <ul>
 *   <li><b>product_name</b> — имя рекомендуемого продукта.</li>
 *   <li><b>product_id</b> — идентификатор продукта в базе данных.</li>
 *   <li><b>product_text</b> — текстовое описание рекомендации.</li>
 *   <li><b>rule</b> — массив условий (запросов), определяющих логику правила.</li>
 * </ul>
 */
public record DynamicRuleRequest(
        String product_name,
        String product_id,
        String product_text,
        List<RuleQueryRequest> rule
) {
}
