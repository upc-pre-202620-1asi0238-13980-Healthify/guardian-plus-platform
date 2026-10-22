package com.healthify.guardian.platform.iam.infrastructure.notifications;

import org.springframework.web.util.HtmlUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Builds the IAM e-mails with the look of the mobile app's authentication screens: the green header with the
 * Guardian+ mark, a white sheet, the code in six boxes like the OTP screen and the expiry notice in the app's pastel
 * yellow. Colors come from the app's palette ({@code core/designsystem/theme/Color.kt}).
 *
 * <p>Written for e-mail clients: table layout, inline styles and no images or web fonts, which Gmail and Outlook
 * strip or block. The fonts fall back to the system ones.</p>
 */
public final class IamEmailTemplates {

    /** A rendered e-mail: subject, HTML body and the plain-text alternative. */
    public record EmailContent(String subject, String html, String text) {
    }

    // Guardian+ palette
    private static final String PRIMARY = "#167A62";
    private static final String BACKGROUND = "#F8FBF9";
    private static final String TEXT_PRIMARY = "#1E2C27";
    private static final String TEXT_MUTED = "#6C7B75";
    private static final String FIELD_BORDER = "#E3E8E5";
    private static final String BORDER = "#CFE0D8";
    private static final String ACCENT_MINT = "#D9F0E7";
    private static final String ACCENT_FOREGROUND = "#123128";
    private static final String NOTICE = "#FFF4CC";
    private static final String NOTICE_TEXT = "#806600";
    private static final String DESTRUCTIVE_PASTEL = "#FBE4E4";
    private static final String DESTRUCTIVE = "#C53B3B";

    private static final String HEADING_FONT = "'Plus Jakarta Sans','Segoe UI',Roboto,Helvetica,Arial,sans-serif";
    private static final String BODY_FONT = "Inter,'Segoe UI',Roboto,Helvetica,Arial,sans-serif";
    private static final String MONO_FONT = "'JetBrains Mono',SFMono-Regular,Menlo,Consolas,monospace";

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final ZoneId zoneId;

