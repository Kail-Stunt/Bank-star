package com.example.bank_start.service;

import com.example.bank_start.dto.RecommendationDTO;
import com.example.bank_start.dto.RecommendationDTO.RecommendationItem;
import com.example.bank_start.evaluator.DynamicRuleEvaluator;
import com.example.bank_start.entity.DynamicRule;
import com.example.bank_start.repository.DynamicRuleRepository;
import com.example.bank_start.rule.RecommendationRuleSet;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервис для формирования рекомендаций пользователей.
 *
 * <p>Объединяет проверку фиксированных правил (через {@link RecommendationRuleSet})
 * и динамических правил (через {@link DynamicRuleEvaluator}).
 *
 * <h3>Логика работы:</h3>
 * <ol>
 *   <li>Выполняет все фиксированные правила рекомендаций.</li>
 *   <li>Загружает все динамические правила из базы данных.</li>
 *   <li>Проверяет каждое динамическое правило для пользователя.</li>
 *   <li>Объединяет результаты в единый список рекомендаций.</li>
 * </ol>
 *
 * <h3>Формат ответа:</h3>
 * <p>Возвращает {@link RecommendationDTO} с полем {@code recommendations},
 * содержащим список рекомендуемых продуктов. Если рекомендаций нет —
 * возвращается пустой массив (HTTP 200 OK).
 */
@Service
public class RecommendationService {

    private final List<RecommendationRuleSet> ruleSets;
    private final DynamicRuleRepository dynamicRuleRepository;
    private final DynamicRuleEvaluator dynamicRuleEvaluator;

    /**
     * Создаёт сервис рекомендаций.
     *
     * @param ruleSets список фиксированных правил рекомендаций
     * @param dynamicRuleRepository репозиторий для динамических правил
     * @param dynamicRuleEvaluator оценщик динамических правил
     */
    public RecommendationService(List<RecommendationRuleSet> ruleSets,
                                  DynamicRuleRepository dynamicRuleRepository,
                                  DynamicRuleEvaluator dynamicRuleEvaluator) {
        this.ruleSets = ruleSets;
        this.dynamicRuleRepository = dynamicRuleRepository;
        this.dynamicRuleEvaluator = dynamicRuleEvaluator;
    }

    /**
     * Формирует список рекомендаций для заданного пользователя.
     *
     * <p>Проверяет как фиксированные, так и динамические правила.
     * Динамические правила загружаются из базы данных и оцениваются
     * с учётом транзакций пользователя.
     *
     * @param userId идентификатор пользователя
     * @return DTO с идентификатором пользователя и списком рекомендаций
     */
    public RecommendationDTO evaluateRecommendations(UUID userId) {
        List<RecommendationItem> recommendations = ruleSets.stream()
                .map(rule -> rule.evaluate(userId))
                .flatMap(opt -> opt.stream())
                .collect(Collectors.toList());

        // Evaluate dynamic rules from database
        List<DynamicRule> dynamicRules = dynamicRuleRepository.findAll();
        List<RecommendationItem> dynamicRecommendations = dynamicRules.stream()
                .filter(rule -> dynamicRuleEvaluator.evaluateRule(rule, userId))
                .map(rule -> new RecommendationItem(
                        rule.getProductName(),
                        rule.getId().toString(),
                        rule.getProductText()
                ))
                .collect(Collectors.toList());

        recommendations.addAll(dynamicRecommendations);

        return new RecommendationDTO(userId.toString(), recommendations);
    }
}
