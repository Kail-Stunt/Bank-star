package com.example.bank_start.repository;

import com.example.bank_start.entity.RuleHit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA-репозиторий для работы с сущностью {@link RuleHit}.
 *
 * <p>Обеспечивает операции для учёта и получения статистики
 * срабатываний динамических правил рекомендаций.
 *
 * <h3>Основные операции:</h3>
 * <ul>
 *   <li>Поиск записи по идентификатору правила.</li>
 *   <li>Получение всех записей о срабатываниях.</li>
 *   <li>Удаление записей при удалении правила.</li>
 * </ul>
 */
@Repository
public interface RuleHitRepository extends JpaRepository<RuleHit, UUID> {

    /**
     * Находит запись о срабатываниях для заданного правила.
     *
     * @param ruleId идентификатор динамического правила
     * @return опциональная запись о срабатываниях
     */
    Optional<RuleHit> findByDynamicRule_Id(UUID ruleId);

    /**
     * Получает все записи о срабатываниях правил.
     *
     * @return список всех записей о срабатываниях
     */
    List<RuleHit> findAll();
}
