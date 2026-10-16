package com.example.bank_start.service;

import com.example.bank_start.dto.RecommendationDTO;
import com.example.bank_start.dto.RecommendationDTO.RecommendationItem;
import com.example.bank_start.rule.RecommendationRuleSet;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final List<RecommendationRuleSet> ruleSets;

    public RecommendationService(List<RecommendationRuleSet> ruleSets) {
        this.ruleSets = ruleSets;
    }

    public RecommendationDTO evaluateRecommendations(UUID userId) {
        List<RecommendationItem> recommendations = ruleSets.stream()
                .map(rule -> rule.evaluate(userId))
                .flatMap(opt -> opt.stream())
                .collect(Collectors.toList());

        return new RecommendationDTO(userId.toString(), recommendations);
    }
}
