package com.example.bank_start.rule;

import com.example.bank_start.dto.RecommendationDTO.RecommendationItem;
import java.util.Optional;
import java.util.UUID;

public interface RecommendationRuleSet {
    Optional<RecommendationItem> evaluate(UUID userId);
    String getRuleName();
}
