package com.healthify.guardian.platform.iam.infrastructure.notifications;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class IamEmailTemplatesTest {

    private final IamEmailTemplates templates = new IamEmailTemplates(ZoneId.of("America/Lima"));
    private final Instant inTenMinutes = Instant.now().plus(Duration.ofMinutes(10));

    @Test
    void drawsEachDigitOfTheCodeInItsOwnBox() {
        var email = templates.loginCode("048213", inTenMinutes);

        assertThat(email.subject()).contains("048213");
        assertThat(email.html()).contains(">0</td>", ">4</td>", ">8</td>", ">2</td>", ">1</td>", ">3</td>");
        assertThat(email.text()).contains("048213");
    }

    @Test
    void tellsWhenTheCodeExpires() {
        var email = templates.emailVerificationCode("048213", inTenMinutes);

        assertThat(email.html()).contains("en 10 minutos");
        assertThat(email.text()).contains("en 10 minutos");
    }

    @Test
    void escapesTheResetToken() {
        var email = templates.passwordResetToken("<script>", inTenMinutes);

        assertThat(email.html()).contains("&lt;script&gt;").doesNotContain("<script>");
        assertThat(email.subject()).doesNotContain("<script>");
    }

    @Test
    void usesTheAppPalette() {
        var email = templates.emailVerificationCode("048213", inTenMinutes);

        assertThat(email.html()).contains("#167A62", "#F8FBF9", "#FFF4CC", "PLATAFORMA DE SALUD", "Guardian+");
    }
}
