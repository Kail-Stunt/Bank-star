package com.example.bank_start.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Сущность для учёта срабатываний динамических правил рекомендаций.
 *
 * <p>Каждая запись фиксирует факт выдачи рекомендации пользователю
 * на основе конкретного динамического правила. Счётчик увеличивается
 * при каждом срабатывании правила.
 *
 * <h3>Связи:</h3>
 * <ul>
 *   <li>ManyToOne с {@link DynamicRule} — каждая запись привязана к одному правилу.</li>
 * </ul>
 */
@Entity
@Table(name = "RULE_HITS", schema = "PUBLIC")
public class RuleHit {

    /**
     * Уникальный идентификатор записи о срабатывании (UUID).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Ссылка на динамическое правило, которое сработало.
     */
    @ManyToOne
    @JoinColumn(name = "rule_id", nullable = false)
    private DynamicRule dynamicRule;

    /**
     * Количество срабатываний данного правила для конкретного пользователя.
     * Увеличивается на 1 при каждом срабатывании.
     */
    @Column(name = "hit_count", nullable = false)
    private int hitCount = 1;

    public RuleHit() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public DynamicRule getDynamicRule() {
        return dynamicRule;
    }

    public void setDynamicRule(DynamicRule dynamicRule) {
        this.dynamicRule = dynamicRule;
    }

    public int getHitCount() {
        return hitCount;
    }

    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }

    /**
     * Увеличивает счётчик срабатываний на единицу.
     */
    public void increment() {
        this.hitCount++;
    }
}
