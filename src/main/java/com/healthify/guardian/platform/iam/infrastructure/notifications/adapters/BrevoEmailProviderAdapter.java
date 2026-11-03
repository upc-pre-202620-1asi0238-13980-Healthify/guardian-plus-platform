package com.healthify.guardian.platform.iam.infrastructure.notifications.adapters;

import com.healthify.guardian.platform.iam.application.outboundservices.EmailNotificationSender;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.Email;
import com.healthify.guardian.platform.iam.infrastructure.notifications.IamEmailTemplates;
import com.healthify.guardian.platform.iam.infrastructure.notifications.IamEmailTemplates.EmailContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * Anti-corruption layer towards Brevo, the e-mail provider: sends the IAM e-mails through Brevo's transactional
 * e-mail API ({@code POST /v3/smtp/email}), so Brevo's contract never reaches the identity model. The content comes
 * from {@link IamEmailTemplates}.
 *
 * <p><b>Simulated</b> when {@code iam.email.brevo.api-key} or {@code iam.email.sender.email} is not set: the e-mail,
 * code included, is only written to the log, so registration and login can be tried locally without sending real
 * e-mails.</p>
 */
@Slf4j
@Component
public class BrevoEmailProviderAdapter implements EmailNotificationSender {

    private final RestClient restClient;
    private final String apiKey;
    private final String senderEmail;
    private final String senderName;
    private final IamEmailTemplates templates;

    public BrevoEmailProviderAdapter(RestClient.Builder restClientBuilder,
                                     @Value("${iam.email.brevo.api-url:https://api.brevo.com/v3/smtp/email}") String apiUrl,
                                     @Value("${iam.email.brevo.api-key:}") String apiKey,
                                     @Value("${iam.email.sender.email:}") String senderEmail,
                                     @Value("${iam.email.sender.name:Guardian+}") String senderName,
                                     @Value("${iam.zone-id:America/Lima}") String zoneId) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = restClientBuilder.clone().baseUrl(apiUrl).requestFactory(requestFactory).build();
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.templates = new IamEmailTemplates(ZoneId.of(zoneId));
        if (isSimulated()) {
            log.warn("iam.email.brevo.api-key or iam.email.sender.email is not set: IAM e-mails are only logged, not sent");
        }
    }

    @Override
    public boolean sendEmailVerificationCode(Email to, String code, Instant expiresAt) {
        return send(to, templates.emailVerificationCode(code, expiresAt), "e-mail verification code " + code);
    }

    @Override
    public boolean sendLoginCode(Email to, String code, Instant expiresAt) {
        return send(to, templates.loginCode(code, expiresAt), "login code " + code);
    }

    @Override
    public boolean sendPasswordResetToken(Email to, String token, Instant expiresAt) {
        return send(to, templates.passwordResetToken(token, expiresAt), "password reset token " + token);
    }

    private boolean send(Email to, EmailContent content, String simulatedSummary) {
        if (isSimulated()) {
            log.info("[simulated e-mail] to {}: {}", to.value(), simulatedSummary);
            return true;
        }
        try {
            restClient.post()
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "sender", Map.of("name", senderName, "email", senderEmail),
                            "to", List.of(Map.of("email", to.value())),
                            "subject", content.subject(),
                            "htmlContent", content.html(),
                            "textContent", content.text()))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException e) {
            // The message may carry details of the request but never the API key, which travels as a header
            log.error("Brevo rejected the e-mail \"{}\": {}", content.subject(), e.getMessage());
            return false;
        }
    }

    private boolean isSimulated() {
        return apiKey == null || apiKey.isBlank() || senderEmail == null || senderEmail.isBlank();
    }
}
