package com.healthify.guardian.platform.subscriptions.interfaces.rest;

import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.subscriptions.application.commandservices.SubscriptionCommandService;
import com.healthify.guardian.platform.subscriptions.application.queryservices.SubscriptionApplicationQueryService;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RequestSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriberId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SubscriptionControllerValidationTest {

    @Mock
    private SubscriptionCommandService subscriptionCommandService;

    @Mock
    private SubscriptionApplicationQueryService subscriptionQueryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        SubscriptionController controller =
                new SubscriptionController(
                        subscriptionCommandService,
                        subscriptionQueryService
                );

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .setControllerAdvice(
                                new SubscriptionRestExceptionHandler()
                        )
                        .setValidator(validator)
                        .build();
    }

    @Test
    void shouldReturnBadRequestWhenSubscriberIdIsMissing()
            throws Exception {

        String requestBody = """
                {
                  "planId": "11111111-1111-1111-1111-111111111111"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/subscriptions")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        MediaType.APPLICATION_JSON
                                )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value(
                                        "Bad Request"
                                )
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Request validation failed"
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.details.subscriberId"
                        ).value(
                                "subscriberId is required"
                        )
                );
    }

    @Test
    void shouldReturnBadRequestWhenNewPlanIdIsMissing()
            throws Exception {

        UUID subscriptionId =
                UUID.randomUUID();

        String requestBody =
                """
                {
                }
                """;

        mockMvc.perform(
                        patch(
                                "/api/v1/subscriptions/{subscriptionId}/plan",
                                subscriptionId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Request validation failed"
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.details.newPlanId"
                        ).value(
                                "newPlanId is required"
                        )
                );
    }

    @Test
    void shouldReturnBadRequestWhenSubscriptionIdIsInvalid()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/subscriptions/{subscriptionId}/cancellation",
                                "invalid-uuid"
                        )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value(
                                        "Bad Request"
                                )
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid value for parameter: subscriptionId"
                                )
                );
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyIsMalformed()
            throws Exception {

        String malformedJson =
                """
                {
                  "subscriberId":
                """;

        mockMvc.perform(
                        post("/api/v1/subscriptions")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        malformedJson
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value(
                                        "Bad Request"
                                )
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Malformed request body"
                                )
                );
    }

    @Test
    void shouldReturnCreatedSubscriptionThroughHttp()
            throws Exception {

        UUID subscriptionId =
                UUID.randomUUID();

        UUID subscriberId =
                UUID.randomUUID();

        UUID planId =
                UUID.randomUUID();

        Subscription subscription =
                createPendingSubscription(
                        subscriptionId,
                        subscriberId,
                        planId
                );

        doReturn(
                Result.success(
                        subscription
                )
        ).when(subscriptionCommandService)
                .handle(
                        any(
                                RequestSubscriptionCommand.class
                        )
                );

        String requestBody =
                """
                {
                  "subscriberId": "%s",
                  "planId": "%s"
                }
                """.formatted(
                        subscriberId,
                        planId
                );

        mockMvc.perform(
                        post("/api/v1/subscriptions")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        MediaType.APPLICATION_JSON
                                )
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(
                                        subscriptionId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.subscriberId")
                                .value(
                                        subscriberId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.planId")
                                .value(
                                        planId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(
                                        "PENDING"
                                )
                )
                .andExpect(
                        jsonPath("$.cancelAtPeriodEnd")
                                .value(false)
                );
    }

    private Subscription createPendingSubscription(
            UUID subscriptionId,
            UUID subscriberId,
            UUID planId
    ) {

        Instant createdAt =
                Instant.parse(
                        "2026-10-23T10:00:00Z"
                );

        Subscription subscription =
                new Subscription();

        subscription.setId(
                new SubscriptionId(
                        subscriptionId
                )
        );

        subscription.setSubscriberId(
                new SubscriberId(
                        subscriberId
                )
        );

        subscription.setPlanId(
                new PlanId(
                        planId
                )
        );

        subscription.setStatus(
                SubscriptionStatus.PENDING
        );

        subscription.setCurrentPeriodStart(
                null
        );

        subscription.setCurrentPeriodEnd(
                null
        );

        subscription.setCancelAtPeriodEnd(
                false
        );

        subscription.setCreatedAt(
                createdAt
        );

        subscription.setUpdatedAt(
                createdAt
        );

        return subscription;
    }
}