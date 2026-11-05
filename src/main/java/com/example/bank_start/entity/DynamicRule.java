package com.example.bank_start.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сущность JPA для хранения динамического правила рекомендаций.
 *
 * <p>Динамическое правило содержит информацию о рекомендуемом продукте
 * и набор условий (запросов), которые должны быть выполнены пользователем
 * для получения данной рекомендации.
 *
 * <p>Правило сохраняется в отдельной базе данных (rulesdb) и используется
 * при формировании рекомендаций на эндпоинте {@code GET /recommendation/<user_id>}.
 *
 * <h3>Связи:</h3>
 * <ul>
 *   <li>OneToMany с {@link RuleQuery} — одно правило содержит множество запросов.</li>
 * </ul>
 */
@Entity
@Table(name = "DYNAMIC_RULES", schema = "PUBLIC")
public class DynamicRule {

    /**
     * Уникальный идентификатор правила (UUID).
     * Генерируется автоматически при сохранении.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Имя продукта, который рекомендуется пользователю
     * при выполнении всех условий правила.
     */
    @Column(name = "product_name", nullable = false)
    private String productName;

    /**
     * Идентификатор продукта в таблице PRODUCTS основной базы данных.
     * Ссылается на внешний ключ продукта, который будет рекомендован.
     */
    @Column(name = "product_id", nullable = false)
    private String productId;

    /**
     * Текстовое описание рекомендации.
     * Отображается пользователю в поле {@code text} ответа API рекомендаций.
     */
    @Column(name = "product_text", nullable = false)
    private String productText;

    /**
     * Набор запросов (условий), определяющих логику правила.
     * Каждый запрос проверяет определённое условие относительно транзакций
     * и продуктов пользователя. Правило срабатывает, когда все запросы
     * возвращают true (после применения модификатора {@code negate}).
     */
    @OneToMany(mappedBy = "dynamicRule", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RuleQuery> queries = new ArrayList<>();

    public DynamicRule() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductText() {
        return productText;
    }

    public void setProductText(String productText) {
        this.productText = productText;
    }

    public List<RuleQuery> getQueries() {
        return queries;
    }

    public void setQueries(List<RuleQuery> queries) {
        this.queries = queries;
    }

    /**
     * Добавляет запрос в правило и устанавливает обратную связь.
     *
     * @param query запрос на добавление
     */
    public void addQuery(RuleQuery query) {
        queries.add(query);
        query.setDynamicRule(this);
    }

    /**
     * Удаляет запрос из правила и очищает обратную связь.
     *
     * @param query запрос на удаление
     */
    public void removeQuery(RuleQuery query) {
        queries.remove(query);
        query.setDynamicRule(null);
    }
}
