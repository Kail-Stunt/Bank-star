package com.example.bank_start.repository;

import com.example.bank_start.entity.RuleFiringStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA-репозиторий для работы со статистикой срабатываний правил.
 *
 * <p>Обеспечивает операции:
 * <ul>
 *   <li>Поиск статистики по идентификатору правила.</li>
 *   <li>Увеличение счётчика срабатываний.</li>
 *   <li>Удаление статистики при удалении правила.</li>
 * </ul>
 */
@Repository
public interface RuleFiringStatsRepository extends JpaRepository<RuleFiringStats, UUID> {

    /**
     * Находит статистику срабатываний по идентификатору правила.
     *
     * @param ruleId идентификатор правила
     * @return Optional со статистикой, если найдена
     */
    Optional<RuleFiringStats> findByRuleId(UUID ruleId);

    /**
     * Увеличивает счётчик срабатываний правила на 1.
     *
     * @param ruleId идентификатор правила
     */
    @Modifying
    @Query("UPDATE RuleFiringStats s SET s.fireCount = s.fireCount + 1 WHERE s.ruleId = :ruleId")
    void incrementFireCount(@Param("ruleId") UUID ruleId);

    /**
     * Удаляет статистику по идентификатору правила.
     *
     * @param ruleId идентификатор правила
     */
    void deleteByRuleId(UUID ruleId);
}
