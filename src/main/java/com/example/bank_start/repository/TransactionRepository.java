package com.example.bank_start.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean hasProductType(UUID userId, String productType) {
        String sql = """
            SELECT COUNT(*) FROM TRANSACTIONS t
            JOIN PRODUCTS p ON t.PRODUCT_ID = p.ID
            WHERE t.USER_ID = ? AND p.TYPE = ?
            """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, productType);
        return count != null && count > 0;
    }

    public boolean hasNoProductType(UUID userId, String productType) {
        return !hasProductType(userId, productType);
    }

    public BigDecimal getSumByProductTypeAndTransactionType(UUID userId, String productType, String transactionType) {
        String sql = """
            SELECT COALESCE(SUM(t.AMOUNT), 0) FROM TRANSACTIONS t
            JOIN PRODUCTS p ON t.PRODUCT_ID = p.ID
            WHERE t.USER_ID = ? AND p.TYPE = ? AND t.TYPE = ?
            """;
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, userId, productType, transactionType);
    }

    public List<String> getProductTypes(UUID userId) {
        String sql = """
            SELECT DISTINCT p.TYPE FROM TRANSACTIONS t
            JOIN PRODUCTS p ON t.PRODUCT_ID = p.ID
            WHERE t.USER_ID = ?
            """;
        return jdbcTemplate.queryForList(sql, String.class, userId);
    }
}
