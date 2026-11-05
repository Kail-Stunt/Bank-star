package com.example.bank_start.service;

import com.example.bank_start.dto.RecommendationDTO;
import com.example.bank_start.dto.RecommendationDTO.RecommendationItem;
import com.example.bank_start.evaluator.DynamicRuleEvaluator;
import com.example.bank_start.repository.DynamicRuleRepository;
import com.example.bank_start.rule.RecommendationRuleSet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    private static final UUID TEST_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Mock
    private RecommendationRuleSet rule1;

    @Mock
    private RecommendationRuleSet rule2;

    @Mock
    private DynamicRuleRepository dynamicRuleRepository;

    @Mock
    private DynamicRuleEvaluator dynamicRuleEvaluator;

    private RecommendationService service;

    @Test
    void evaluateRecommendations_shouldReturnMultipleRecommendations() {
        when(rule1.evaluate(TEST_USER_ID)).thenReturn(Optional.of(new RecommendationItem("Rule1", "id1", "text1")));
        when(rule2.evaluate(TEST_USER_ID)).thenReturn(Optional.of(new RecommendationItem("Rule2", "id2", "text2")));

        service = new RecommendationService(List.of(rule1, rule2), dynamicRuleRepository, dynamicRuleEvaluator);

        RecommendationDTO result = service.evaluateRecommendations(TEST_USER_ID);

        assertThat(result.userId()).isEqualTo(TEST_USER_ID.toString());
        assertThat(result.recommendations()).hasSize(2);
        assertThat(result.recommendations()).extracting(RecommendationItem::name)
                .containsExactly("Rule1", "Rule2");
    }

    @Test
    void evaluateRecommendations_shouldReturnEmptyList_whenNoRulesMatch() {
        when(rule1.evaluate(TEST_USER_ID)).thenReturn(Optional.empty());
        when(rule2.evaluate(TEST_USER_ID)).thenReturn(Optional.empty());

        service = new RecommendationService(List.of(rule1, rule2), dynamicRuleRepository, dynamicRuleEvaluator);

        RecommendationDTO result = service.evaluateRecommendations(TEST_USER_ID);

        assertThat(result.userId()).isEqualTo(TEST_USER_ID.toString());
        assertThat(result.recommendations()).isEmpty();
    }

    @Test
    void evaluateRecommendations_shouldReturnSingleRecommendation_whenOnlyOneRuleMatches() {
        when(rule1.evaluate(TEST_USER_ID)).thenReturn(Optional.of(new RecommendationItem("Rule1", "id1", "text1")));
        when(rule2.evaluate(TEST_USER_ID)).thenReturn(Optional.empty());

        service = new RecommendationService(List.of(rule1, rule2), dynamicRuleRepository, dynamicRuleEvaluator);

        RecommendationDTO result = service.evaluateRecommendations(TEST_USER_ID);

        assertThat(result.userId()).isEqualTo(TEST_USER_ID.toString());
        assertThat(result.recommendations()).hasSize(1);
        assertThat(result.recommendations()).extracting(RecommendationItem::name)
                .containsExactly("Rule1");
    }
}
