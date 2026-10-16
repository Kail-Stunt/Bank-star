package com.example.bank_start.rule;

import com.example.bank_start.dto.RecommendationDTO.RecommendationItem;
import com.example.bank_start.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleCreditRuleTest {

    private static final UUID TEST_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Mock
    private TransactionRepository transactionRepository;

    private SimpleCreditRule rule;

    @BeforeEach
    void setUp() {
        rule = new SimpleCreditRule(transactionRepository);
    }

    @Test
    void evaluate_shouldReturnRecommendation_whenAllConditionsMet() {
        when(transactionRepository.hasNoProductType(TEST_USER_ID, "CREDIT")).thenReturn(true);
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "DEPOSIT"))
                .thenReturn(new BigDecimal("200000"));
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "WITHDRAW"))
                .thenReturn(new BigDecimal("150000"));

        var result = rule.evaluate(TEST_USER_ID);

        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("Простой кредит");
        assertThat(result.get().id()).isEqualTo("ab138afb-f3ba-4a93-b74f-0fcee86d447f");
    }

    @Test
    void evaluate_shouldReturnEmpty_whenHasCreditProduct() {
        when(transactionRepository.hasNoProductType(TEST_USER_ID, "CREDIT")).thenReturn(false);

        var result = rule.evaluate(TEST_USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void evaluate_shouldReturnEmpty_whenDepositsLessThanWithdrawals() {
        when(transactionRepository.hasNoProductType(TEST_USER_ID, "CREDIT")).thenReturn(true);
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "DEPOSIT"))
                .thenReturn(new BigDecimal("50000"));
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "WITHDRAW"))
                .thenReturn(new BigDecimal("150000"));

        var result = rule.evaluate(TEST_USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void evaluate_shouldReturnEmpty_whenWithdrawalsBelowThreshold() {
        when(transactionRepository.hasNoProductType(TEST_USER_ID, "CREDIT")).thenReturn(true);
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "DEPOSIT"))
                .thenReturn(new BigDecimal("200000"));
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "WITHDRAW"))
                .thenReturn(new BigDecimal("50000"));

        var result = rule.evaluate(TEST_USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void getRuleName_shouldReturnRuleName() {
        assertThat(rule.getRuleName()).isEqualTo("Простой кредит");
    }
}
