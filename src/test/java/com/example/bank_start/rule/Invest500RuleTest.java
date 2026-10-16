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
class Invest500RuleTest {

    private static final UUID TEST_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final BigDecimal SAVING_DEPOSITS = new BigDecimal("5000");

    @Mock
    private TransactionRepository transactionRepository;

    private Invest500Rule rule;

    @BeforeEach
    void setUp() {
        rule = new Invest500Rule(transactionRepository);
    }

    @Test
    void evaluate_shouldReturnRecommendation_whenAllConditionsMet() {
        when(transactionRepository.hasProductType(TEST_USER_ID, "DEBIT")).thenReturn(true);
        when(transactionRepository.hasNoProductType(TEST_USER_ID, "INVEST")).thenReturn(true);
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "SAVING", "DEPOSIT"))
                .thenReturn(SAVING_DEPOSITS);

        var result = rule.evaluate(TEST_USER_ID);

        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("Invest 500");
        assertThat(result.get().id()).isEqualTo("147f6a0f-3b91-413b-ab99-87f081d60d5a");
    }

    @Test
    void evaluate_shouldReturnEmpty_whenNoDebitProduct() {
        when(transactionRepository.hasProductType(TEST_USER_ID, "DEBIT")).thenReturn(false);

        var result = rule.evaluate(TEST_USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void evaluate_shouldReturnEmpty_whenHasInvestProduct() {
        when(transactionRepository.hasProductType(TEST_USER_ID, "DEBIT")).thenReturn(true);
        when(transactionRepository.hasNoProductType(TEST_USER_ID, "INVEST")).thenReturn(false);

        var result = rule.evaluate(TEST_USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void evaluate_shouldReturnEmpty_whenSavingDepositsBelowThreshold() {
        when(transactionRepository.hasProductType(TEST_USER_ID, "DEBIT")).thenReturn(true);
        when(transactionRepository.hasNoProductType(TEST_USER_ID, "INVEST")).thenReturn(true);
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "SAVING", "DEPOSIT"))
                .thenReturn(new BigDecimal("500"));

        var result = rule.evaluate(TEST_USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void getRuleName_shouldReturnRuleName() {
        assertThat(rule.getRuleName()).isEqualTo("Invest 500");
    }
}
