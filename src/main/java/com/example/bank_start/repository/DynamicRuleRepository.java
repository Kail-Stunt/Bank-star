package com.example.bank_start.repository;

import com.example.bank_start.entity.DynamicRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA-репозиторий для работы с сущностью {@link DynamicRule}.
 *
 * <p>Обеспечивает CRUD-операции для динамических правил рекомендаций.
 * Работает с отдельной базой данных (rulesdb), настроенной через
 * {@link RulesDataSourceConfig}.
 *
 * <h3>Основные операции:</h3>
 * <ul>
 *   <li>Сохранение нового правила с вложенными запросами.</li>
 *   <li>Получение всех правил.</li>
 *   <li>Удаление правила по идентификатору.</li>
 * </ul>
 */
@Repository
public interface DynamicRuleRepository extends JpaRepository<DynamicRule, UUID> {
}
