package com.example.bank_start.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с данными о пользователях.
 *
 * <p>Использует JdbcTemplate для выполнения SQL-запросов к основной базе данных.
 * Обеспечивает поиск пользователей по имени и фамилии.
 */
@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Создаёт репозиторий с указанным JdbcTemplate.
     *
     * @param jdbcTemplate JdbcTemplate для выполнения SQL-запросов
     */
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Ищет пользователей по имени и фамилии.
     *
     * <p>Выполняет SQL-запрос к таблице USERS, ища записи,
     * где FIRST_NAME и LAST_NAME совпадают с указанными значениями.
     *
     * @param firstName имя пользователя
     * @param lastName фамилия пользователя
     * @return список идентификаторов найденных пользователей
     */
    public List<UUID> findByFirstNameAndLastName(String firstName, String lastName) {
        String sql = """
            SELECT ID FROM USERS
            WHERE FIRST_NAME = ? AND LAST_NAME = ?
            """;
        return jdbcTemplate.queryForList(sql, UUID.class, firstName, lastName);
    }

    /**
     * Получает полное имя пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return полное имя в формате "FirstName LastName" или null
     */
    public String getFullNameById(UUID userId) {
        String sql = """
            SELECT FIRST_NAME || ' ' || LAST_NAME FROM USERS
            WHERE ID = ?
            """;
        return jdbcTemplate.queryForObject(sql, String.class, userId);
    }
}
