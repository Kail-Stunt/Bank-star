package com.example.bank_start.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация для базы данных динамических правил (rulesdb).
 *
 * <p>Содержит настройки DataSource, EntityManagerFactory и JPA-репозиториев
 * для отдельной базы данных, которая хранит динамические правила рекомендаций.
 *
 * <h3>Основные компоненты:</h3>
 * <ul>
 *   <li>{@code rulesDataSource} — DataSource для rulesdb.</li>
 *   <li>{@code rulesEntityManagerFactory} — EntityManagerFactory для JPA-сущностей.</li>
 *   <li>{@code rulesTransactionManager} — менеджер транзакций для rulesdb.</li>
 *   <li>{@code rulesJdbcTemplate} — JdbcTemplate для rulesdb (на случай использования JDBC).</li>
 * </ul>
 *
 * <h3>Пакет сканирования:</h3>
 * <p>Репозитории сканируются в пакете {@code com.example.bank_start.repository}.
 * Сущности находятся в пакете {@code com.example.bank_start.entity}.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.bank_start.repository",
        entityManagerFactoryRef = "rulesEntityManagerFactory",
        transactionManagerRef = "rulesTransactionManager"
)
public class RulesDataSourceConfig {

    /**
     * DataSource для базы данных правил.
     *
     * @return DataSource на основе свойств spring.rules.datasource
     */
    @Bean
    @Qualifier("rulesDataSource")
    public DataSource rulesDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:h2:mem:rulesdb");
        ds.setDriverClassName("org.h2.Driver");
        ds.setUsername("sa");
        ds.setPassword("");
        return ds;
    }

    /**
     * EntityManagerFactory для базы данных правил.
     *
     * @param dataSource DataSource базы данных правил
     * @return LocalContainerEntityManagerFactoryBean с настроенным EntityManagerFactory
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean rulesEntityManagerFactory(
            @Qualifier("rulesDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.example.bank_start.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(false);
        factory.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "update");
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        factory.setJpaPropertyMap(jpaProperties);

        return factory;
    }

    /**
     * Менеджер транзакций для базы данных правил.
     *
     * @param entityManagerFactory EntityManagerFactory для rulesdb
     * @return JpaTransactionManager для управления транзакциями
     */
    @Bean
    public PlatformTransactionManager rulesTransactionManager(
            @Qualifier("rulesEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }

    /**
     * JdbcTemplate для базы данных правил.
     *
     * @param dataSource DataSource базы данных правил
     * @return JdbcTemplate для выполнения SQL-запросов к rulesdb
     */
    @Bean
    public JdbcTemplate rulesJdbcTemplate(@Qualifier("rulesDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
