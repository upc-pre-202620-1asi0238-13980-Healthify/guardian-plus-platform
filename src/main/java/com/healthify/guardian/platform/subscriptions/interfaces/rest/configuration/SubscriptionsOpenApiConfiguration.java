package com.healthify.guardian.platform.subscriptions.interfaces.rest.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Guardian+ Backend API",
                version = "v1",
                description = """
                        REST API for Guardian+.
                        Includes subscription lifecycle,
                        plan management and entitlement queries.
                        """
        )
)
public class SubscriptionsOpenApiConfiguration {
}