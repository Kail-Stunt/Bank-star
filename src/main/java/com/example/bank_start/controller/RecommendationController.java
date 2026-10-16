package com.example.bank_start.controller;

import com.example.bank_start.dto.RecommendationDTO;
import com.example.bank_start.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/recommendation")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{user_id}")
    public ResponseEntity<RecommendationDTO> getRecommendations(@PathVariable("user_id") UUID userId) {
        RecommendationDTO dto = recommendationService.evaluateRecommendations(userId);
        return ResponseEntity.ok(dto);
    }
}
