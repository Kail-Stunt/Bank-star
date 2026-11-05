package com.example.bank_start.controller;

import com.example.bank_start.entity.DynamicRule;
import com.example.bank_start.entity.RuleQuery;
import com.example.bank_start.repository.DynamicRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RuleControllerTest {

    private static final UUID TEST_RULE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TEST_PRODUCT_ID = UUID.fromString("ab138afb-f3ba-4a93-b74f-0fcee86d447f");

    @Mock
    private DynamicRuleRepository dynamicRuleRepository;

    @InjectMocks
    private RuleController ruleController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ruleController).build();
    }

    @Test
    void createRule_shouldReturnCreatedRule() throws Exception {
        DynamicRule savedRule = createTestRule();
        when(dynamicRuleRepository.save(any(DynamicRule.class))).thenReturn(savedRule);

        String requestBody = """
            {
                "product_name": "Простой кредит",
                "product_id": "ab138afb-f3ba-4a93-b74f-0fcee86d447f",
                "product_text": "Оформите кредит на выгодных условиях",
                "rule": [
                    {
                        "query": "USER_OF",
                        "arguments": ["CREDIT"],
                        "negate": true
                    },
                    {
                        "query": "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW",
                        "arguments": ["DEBIT", ">"],
                        "negate": false
                    }
                ]
            }
            """;

        mockMvc.perform(post("/rule")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_RULE_ID.toString()))
                .andExpect(jsonPath("$.product_name").value("Простой кредит"))
                .andExpect(jsonPath("$.product_id").value(TEST_PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.product_text").value("Оформите кредит на выгодных условиях"))
                .andExpect(jsonPath("$.rule").isArray())
                .andExpect(jsonPath("$.rule.length()").value(2))
                .andExpect(jsonPath("$.rule[0].query").value("USER_OF"))
                .andExpect(jsonPath("$.rule[0].negate").value(true))
                .andExpect(jsonPath("$.rule[1].query").value("TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW"));
    }

    @Test
    void getAllRules_shouldReturnAllRules() throws Exception {
        DynamicRule rule1 = createTestRule();
        DynamicRule rule2 = createTestRule();
        rule2.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        rule2.setProductName("Invest 500");

        when(dynamicRuleRepository.findAll()).thenReturn(List.of(rule1, rule2));

        mockMvc.perform(get("/rule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].product_name").value("Простой кредит"))
                .andExpect(jsonPath("$.data[1].product_name").value("Invest 500"));
    }

    @Test
    void getAllRules_shouldReturnEmptyList_whenNoRules() throws Exception {
        when(dynamicRuleRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/rule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void deleteRule_shouldReturnNoContent_whenRuleExists() throws Exception {
        DynamicRule rule = createTestRule();
        when(dynamicRuleRepository.findAll()).thenReturn(List.of(rule));

        mockMvc.perform(delete("/rule/" + TEST_PRODUCT_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteRule_shouldReturnNoContent_whenRuleNotFound() throws Exception {
        when(dynamicRuleRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(delete("/rule/" + TEST_PRODUCT_ID))
                .andExpect(status().isNoContent());
    }

    private DynamicRule createTestRule() {
        DynamicRule rule = new DynamicRule();
        rule.setId(TEST_RULE_ID);
        rule.setProductName("Простой кредит");
        rule.setProductId(TEST_PRODUCT_ID.toString());
        rule.setProductText("Оформите кредит на выгодных условиях");

        RuleQuery query1 = new RuleQuery();
        query1.setQueryType("USER_OF");
        query1.setArguments(List.of("CREDIT"));
        query1.setNegate(true);
        rule.addQuery(query1);

        RuleQuery query2 = new RuleQuery();
        query2.setQueryType("TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW");
        query2.setArguments(List.of("DEBIT", ">"));
        query2.setNegate(false);
        rule.addQuery(query2);

        return rule;
    }
}
