package com.example.bank_start.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI (Swagger).
 *
 * <p>Определяет метаданные API, которые отображаются в Swagger UI.
 * Доступно по адресу {@code http://localhost:8080/swagger-ui.html}.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Создаёт OpenAPI-документ с метаданными сервиса.
     *
     * @return OpenAPI-конфигурация
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearer-key";

        return new OpenAPI()
                .info(new Info()
                        .title("Bank Start API")
                        .version("0.0.1")
                        .description("API для управления рекомендациями продуктов банка. " +
                                "Поддерживает динамические правила рекомендаций, статистику " +
                                "срабатываний и интеграцию с Telegram-ботом.")
                        .contact(new Contact()
                                .name("Bank Start Team")
                                .email("support@bank-start.example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
