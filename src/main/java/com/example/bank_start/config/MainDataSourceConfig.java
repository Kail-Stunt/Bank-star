package com.example.bank_start.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Конфигурация для основной базы данных (bankdb).
 *
 * <p>Содержит настройки DataSource и JdbcTemplate для основной базы данных,
 * которая хранит данные о продуктах, транзакциях и пользователях.
 */
@Configuration
public class MainDataSourceConfig {

    /**
     * DataSource для основной базы данных.
     *
     * @return DataSource на основе свойств spring.datasource
     */
    @Bean
    @Primary
    @Qualifier("mainDataSource")
    public DataSource mainDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:h2:mem:bankdb");
        ds.setDriverClassName("org.h2.Driver");
        ds.setUsername("sa");
        ds.setPassword("");
        return ds;
    }

    /**
     * JdbcTemplate для основной базы данных.
     *
     * @param dataSource DataSource основной базы данных
     * @return JdbcTemplate для выполнения SQL-запросов к основной БД
     */
    @Bean
    @Primary
    public JdbcTemplate mainJdbcTemplate(@Qualifier("mainDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
