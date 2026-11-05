package com.example.bank_start.controller;

import com.example.bank_start.dto.DynamicRuleRequest;
import com.example.bank_start.dto.DynamicRuleResponse;
import com.example.bank_start.dto.RuleListResponse;
import com.example.bank_start.dto.RuleQueryRequest;
import com.example.bank_start.dto.RuleQueryResponse;
import com.example.bank_start.entity.DynamicRule;
import com.example.bank_start.entity.RuleQuery;
import com.example.bank_start.repository.DynamicRuleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST-контроллер для управления динамическими правилами рекомендаций.
 *
 * <p>Предоставляет три эндпоинта:
 * <ul>
 *   <li>{@code POST /rule} — создание нового динамического правила.</li>
 *   <li>{@code GET /rule} — получение списка всех динамических правил.</li>
 *   <li>{@code DELETE /rule/{product_id}} — удаление правила по идентификатору продукта.</li>
 * </ul>
 *
 * <h3>API Endpoints:</h3>
 * <table border="1">
 *   <tr>
 *     <th>Метод</th>
 *     <th>Путь</th>
 *     <th>Описание</th>
 *     <th>Ответ</th>
 *   </tr>
 *   <tr>
 *     <td>POST</td>
 *     <td>/rule</td>
 *     <td>Создание нового правила</td>
 *     <td>200 OK с созданным правилом</td>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>/rule</td>
 *     <td>Получение всех правил</td>
 *     <td>200 OK со списком правил</td>
 *   </tr>
 *   <tr>
 *     <td>DELETE</td>
 *     <td>/rule/{product_id}</td>
 *     <td>Удаление правила</td>
 *     <td>204 No Content</td>
 *   </tr>
 * </table>
 */
@RestController
@RequestMapping("/rule")
public class RuleController {

    private final DynamicRuleRepository dynamicRuleRepository;

    /**
     * Создаёт контроллер с указанным репозиторием.
     *
     * @param dynamicRuleRepository репозиторий для работы с динамическими правилами
     */
    public RuleController(DynamicRuleRepository dynamicRuleRepository) {
        this.dynamicRuleRepository = dynamicRuleRepository;
    }

    /**
     * Создаёт новое динамическое правило рекомендаций.
     *
     * <p>Принимает запрос с информацией о рекомендуемом продукте и набором условий.
     * Сохраняет правило и все его запросы в базе данных.
     *
     * <h3>Тело запроса:</h3>
     * <pre>
     * {
     *   "product_name": "Простой кредит",
     *   "product_id": "ab138afb-f3ba-4a93-b74f-0fcee86d447f",
     *   "product_text": "Оформите кредит на выгодных условиях",
     *   "rule": [...]
     * }
     * </pre>
     *
     * <h3>Ответ:</h3>
     * <p>200 OK с созданным правилом (включая сгенерированный идентификатор).
     *
     * @param request тело запроса с данными нового правила
     * @return ResponseEntity с созданным правилом и статусом 200 OK
     */
    @PostMapping
    public ResponseEntity<DynamicRuleResponse> createRule(@RequestBody DynamicRuleRequest request) {
        DynamicRule dynamicRule = new DynamicRule();
        dynamicRule.setProductName(request.product_name());
        dynamicRule.setProductId(request.product_id());
        dynamicRule.setProductText(request.product_text());

        // Convert and add queries
        if (request.rule() != null) {
            for (RuleQueryRequest queryRequest : request.rule()) {
                RuleQuery query = new RuleQuery();
                query.setQueryType(queryRequest.query());
                query.setArguments(queryRequest.arguments());
                query.setNegate(queryRequest.negate() != null ? queryRequest.negate() : false);
                dynamicRule.addQuery(query);
            }
        }

        DynamicRule saved = dynamicRuleRepository.save(dynamicRule);
        return ResponseEntity.ok(toResponse(saved));
    }

    /**
     * Возвращает список всех динамических правил рекомендаций.
     *
     * <h3>Ответ:</h3>
     * <p>200 OK с объектом {@code RuleListResponse}, содержащим массив правил.
     *
     * <pre>
     * {
     *   "data": [
     *     {
     *       "id": "uuid",
     *       "product_name": "Простой кредит",
     *       ...
     *     }
     *   ]
     * }
     * </pre>
     *
     * @return ResponseEntity со списком правил и статусом 200 OK
     */
    @GetMapping
    public ResponseEntity<RuleListResponse> getAllRules() {
        List<DynamicRuleResponse> rules = dynamicRuleRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new RuleListResponse(rules));
    }

    /**
     * Удаляет динамическое правило по идентификатору продукта.
     *
     * <p>Ищет правило по полю {@code product_id} и удаляет его вместе
     * со всеми вложенными запросами.
     *
     * <h3>Параметры пути:</h3>
     * <ul>
     *   <li>{@code product_id} — UUID продукта, для которого нужно удалить правило.</li>
     * </ul>
     *
     * <h3>Ответ:</h3>
     * <p>204 No Content (независимо от того, найдено правило или нет).
     *
     * @param productId идентификатор продукта (UUID в строковом формате)
     * @return ResponseEntity со статусом 204 No Content
     */
    @DeleteMapping("/{product_id}")
    public ResponseEntity<Void> deleteRule(@PathVariable("product_id") String productId) {
        dynamicRuleRepository.findAll().stream()
                .filter(rule -> rule.getProductId().equals(productId))
                .findFirst()
                .ifPresent(dynamicRuleRepository::delete);
        return ResponseEntity.noContent().build();
    }

    /**
     * Преобразует сущность {@link DynamicRule} в DTO {@link DynamicRuleResponse}.
     *
     * @param rule сущность правила для преобразования
     * @return DTO с данными правила
     */
    private DynamicRuleResponse toResponse(DynamicRule rule) {
        List<RuleQueryResponse> queries = rule.getQueries().stream()
                .map(query -> new RuleQueryResponse(
                        query.getQueryType(),
                        query.getArgumentsList(),
                        query.getNegate()
                ))
                .collect(Collectors.toList());

        return new DynamicRuleResponse(
                rule.getId(),
                rule.getProductName(),
                rule.getProductId(),
                rule.getProductText(),
                queries
        );
    }
}