    public IamEmailTemplates(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public EmailContent emailVerificationCode(String code, Instant expiresAt) {
        var body = """
                %s
                <p style="margin:0 0 24px;font:15px/1.6 %s;color:%s">Ingresa este código en la aplicación para \
                confirmar tu correo y activar tu cuenta de cuidador.</p>
                %s
                %s""".formatted(
                sheetTitle("Código de verificación", "Confirma que este correo es tuyo"),
                BODY_FONT, TEXT_MUTED, codeBoxes(code), expiryNotice(expiresAt));
        return new EmailContent(
                "Tu código de verificación de Guardian+: " + code,
                layout("Tu código de verificación es " + code, "Confirma<br>tu correo.",
                        "Un paso más para empezar a cuidar con Guardian+.", body),
                plainText("Confirma tu correo en Guardian+.", "Tu código de verificación es: " + code, expiresAt,
                        "Si no creaste una cuenta en Guardian+, ignora este correo."));
    }

    public EmailContent loginCode(String code, Instant expiresAt) {
        var body = """
                %s
                <p style="margin:0 0 24px;font:15px/1.6 %s;color:%s">Alguien inició sesión en tu cuenta con tu \
                contraseña. Si fuiste tú, ingresa este código para terminar de entrar.</p>
                %s
                %s
                %s""".formatted(
                sheetTitle("Código de inicio de sesión", "Segundo paso de tu inicio de sesión"),
                BODY_FONT, TEXT_MUTED, codeBoxes(code), expiryNotice(expiresAt),
                callout(DESTRUCTIVE_PASTEL, DESTRUCTIVE, "<strong>¿No fuiste tú?</strong> Alguien conoce tu "
                        + "contraseña. Cámbiala desde «¿Olvidaste tu contraseña?» en la aplicación."));
        return new EmailContent(
                "Tu código de inicio de sesión de Guardian+: " + code,
                layout("Tu código de inicio de sesión es " + code, "Verifica<br>tu identidad.",
                        "Confirmamos que eres tú antes de darte acceso.", body),
                plainText("Inicio de sesión en Guardian+.", "Tu código de inicio de sesión es: " + code, expiresAt,
                        "¿No fuiste tú? Alguien conoce tu contraseña: cámbiala desde la aplicación."));
    }

    public EmailContent passwordResetToken(String token, Instant expiresAt) {
        var body = """
                %s
                <p style="margin:0 0 24px;font:15px/1.6 %s;color:%s">Copia este código en la pantalla \
                <strong style="color:%s">Nueva contraseña</strong> de la aplicación y elige una contraseña que solo \
                tú conozcas.</p>
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                <tr><td style="background:%s;border:1px solid %s;border-radius:12px;padding:18px 20px">
                <div style="font:11px/1 %s;letter-spacing:2px;color:%s;margin-bottom:10px">CÓDIGO DE RECUPERACIÓN</div>
                <div style="font:600 14px/1.5 %s;color:%s;word-break:break-all">%s</div>
                </td></tr>
                </table>
                <div style="height:20px;line-height:20px">&nbsp;</div>
                %s
                %s""".formatted(
                sheetTitle("Recuperar contraseña", "Recibimos una solicitud para tu cuenta"),
                BODY_FONT, TEXT_MUTED, TEXT_PRIMARY,
                ACCENT_MINT, BORDER, MONO_FONT, ACCENT_FOREGROUND, MONO_FONT, ACCENT_FOREGROUND,
                HtmlUtils.htmlEscape(token),
                expiryNotice(expiresAt),
                callout(BACKGROUND, TEXT_MUTED, "Si no pediste cambiar tu contraseña, ignora este correo: tu "
                        + "contraseña actual sigue funcionando."));
        return new EmailContent(
                "Recupera tu contraseña de Guardian+",
                layout("Usa este código para crear una nueva contraseña", "Recupera<br>tu acceso.",
                        "Elige una contraseña que solo tú conozcas.", body),
                plainText("Recupera tu contraseña de Guardian+.", "Tu código de recuperación es: " + token, expiresAt,
                        "Si no pediste cambiar tu contraseña, ignora este correo."));
    }

    private String layout(String preheader, String headline, String subtitle, String sheet) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <meta name="color-scheme" content="light">
                <title>Guardian+</title>
                </head>
                <body style="margin:0;padding:0;background:%1$s">
                <div style="display:none;max-height:0;overflow:hidden;opacity:0">%2$s</div>
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="background:%1$s">
                <tr><td align="center" style="padding:24px 12px">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="max-width:520px">
                <tr><td style="background:%3$s;border-radius:20px 20px 0 0;padding:32px 24px 40px">
                  <table role="presentation" cellpadding="0" cellspacing="0" border="0"><tr>
                    <td width="44" height="44" align="center" valign="middle" style="background:rgba(255,255,255,0.15);border-radius:12px;font:700 24px/44px %4$s;color:#FFFFFF">+</td>
                    <td style="padding-left:12px">
                      <div style="font:11px/1.4 %5$s;letter-spacing:2px;color:rgba(255,255,255,0.7)">PLATAFORMA DE SALUD</div>
                      <div style="font:600 22px/1.3 %4$s;color:#FFFFFF">Guardian+</div>
                    </td>
                  </tr></table>
                  <div style="font:700 34px/1.12 %4$s;color:#FFFFFF;margin-top:32px">%6$s</div>
                  <div style="font:14px/1.5 %7$s;color:rgba(255,255,255,0.65);margin-top:12px">%8$s</div>
                </td></tr>
                <tr><td style="background:#FFFFFF;border:1px solid %9$s;border-top:0;border-radius:0 0 20px 20px;padding:28px 24px">
                %10$s
                </td></tr>
                <tr><td align="center" style="padding:24px 16px 0;font:12px/1.6 %7$s;color:%11$s">
                  Guardian+ · Healthify<br>Recibes este correo por una acción en tu cuenta de Guardian+.
                </td></tr>
                </table>
                </td></tr>
                </table>
                </body>
                </html>""".formatted(BACKGROUND, HtmlUtils.htmlEscape(preheader), PRIMARY, HEADING_FONT, MONO_FONT,
                headline, BODY_FONT, HtmlUtils.htmlEscape(subtitle), FIELD_BORDER, sheet, TEXT_MUTED);
    }

    private static String sheetTitle(String title, String subtitle) {
        return """
                <div style="font:600 22px/1.3 %s;color:%s">%s</div>
                <div style="font:14px/1.5 %s;color:%s;margin:4px 0 20px">%s</div>""".formatted(
                HEADING_FONT, TEXT_PRIMARY, title, BODY_FONT, TEXT_MUTED, subtitle);
    }

    /** The code drawn in one box per digit, like the OTP screen of the app. */
    private static String codeBoxes(String code) {
        var cells = new StringBuilder();
        for (int i = 0; i < code.length(); i++) {
            if (i > 0) {
                cells.append("<td width=\"8\" style=\"font-size:0\">&nbsp;</td>");
            }
            cells.append("""
                    <td align="center" valign="middle" width="40" height="52" style="background:#FFFFFF;border:1.5px solid %s;\
                    border-radius:12px;font:600 24px/52px %s;color:%s">%s</td>""".formatted(
                    PRIMARY, MONO_FONT, TEXT_PRIMARY, HtmlUtils.htmlEscape(String.valueOf(code.charAt(i)))));
        }
        return """
                <table role="presentation" align="center" cellpadding="0" cellspacing="0" border="0" style="margin:0 auto 24px">
                <tr>%s</tr>
                </table>""".formatted(cells);
    }

    private String expiryNotice(Instant expiresAt) {
        return callout(NOTICE, NOTICE_TEXT, "El código vence a las <strong>%s</strong> (en %s). Nunca lo compartas "
                .formatted(time(expiresAt), minutesLeft(expiresAt)) + "con nadie, incluyendo el soporte de Guardian+.");
    }

    private static String callout(String background, String color, String html) {
        return """
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-top:12px">
                <tr><td style="background:%s;border-radius:12px;padding:14px 16px;font:13px/1.55 %s;color:%s">%s</td></tr>
                </table>""".formatted(background, BODY_FONT, color, html);
    }

    private String plainText(String intro, String secretLine, Instant expiresAt, String closing) {
        return """
                %s

                %s

                Vence a las %s (en %s). Nunca lo compartas con nadie, incluyendo el soporte de Guardian+.

                %s

                Guardian+ · Healthify""".formatted(intro, secretLine, time(expiresAt), minutesLeft(expiresAt), closing);
    }

    private String time(Instant instant) {
        return TIME.format(instant.atZone(zoneId));
    }

    private static String minutesLeft(Instant expiresAt) {
        var minutes = Math.max(1, (Duration.between(Instant.now(), expiresAt).getSeconds() + 59) / 60);
        return minutes == 1 ? "1 minuto" : minutes + " minutos";
    }
}
