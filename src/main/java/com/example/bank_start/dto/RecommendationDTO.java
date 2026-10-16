package com.example.bank_start.dto;

import java.util.List;

public record RecommendationDTO(
    String userId,
    List<RecommendationItem> recommendations
) {
    public record RecommendationItem(
        String name,
        String id,
        String text
    ) {}
}
