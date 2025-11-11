package com.example.tennis;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TennisApplication {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().addSecurityItem(new SecurityRequirement()
                        .addList("JWT Authentication"))
                .components(new Components().addSecuritySchemes
                        ("JWT Authentication", createAPIKeyScheme()))
                .info(new Info()
                        .title("Tennis Court Management API")
                        .version("1.0")
                        .description("API for managing tennis courts")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@company.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    public static void main(String[] args) {
		SpringApplication.run(TennisApplication.class, args);
	}

}
