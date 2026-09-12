package com.micropymes.backend.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("Micropymes Commercial Tracking API")
                                .description(
                                        """
                                        REST API for managing customers,
                                        commercial opportunities, quotes,
                                        follow-ups and sales activity.
                                        """
                                )
                                .version("1.0.0")
                                .contact(
                                        new Contact()
                                                .name("Micropymes")
                                )
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "sessionAuth",
                                        new SecurityScheme()
                                                .type(
                                                        SecurityScheme.Type.APIKEY
                                                )
                                                .in(
                                                        SecurityScheme.In.COOKIE
                                                )
                                                .name("JSESSIONID")
                                )
                );
    }
}