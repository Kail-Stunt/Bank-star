package com.example.bank_start.controller;

import com.example.bank_start.dto.RecommendationDTO;
import com.example.bank_start.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/recommendation")
@Tag(name = "Recommendations", description = "API для получения рекомендаций пользователей")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Operation(summary = "Получение рекомендаций", description = "Возвращает список рекомендаций для пользователя на основе его транзакций и правил")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Рекомендации получены"),
            @ApiResponse(responseCode = "400", description = "Некорректный формат user_id")
    })
    @GetMapping("/{user_id}")
    public ResponseEntity<RecommendationDTO> getRecommendations(@Parameter(description = "UUID пользователя") @PathVariable("user_id") UUID userId) {
        RecommendationDTO dto = recommendationService.evaluateRecommendations(userId);
        return ResponseEntity.ok(dto);
    }
}
