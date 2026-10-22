package com.healthify.guardian.platform.iam.infrastructure.notifications.adapters;

import com.healthify.guardian.platform.iam.application.outboundservices.EmailNotificationSender;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.HtmlUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Anti-corruption layer towards Brevo, the e-mail provider: sends the IAM e-mails through Brevo's transactional
 * e-mail API ({@code POST /v3/smtp/email}), so Brevo's contract never reaches the identity model.
 *
 * <p><b>Simulated</b> when {@code iam.email.brevo.api-key} is not set: the e-mail, code included, is only written
 * to the log, so registration and login can be tried locally without sending real e-mails.</p>
 */
@Slf4j
@Component
public class BrevoEmailProviderAdapter implements EmailNotificationSender {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final RestClient restClient;
    private final String apiKey;
    private final String senderEmail;
    private final String senderName;
    private final String passwordResetLinkTemplate;
    private final ZoneId zoneId;

    public BrevoEmailProviderAdapter(RestClient.Builder restClientBuilder,
                                     @Value("${iam.email.brevo.api-url:https://api.brevo.com/v3/smtp/email}") String apiUrl,
                                     @Value("${iam.email.brevo.api-key:}") String apiKey,
                                     @Value("${iam.email.sender.email:}") String senderEmail,
                                     @Value("${iam.email.sender.name:Guardian+}") String senderName,
                                     @Value("${iam.password-reset.link-template:guardianplus://reset-password?token={token}}")
                                     String passwordResetLinkTemplate,
                                     @Value("${iam.zone-id:America/Lima}") String zoneId) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = restClientBuilder.clone().baseUrl(apiUrl).requestFactory(requestFactory).build();
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.passwordResetLinkTemplate = passwordResetLinkTemplate;
        this.zoneId = ZoneId.of(zoneId);
        if (isSimulated()) {
            log.warn("iam.email.brevo.api-key or iam.email.sender.email is not set: IAM e-mails are only logged, not sent");
        }
    }

    @Override
    public boolean sendEmailVerificationCode(Email to, String code, Instant expiresAt) {
        return send(to, "Verifica tu correo en Guardian+",
                codeEmail("Confirma tu correo", "Usa este código para verificar tu cuenta de Guardian+:", code, expiresAt),
                "e-mail verification code " + code);
    }

    @Override
    public boolean sendLoginCode(Email to, String code, Instant expiresAt) {
        return send(to, "Tu código de inicio de sesión de Guardian+",
                codeEmail("Código de inicio de sesión", "Alguien inició sesión en tu cuenta. Si fuiste tú, ingresa este código:",
                        code, expiresAt),
                "login code " + code);
    }

    @Override
    public boolean sendPasswordResetToken(Email to, String token, Instant expiresAt) {
        var link = passwordResetLinkTemplate.replace("{token}", token);
        var html = layout("Restablece tu contraseña", """
                <p>Recibimos una solicitud para cambiar la contraseña de tu cuenta de Guardian+.</p>
                <p><a href="%s" style="display:inline-block;padding:12px 20px;background:#2563eb;color:#ffffff;\
                text-decoration:none;border-radius:8px">Cambiar contraseña</a></p>
                <p>O ingresa este código en la aplicación:</p>
                <p style="font-family:monospace;font-size:14px;word-break:break-all">%s</p>
                <p>Vence a las %s. Si no lo solicitaste, ignora este correo.</p>"""
                .formatted(HtmlUtils.htmlEscape(link), HtmlUtils.htmlEscape(token), time(expiresAt)));
        return send(to, "Restablece tu contraseña de Guardian+", html, "password reset token " + token);
    }

    private boolean send(Email to, String subject, String html, String simulatedSummary) {
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
                            "subject", subject,
                            "htmlContent", html))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException e) {
            // The message may carry details of the request but never the API key, which travels as a header
            log.error("Brevo rejected the e-mail \"{}\": {}", subject, e.getMessage());
            return false;
        }
    }

    private String codeEmail(String title, String intro, String code, Instant expiresAt) {
        return layout(title, """
                <p>%s</p>
                <p style="font-size:32px;font-weight:bold;letter-spacing:8px;margin:24px 0">%s</p>
                <p>Vence a las %s. No compartas este código con nadie.</p>"""
                .formatted(intro, HtmlUtils.htmlEscape(code), time(expiresAt)));
    }

    private static String layout(String title, String body) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto;color:#111827">
                <h2>%s</h2>
                %s
                <p style="color:#6b7280;font-size:12px">Guardian+ · Healthify</p>
                </div>""".formatted(title, body);
    }

    private String time(Instant instant) {
        return TIME.format(instant.atZone(zoneId));
    }

    private boolean isSimulated() {
        return apiKey == null || apiKey.isBlank() || senderEmail == null || senderEmail.isBlank();
    }
}
