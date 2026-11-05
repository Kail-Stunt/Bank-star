package com.example.bank_start.dto;

import java.util.List;

/**
 * DTO для создания нового динамического правила рекомендаций.
 *
 * <p>Используется в POST-запросе {@code POST /rule} для создания
 * нового правила с условиями и рекомендуемым продуктом.
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
