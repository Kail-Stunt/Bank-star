package com.example.bank_start.rule;

import com.example.bank_start.dto.RecommendationDTO.RecommendationItem;
import com.example.bank_start.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Фиксированное правило рекомендации продукта «Простой кредит».
 *
 * <p>Правило рекомендует кредитный продукт пользователю, если выполнены
 * все следующие условия:
 * <ol>
 *   <li>У пользователя НЕТ продуктов типа {@code CREDIT}.</li>
 *   <li>Сумма депозитов по продуктам типа {@code DEBIT} больше суммы выводов.</li>
 *   <li>Сумма выводов по продуктам типа {@code DEBIT} превышает 100 000.</li>
 * </ol>
 *
 * <h3>Параметры правила:</h3>
 * <ul>
 *   <li><b>ID правила:</b> {@code ab138afb-f3ba-4a93-b74f-0fcee86d447f}</li>
 *   <li><b>Название:</b> {@code Простой кредит}</li>
 *   <li><b>Порог выводов DEBIT:</b> 100 000</li>
 * </ul>
 *
 * <h3>Текст рекомендации:</h3>
 * <p>{@code "Оформите кредит на выгодных условиях"}
 *
 * @see RecommendationRuleSet
 */
@Component
public class SimpleCreditRule implements RecommendationRuleSet {

    private static final String RULE_ID = "ab138afb-f3ba-4a93-b74f-0fcee86d447f";
    private static final String RULE_NAME = "Простой кредит";
    private static final String RULE_TEXT = "Оформите кредит на выгодных условиях";
    private static final BigDecimal WITHDRAWAL_THRESHOLD = new BigDecimal("100000");

    private final TransactionRepository transactionRepository;

    public SimpleCreditRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Optional<RecommendationItem> evaluate(UUID userId) {
        boolean hasNoCredit = transactionRepository.hasNoProductType(userId, "CREDIT");
        BigDecimal debitDeposits = transactionRepository.getSumByProductTypeAndTransactionType(
                userId, "DEBIT", "DEPOSIT");
        BigDecimal debitWithdrawals = transactionRepository.getSumByProductTypeAndTransactionType(
                userId, "DEBIT", "WITHDRAW");

        if (hasNoCredit
                && debitDeposits.compareTo(debitWithdrawals) > 0
                && debitWithdrawals.compareTo(WITHDRAWAL_THRESHOLD) > 0) {
            return Optional.of(new RecommendationItem(RULE_NAME, RULE_ID, RULE_TEXT));
        }
        return Optional.empty();
    }

    @Override
    public String getRuleName() {
        return RULE_NAME;
    }
}
