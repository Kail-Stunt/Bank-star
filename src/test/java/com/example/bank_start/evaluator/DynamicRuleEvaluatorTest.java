package com.example.bank_start.evaluator;

import com.example.bank_start.entity.DynamicRule;
import com.example.bank_start.entity.RuleQuery;
import com.example.bank_start.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamicRuleEvaluatorTest {

    private static final UUID TEST_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Mock
    private TransactionRepository transactionRepository;

    private DynamicRuleEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new DynamicRuleEvaluator(transactionRepository);
    }

    @Test
    void evaluateRule_shouldReturnTrue_whenAllQueriesMatch() {
        DynamicRule rule = createRuleWithSingleQuery("USER_OF", List.of("DEBIT"), false);
        when(transactionRepository.hasProductType(TEST_USER_ID, "DEBIT")).thenReturn(true);

        boolean result = evaluator.evaluateRule(rule, TEST_USER_ID);

        assertThat(result).isTrue();
    }

    @Test
    void evaluateRule_shouldReturnFalse_whenQueryDoesNotMatch() {
        DynamicRule rule = createRuleWithSingleQuery("USER_OF", List.of("CREDIT"), false);
        when(transactionRepository.hasProductType(TEST_USER_ID, "CREDIT")).thenReturn(false);

        boolean result = evaluator.evaluateRule(rule, TEST_USER_ID);

        assertThat(result).isFalse();
    }

    @Test
    void evaluateRule_shouldReturnTrue_withNegate_whenQueryDoesNotMatch() {
        DynamicRule rule = createRuleWithSingleQuery("USER_OF", List.of("CREDIT"), true);
        when(transactionRepository.hasProductType(TEST_USER_ID, "CREDIT")).thenReturn(false);

        boolean result = evaluator.evaluateRule(rule, TEST_USER_ID);

        assertThat(result).isTrue();
    }

    @Test
    void evaluateRule_shouldReturnFalse_withNegate_whenQueryMatches() {
        DynamicRule rule = createRuleWithSingleQuery("USER_OF", List.of("CREDIT"), true);
        when(transactionRepository.hasProductType(TEST_USER_ID, "CREDIT")).thenReturn(true);

        boolean result = evaluator.evaluateRule(rule, TEST_USER_ID);

        assertThat(result).isFalse();
    }

    @Test
    void evaluateRule_shouldReturnTrue_whenMultipleQueriesMatch() {
        DynamicRule rule = new DynamicRule();
        RuleQuery query1 = new RuleQuery();
        query1.setQueryType("USER_OF");
        query1.setArguments(List.of("DEBIT"));
        query1.setNegate(false);
        rule.addQuery(query1);

        RuleQuery query2 = new RuleQuery();
        query2.setQueryType("TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW");
        query2.setArguments(List.of("DEBIT", ">"));
        query2.setNegate(false);
        rule.addQuery(query2);

        when(transactionRepository.hasProductType(TEST_USER_ID, "DEBIT")).thenReturn(true);
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "DEPOSIT"))
                .thenReturn(new BigDecimal("200000"));
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "WITHDRAW"))
                .thenReturn(new BigDecimal("100000"));

        boolean result = evaluator.evaluateRule(rule, TEST_USER_ID);

        assertThat(result).isTrue();
    }

    @Test
    void evaluateRule_shouldReturnFalse_whenOneQueryFails() {
        DynamicRule rule = new DynamicRule();
        RuleQuery query1 = new RuleQuery();
        query1.setQueryType("USER_OF");
        query1.setArguments(List.of("DEBIT"));
        query1.setNegate(false);
        rule.addQuery(query1);

        RuleQuery query2 = new RuleQuery();
        query2.setQueryType("TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW");
        query2.setArguments(List.of("DEBIT", ">"));
        query2.setNegate(false);
        rule.addQuery(query2);

        when(transactionRepository.hasProductType(TEST_USER_ID, "DEBIT")).thenReturn(true);
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "DEPOSIT"))
                .thenReturn(new BigDecimal("50000"));
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "WITHDRAW"))
                .thenReturn(new BigDecimal("100000"));

        boolean result = evaluator.evaluateRule(rule, TEST_USER_ID);

        assertThat(result).isFalse();
    }

    @Test
    void evaluateRule_shouldReturnFalse_whenRuleHasNoQueries() {
        DynamicRule rule = new DynamicRule();

        boolean result = evaluator.evaluateRule(rule, TEST_USER_ID);

        assertThat(result).isFalse();
    }

    @Test
    void evaluateRule_shouldReturnTrue_forActiveUser_whenCountGteThreshold() {
        DynamicRule rule = createRuleWithSingleQuery("ACTIVE_USER_OF", List.of("DEBIT"), false);
        when(transactionRepository.countTransactionsByProductType(TEST_USER_ID, "DEBIT")).thenReturn(5);

        boolean result = evaluator.evaluateRule(rule, TEST_USER_ID);

        assertThat(result).isTrue();
    }

    @Test
    void evaluateRule_shouldReturnFalse_forActiveUser_whenCountBelowThreshold() {
        DynamicRule rule = createRuleWithSingleQuery("ACTIVE_USER_OF", List.of("DEBIT"), false);
        when(transactionRepository.countTransactionsByProductType(TEST_USER_ID, "DEBIT")).thenReturn(4);

        boolean result = evaluator.evaluateRule(rule, TEST_USER_ID);

        assertThat(result).isFalse();
    }

    @Test
    void evaluateRule_shouldReturnTrue_forTransactionSumCompare_withGreaterThan() {
        DynamicRule rule = createRuleWithSingleQuery("TRANSACTION_SUM_COMPARE",
                List.of("DEBIT", "DEPOSIT", ">", "100000"), false);
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "DEPOSIT"))
                .thenReturn(new BigDecimal("150000"));

        boolean result = evaluator.evaluateRule(rule, TEST_USER_ID);

        assertThat(result).isTrue();
    }

    @Test
    void evaluateRule_shouldReturnFalse_forTransactionSumCompare_withGreaterThan() {
        DynamicRule rule = createRuleWithSingleQuery("TRANSACTION_SUM_COMPARE",
                List.of("DEBIT", "DEPOSIT", ">", "100000"), false);
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "DEPOSIT"))
                .thenReturn(new BigDecimal("50000"));

        boolean result = evaluator.evaluateRule(rule, TEST_USER_ID);

        assertThat(result).isFalse();
    }

    @Test
    void evaluateRule_shouldReturnTrue_forTransactionSumCompare_withLessThan() {
        DynamicRule rule = createRuleWithSingleQuery("TRANSACTION_SUM_COMPARE",
                List.of("DEBIT", "WITHDRAW", "<", "50000"), false);
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "WITHDRAW"))
                .thenReturn(new BigDecimal("30000"));

        boolean result = evaluator.evaluateRule(rule, TEST_USER_ID);

        assertThat(result).isTrue();
    }

    @Test
    void evaluateRule_shouldReturnTrue_forTransactionSumCompare_withEqual() {
        DynamicRule rule = createRuleWithSingleQuery("TRANSACTION_SUM_COMPARE",
                List.of("DEBIT", "DEPOSIT", "=", "100000"), false);
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "DEPOSIT"))
                .thenReturn(new BigDecimal("100000"));

        boolean result = evaluator.evaluateRule(rule, TEST_USER_ID);

        assertThat(result).isTrue();
    }

    @Test
    void evaluateRule_shouldReturnTrue_forTransactionSumCompare_withGreaterThanOrEqual() {
        DynamicRule rule = createRuleWithSingleQuery("TRANSACTION_SUM_COMPARE",
                List.of("DEBIT", "DEPOSIT", ">=", "100000"), false);
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "DEPOSIT"))
                .thenReturn(new BigDecimal("100000"));

        boolean result = evaluator.evaluateRule(rule, TEST_USER_ID);

        assertThat(result).isTrue();
    }

    @Test
    void evaluateRule_shouldReturnTrue_forTransactionSumCompare_withLessThanOrEqual() {
        DynamicRule rule = createRuleWithSingleQuery("TRANSACTION_SUM_COMPARE",
                List.of("DEBIT", "DEPOSIT", "<=", "100000"), false);
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "DEPOSIT"))
                .thenReturn(new BigDecimal("50000"));

        boolean result = evaluator.evaluateRule(rule, TEST_USER_ID);

        assertThat(result).isTrue();
    }

    @Test
    void evaluateRule_shouldThrowException_forUnknownQueryType() {
        DynamicRule rule = createRuleWithSingleQuery("UNKNOWN_QUERY", List.of(), false);

        assertThatThrownBy(() -> evaluator.evaluateRule(rule, TEST_USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown query type");
    }

    @Test
    void evaluateRule_shouldThrowException_forMissingArguments() {
        DynamicRule rule = createRuleWithSingleQuery("USER_OF", List.of(), false);

        assertThatThrownBy(() -> evaluator.evaluateRule(rule, TEST_USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requires 1 argument");
    }

    @Test
    void evaluateRule_shouldThrowException_forTransactionSumCompareMissingArguments() {
        DynamicRule rule = createRuleWithSingleQuery("TRANSACTION_SUM_COMPARE", List.of("DEBIT"), false);

        assertThatThrownBy(() -> evaluator.evaluateRule(rule, TEST_USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requires 4 arguments");
    }

    @Test
    void evaluateRule_shouldThrowException_forDepositWithdrawCompareMissingArguments() {
        DynamicRule rule = createRuleWithSingleQuery("TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW", List.of("DEBIT"), false);

        assertThatThrownBy(() -> evaluator.evaluateRule(rule, TEST_USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requires 2 arguments");
    }

    @Test
    void evaluateRule_shouldThrowException_forUnknownOperator() {
        DynamicRule rule = createRuleWithSingleQuery("TRANSACTION_SUM_COMPARE",
                List.of("DEBIT", "DEPOSIT", "invalid", "100"), false);
        when(transactionRepository.getSumByProductTypeAndTransactionType(TEST_USER_ID, "DEBIT", "DEPOSIT"))
                .thenReturn(new BigDecimal("200"));

        assertThatThrownBy(() -> evaluator.evaluateRule(rule, TEST_USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown comparison operator");
    }

    private DynamicRule createRuleWithSingleQuery(String queryType, List<String> arguments, boolean negate) {
        DynamicRule rule = new DynamicRule();
        RuleQuery query = new RuleQuery();
        query.setQueryType(queryType);
        query.setArguments(arguments);
        query.setNegate(negate);
        rule.addQuery(query);
        return rule;
    }
}
