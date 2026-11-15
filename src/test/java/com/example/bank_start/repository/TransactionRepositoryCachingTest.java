package com.example.bank_start.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionRepositoryCachingTest {

    private static final UUID TEST_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Mock
    private JdbcTemplate jdbcTemplate;

    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository(jdbcTemplate);
    }

    @Test
    void hasProductType_shouldCacheResult() {
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq(TEST_USER_ID), eq("DEBIT")))
                .thenReturn(5);

        boolean result1 = repository.hasProductType(TEST_USER_ID, "DEBIT");
        boolean result2 = repository.hasProductType(TEST_USER_ID, "DEBIT");

        assertThat(result1).isTrue();
        assertThat(result2).isTrue();
        verify(jdbcTemplate, times(1)).queryForObject(any(String.class), eq(Integer.class), eq(TEST_USER_ID), eq("DEBIT"));
    }

    @Test
    void hasProductType_shouldCacheDifferentProductTypesSeparately() {
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq(TEST_USER_ID), eq("DEBIT")))
                .thenReturn(5);
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq(TEST_USER_ID), eq("CREDIT")))
                .thenReturn(0);

        boolean debitResult = repository.hasProductType(TEST_USER_ID, "DEBIT");
        boolean creditResult = repository.hasProductType(TEST_USER_ID, "CREDIT");

        assertThat(debitResult).isTrue();
        assertThat(creditResult).isFalse();
        verify(jdbcTemplate, times(2)).queryForObject(any(String.class), eq(Integer.class), any(UUID.class), any(String.class));
    }

    @Test
    void getSumByProductTypeAndTransactionType_shouldCacheResult() {
        when(jdbcTemplate.queryForObject(any(String.class), eq(BigDecimal.class), eq(TEST_USER_ID), eq("DEBIT"), eq("DEPOSIT")))
                .thenReturn(new BigDecimal("150000"));

        BigDecimal result1 = repository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "DEPOSIT");
        BigDecimal result2 = repository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "DEPOSIT");

        assertThat(result1).isEqualTo(new BigDecimal("150000"));
        assertThat(result2).isEqualTo(new BigDecimal("150000"));
        verify(jdbcTemplate, times(1)).queryForObject(any(String.class), eq(BigDecimal.class), eq(TEST_USER_ID), eq("DEBIT"), eq("DEPOSIT"));
    }

    @Test
    void countTransactionsByProductType_shouldCacheResult() {
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq(TEST_USER_ID), eq("DEBIT")))
                .thenReturn(10);

        int count1 = repository.countTransactionsByProductType(TEST_USER_ID, "DEBIT");
        int count2 = repository.countTransactionsByProductType(TEST_USER_ID, "DEBIT");

        assertThat(count1).isEqualTo(10);
        assertThat(count2).isEqualTo(10);
        verify(jdbcTemplate, times(1)).queryForObject(any(String.class), eq(Integer.class), eq(TEST_USER_ID), eq("DEBIT"));
    }

    @Test
    void clearAllCaches_shouldInvalidateAllCaches() {
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq(TEST_USER_ID), eq("DEBIT")))
                .thenReturn(5);
        when(jdbcTemplate.queryForObject(any(String.class), eq(BigDecimal.class), eq(TEST_USER_ID), eq("DEBIT"), eq("DEPOSIT")))
                .thenReturn(new BigDecimal("100000"));

        repository.hasProductType(TEST_USER_ID, "DEBIT");
        repository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "DEPOSIT");
        repository.countTransactionsByProductType(TEST_USER_ID, "DEBIT");

        repository.clearAllCaches();

        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq(TEST_USER_ID), eq("DEBIT")))
                .thenReturn(3);
        when(jdbcTemplate.queryForObject(any(String.class), eq(BigDecimal.class), eq(TEST_USER_ID), eq("DEBIT"), eq("DEPOSIT")))
                .thenReturn(new BigDecimal("50000"));

        int countAfterClear = repository.countTransactionsByProductType(TEST_USER_ID, "DEBIT");
        BigDecimal sumAfterClear = repository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "DEPOSIT");

        assertThat(countAfterClear).isEqualTo(3);
        assertThat(sumAfterClear).isEqualTo(new BigDecimal("50000"));
    }
}
