package com.example.bank_start.controller;

import com.example.bank_start.dto.RecommendationDTO;
import com.example.bank_start.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RecommendationControllerTest {

    private static final UUID TEST_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    private MockMvc mockMvc;

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private RecommendationController recommendationController;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(recommendationController).build();
    }

    @Test
    void getRecommendations_shouldReturn200WithRecommendations() throws Exception {
        var item = new RecommendationDTO.RecommendationItem("Invest 500", "rule-id-1", "Recommendation text");
        var dto = new RecommendationDTO(TEST_USER_ID.toString(), List.of(item));

        when(recommendationService.evaluateRecommendations(TEST_USER_ID)).thenReturn(dto);

        mockMvc.perform(get("/recommendation/{user_id}", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(TEST_USER_ID.toString()))
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.recommendations.length()").value(1))
                .andExpect(jsonPath("$.recommendations[0].name").value("Invest 500"))
                .andExpect(jsonPath("$.recommendations[0].id").value("rule-id-1"))
                .andExpect(jsonPath("$.recommendations[0].text").value("Recommendation text"));
    }

    @Test
    void getRecommendations_shouldReturn200WithEmptyList_whenNoRecommendations() throws Exception {
        var dto = new RecommendationDTO(TEST_USER_ID.toString(), List.of());

        when(recommendationService.evaluateRecommendations(TEST_USER_ID)).thenReturn(dto);

        mockMvc.perform(get("/recommendation/{user_id}", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(TEST_USER_ID.toString()))
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.recommendations.length()").value(0));
    }
}
