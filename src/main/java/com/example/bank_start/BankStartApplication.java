package com.example.bank_start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа в Spring Boot приложение банка.
 *
 * <p>Приложение предоставляет REST API для:
 * <ul>
 *   <li>Формирования рекомендаций пользователей на основе транзакций.</li>
 *   <li>Управления динамическими правилами рекомендаций.</li>
 *   <li>Получения статистики срабатываний правил.</li>
 *   <li>Интеграции с Telegram-ботом для выдачи рекомендаций по имени.</li>
 * </ul>
 *
 * <h3>Архитектура:</h3>
 * <p>Проект использует две встроенные базы данных H2:
 * <ul>
 *   <li>{@code bankdb} — данные о продуктах, транзакциях и пользователях.</li>
 *   <li>{@code rulesdb} — динамические правила и статистика их срабатываний.</li>
 * </ul>
 *
 * <h3>Основные эндпоинты:</h3>
 * <ul>
 *   <li>{@code GET /recommendation/{user_id}} — рекомендации пользователя.</li>
 *   <li>{@code POST /rule} — создание правила.</li>
 *   <li>{@code GET /rule} — список правил.</li>
 *   <li>{@code GET /rule/stats} — статистика срабатываний.</li>
 *   <li>{@code DELETE /rule/{product_id}} — удаление правила.</li>
 *   <li>{@code POST /management/clear-caches} — очистка кешей.</li>
 *   <li>{@code GET /management/info} — информация о сервисе.</li>
 * </ul>
 */
@SpringBootApplication
public class BankStartApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankStartApplication.class, args);
	}

}
