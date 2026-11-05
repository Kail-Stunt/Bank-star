package com.example.bank_start.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Сущность для хранения статистики срабатываний динамических правил рекомендаций.
 *
 * <p>Каждая запись связывает динамическое правило с количеством раз,
 * когда оно было выполнено для какого-либо пользователя.
 *
 * <h3>Связи:</h3>
 * <ul>
 *   <li>ManyToOne с {@link DynamicRule} — статистика привязана к правилу.</li>
 * </ul>
 */
@Entity
@Table(name = "RULE_FIRE_STATS", schema = "PUBLIC")
public class RuleFiringStats {

    /**
     * Уникальный идентификатор записи статистики (UUID).
     * Генерируется автоматически при сохранении.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Идентификатор динамического правила, для которого ведётся статистика.
     */
    @Column(name = "rule_id", nullable = false, unique = true)
    private UUID ruleId;

    /**
     * Количество срабатываний правила.
     * Увеличивается на 1 при каждом выполнении правила для пользователя.
     */
    @Column(name = "fire_count", nullable = false)
    private Long fireCount = 0L;

    public RuleFiringStats() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRuleId() {
        return ruleId;
    }

    public void setRuleId(UUID ruleId) {
        this.ruleId = ruleId;
    }

    public Long getFireCount() {
        return fireCount;
    }

    public void setFireCount(Long fireCount) {
        this.fireCount = fireCount;
    }
}
