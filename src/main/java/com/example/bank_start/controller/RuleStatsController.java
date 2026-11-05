package com.example.bank_start.controller;

import com.example.bank_start.entity.DynamicRule;
import com.example.bank_start.entity.RuleFiringStats;
import com.example.bank_start.repository.DynamicRuleRepository;
import com.example.bank_start.repository.RuleFiringStatsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST-контроллер для получения статистики срабатываний динамических правил.
 *
 * <p>Предоставляет эндпоинт {@code GET /rule/stats} для получения
 * списка всех правил с количеством их срабатываний.
 *
 * <h3>Логика:</h3>
 * <ul>
 *   <li>Если правило срабатывало — возвращается актуальный счётчик.</li>
 *   <li>Если правило не срабатывало — возвращается 0.</li>
 *   <li>Все правила присутствуют в ответе, даже если у них 0 срабатываний.</li>
 * </ul>
 *
 * <h3>API Endpoint:</h3>
 * <table border="1">
 *   <tr>
 *     <th>Метод</th>
 *     <th>Путь</th>
 *     <th>Ответ</th>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>/rule/stats</td>
 *     <td>200 OK со списком статистики</td>
 *   </tr>
 * </table>
 */
@RestController
@RequestMapping("/rule")
public class RuleStatsController {

    private final DynamicRuleRepository dynamicRuleRepository;
    private final RuleFiringStatsRepository statsRepository;

    /**
     * Создаёт контроллер с указанными репозиториями.
     *
     * @param dynamicRuleRepository репозиторий для динамических правил
     * @param statsRepository репозиторий для статистики срабатываний
     */
    public RuleStatsController(DynamicRuleRepository dynamicRuleRepository,
                                RuleFiringStatsRepository statsRepository) {
        this.dynamicRuleRepository = dynamicRuleRepository;
        this.statsRepository = statsRepository;
    }

    /**
     * Возвращает статистику срабатываний всех динамических правил.
     *
     * <p>Для каждого правила возвращается его идентификатор и количество
     * раз, когда оно было выполнено для пользователей.
     *
     * <h3>Пример ответа:</h3>
     * <pre>
     * {
     *   "stats": [
     *     {
     *       "rule_id": "uuid-1",
     *       "count": 5
     *     },
     *     {
     *       "rule_id": "uuid-2",
     *       "count": 0
     *     }
     *   ]
     * }
     * </pre>
     *
     * @return ResponseEntity со списком статистики и статусом 200 OK
     */
    @GetMapping("/stats")
    public ResponseEntity<RuleStatsResponse> getRuleStats() {
        List<DynamicRule> allRules = dynamicRuleRepository.findAll();

        // Build a map of ruleId -> fireCount for rules that have stats
        Map<UUID, Long> statsMap = statsRepository.findAll().stream()
                .collect(Collectors.toMap(
                        RuleFiringStats::getRuleId,
                        RuleFiringStats::getFireCount
                ));

        List<RuleStatItem> stats = allRules.stream()
                .map(rule -> new RuleStatItem(
                        rule.getId(),
                        statsMap.getOrDefault(rule.getId(), 0L)
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new RuleStatsResponse(stats));
    }

    /**
     * DTO для ответа со статистикой срабатываний правил.
     *
     * <h3>Поля:</h3>
     * <ul>
     *   <li><b>stats</b> — массив элементов статистики по каждому правилу.</li>
     * </ul>
     */
    public record RuleStatsResponse(List<RuleStatItem> stats) {
    }

    /**
     * DTO для элемента статистики одного правила.
     *
     * <h3>Поля:</h3>
     * <ul>
     *   <li><b>rule_id</b> — уникальный идентификатор правила (UUID).</li>
     *   <li><b>count</b> — количество срабатываний правила.</li>
     * </ul>
     */
    public record RuleStatItem(
            UUID rule_id,
            Long count
    ) {
    }
}
