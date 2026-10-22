package com.healthify.guardian.platform.iam.infrastructure.security.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * HTTP security of the platform. The API is stateless: the mobile app sends the JWT issued by IAM on every request
 * ({@code Authorization: Bearer <token>}), so there are no sessions, no cookies and therefore no CSRF tokens.
 *
 * <p>While the other bounded contexts do not read the authenticated user yet, requests without a token are still
 * accepted ({@code iam.security.enforce-authentication=false}). A token that is sent is always validated, and an
 * invalid or expired one is rejected with {@code 401}. Turn enforcement on to require a token everywhere except the
 * public endpoints.</p>
 */
@Configuration
public class WebSecurityConfiguration {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/**",
            "/api/v1/user-accounts/availability",
            // The notification provider calls this webhook from outside: it can never carry a user token
            "/api/v1/webhooks/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${iam.security.enforce-authentication:false}") boolean enforceAuthentication) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> {
                    requests.requestMatchers(PUBLIC_ENDPOINTS).permitAll();
                    if (enforceAuthentication) {
                        requests.anyRequest().authenticated();
                    } else {
                        requests.anyRequest().permitAll();
                    }
                })
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
