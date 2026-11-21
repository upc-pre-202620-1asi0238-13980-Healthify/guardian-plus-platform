package com.healthify.guardian.platform.subscriptions.infrastructure.payments.stripe;

import com.stripe.exception.SignatureVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StripeWebhookControllerTest {

    private StripeWebhookService stripeWebhookService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        stripeWebhookService =
                mock(StripeWebhookService.class);

        StripeWebhookController controller =
                new StripeWebhookController(
                        stripeWebhookService
                );

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .build();
    }

    @Test
    void shouldReturnOkWhenWebhookIsValid()
            throws Exception {

        String payload =
                """
                {
                  "id": "evt_test",
                  "type": "payment_intent.succeeded"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/webhooks/stripe")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .header(
                                        "Stripe-Signature",
                                        "valid-signature"
                                )
                                .content(
                                        payload
                                )
                )
                .andExpect(
                        status().isOk()
                );

        verify(stripeWebhookService)
                .process(
                        payload,
                        "valid-signature"
                );
    }

    @Test
    void shouldReturnBadRequestWhenStripeSignatureIsInvalid()
            throws Exception {

        SignatureVerificationException exception =
                mock(
                        SignatureVerificationException.class
                );

        doThrow(exception)
                .when(stripeWebhookService)
                .process(
                        anyString(),
                        anyString()
                );

        mockMvc.perform(
                        post("/api/v1/webhooks/stripe")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .header(
                                        "Stripe-Signature",
                                        "invalid-signature"
                                )
                                .content(
                                        "{}"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );
    }
}