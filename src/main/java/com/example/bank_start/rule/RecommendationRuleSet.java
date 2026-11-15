package com.example.bank_start.rule;

import com.example.bank_start.dto.RecommendationDTO.RecommendationItem;
import java.util.Optional;
import java.util.UUID;

/**
 * Интерфейс для фиксированных правил рекомендаций.
 *
 * <p>Каждая реализация этого интерфейса представляет одно правило,
 * которое проверяет условия для пользователя и возвращает рекомендацию,
 * если все условия выполнены.
 *
 * <h3>Реализации:</h3>
 * <ul>
 *   <li>{@link Invest500Rule} — рекомендация инвестиционного продукта.</li>
 *   <li>{@link SimpleCreditRule} — рекомендация кредита.</li>
 * </ul>
 *
 * <h3>Логика работы:</h3>
 * <p>Метод {@link #evaluate(UUID)} выполняет проверку условий на основе
 * транзакций пользователя. Если все условия выполнены — возвращает
 * {@link RecommendationItem}, иначе — {@code Optional.empty()}.
 *
 * @see RecommendationService
 * @see DynamicRuleEvaluator
 */
public interface RecommendationRuleSet {
    /**
     * Проверяет, выполняется ли правило для заданного пользователя.
     *
     * <p>Метод анализирует транзакции пользователя и возвращает рекомендацию,
     * если все условия правила выполнены.
     *
     * @param userId идентификатор пользователя для проверки
     * @return {@link RecommendationItem} с рекомендацией, если правило выполнено;
     *         {@code Optional.empty()} если правило не выполнено
     */
    Optional<RecommendationItem> evaluate(UUID userId);

    /**
     * Возвращает имя правила.
     *
     * <p>Имя используется для идентификации правила в логах,
     * статистике и ответах API.
     *
     * @return имя правила
     */
    String getRuleName();
}
