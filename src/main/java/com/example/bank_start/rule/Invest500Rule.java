package com.example.bank_start.rule;

import com.example.bank_start.dto.RecommendationDTO.RecommendationItem;
import com.example.bank_start.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
public class Invest500Rule implements RecommendationRuleSet {

    private static final String RULE_ID = "147f6a0f-3b91-413b-ab99-87f081d60d5a";
    private static final String RULE_NAME = "Invest 500";
    private static final String RULE_TEXT = "Откройте инвестиционный счет для приумножения ваших сбережений";
    private static final BigDecimal THRESHOLD = new BigDecimal("1000");

    private final TransactionRepository transactionRepository;

    public Invest500Rule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Optional<RecommendationItem> evaluate(UUID userId) {
        boolean hasDebit = transactionRepository.hasProductType(userId, "DEBIT");
        boolean hasNoInvest = transactionRepository.hasNoProductType(userId, "INVEST");
        BigDecimal savingDeposits = transactionRepository.getSumByProductTypeAndTransactionType(
                userId, "SAVING", "DEPOSIT");

        if (hasDebit && hasNoInvest && savingDeposits.compareTo(THRESHOLD) > 0) {
            return Optional.of(new RecommendationItem(RULE_NAME, RULE_ID, RULE_TEXT));
        }
        return Optional.empty();
    }

    @Override
    public String getRuleName() {
        return RULE_NAME;
    }
}
