package com.example.bank_start.bot;

import com.example.bank_start.dto.RecommendationDTO;
import com.example.bank_start.repository.TransactionRepository;
import com.example.bank_start.service.RecommendationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Telegram-бот для выдачи рекомендаций пользователям по имени.
 *
 * <p>Бот работает по простому протоколу:
 * <ul>
 *   <li>При первом обращении — приветствует пользователя и выводит справку.</li>
 *   <li>Команда {@code /recommend username} — ищет пользователя по имени
 *       и возвращает список рекомендаций.</li>
 * </ul>
 *
 * <h3>Логика работы:</h3>
 * <ol>
 *   <li>Бот ищет пользователя в таблице USERS по полю {@code FIRST_NAME} и {@code LAST_NAME}.</li>
 *   <li>Если пользователь найден один — вызывает {@link RecommendationService}
 *       и формирует отформатированный ответ.</li>
 *   <li>Если пользователь не найден или найдено несколько — выводит сообщение об ошибке.</li>
 * </ol>
 *
 * <h3>Формат ответа при нахождении пользователя:</h3>
 * <pre>
 * Здравствуйте <Имя и фамилия пользователя> (данные есть в базе).
 * Новые продукты для вас:
 * - Название продукта: описание
 * - ...
 * </pre>
 *
 * <h3>Формат ответа при ошибке:</h3>
 * <pre>
 * Пользователь не найден
 * </pre>
 */
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    private final TransactionRepository transactionRepository;
    private final RecommendationService recommendationService;

    /**
     * Создаёт бота с указанными сервисами.
     *
     * @param transactionRepository репозиторий для доступа к данным о транзакциях и пользователях
     * @param recommendationService сервис для формирования рекомендаций
     */
    public TelegramBot(TransactionRepository transactionRepository,
                       RecommendationService recommendationService) {
        this.transactionRepository = transactionRepository;
        this.recommendationService = recommendationService;
    }

    /**
     * Возвращает токен бота для подключения к Telegram API.
     *
     * @return токен бота
     */
    @Override
    public String getBotToken() {
        return botToken;
    }

    /**
     * Возвращает имя бота для подключения к Telegram API.
     *
     * @return имя бота
     */
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Обрабатывает входящие обновления от Telegram.
     *
     * <p>Проверяет тип обновления:
     * <ul>
     *   <li>Если это сообщение — вызывает {@link #handleMessage(org.telegram.telegrambots.meta.api.objects.Message}.</li>
     *   <li>Иначе — игнорирует.</li>
     * </ul>
     *
     * @param update входящее обновление от Telegram
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update.getMessage());
        }
    }

    /**
     * Обрабатывает входящее сообщение от пользователя.
     *
     * <p>Если команда {@code /recommend} — извлекает имя пользователя из аргумента,
     * ищет его в базе данных и возвращает рекомендации.
     *
     * <p>При первом сообщении (без команды) — выводит справку.
     *
     * @param message входящее сообщение
     */
    private void handleMessage(org.telegram.telegrambots.meta.api.objects.Message message) {
        long chatId = message.getChatId();
        String text = message.getText();

        if (text.startsWith("/recommend")) {
            String username = text.substring("/recommend".length()).trim();
            handleRecommendCommand(chatId, username);
        } else {
            handleStartCommand(chatId);
        }
    }

    /**
     * Обрабатывает команду {@code /recommend username}.
     *
     * <p>Ищет пользователя в базе данных по имени и фамилии.
     * Если найден один пользователь — формирует и отправляет рекомендации.
     * Если не найден или найдено несколько — отправляет сообщение об ошибке.
     *
     * @param chatId идентификатор чата
     * @param username имя и фамилия пользователя для поиска
     */
    private void handleRecommendCommand(long chatId, String username) {
        if (username.isBlank()) {
            sendMessage(chatId, "Пожалуйста, укажите имя и фамилию пользователя.\nПример: /recommend Иван Иванов");
            return;
        }

        // Search for user by name (FIRST_NAME + LAST_NAME)
        List<String> names = transactionRepository.getProductTypes(null);
        // We need to search users by name - let's use a raw SQL query via JdbcTemplate
        // Since we only have TransactionRepository, we'll search by checking if we can find users

        // Actually, we need to query USERS table directly. Let's use the main datasource.
        // We'll need to inject the main JdbcTemplate or use a different approach.
        // For now, let's use a workaround: we'll query the USERS table directly.

        UUID userId = findUserByName(username);

        if (userId == null) {
            sendMessage(chatId, "Пользователь не найден");
            return;
        }

        // Get recommendations for the user
        RecommendationDTO dto = recommendationService.evaluateRecommendations(userId);

        // Build response
        StringBuilder response = new StringBuilder();
        // Get user name from the database
        String userName = getUserNameById(userId);

        if (userName != null) {
            response.append("Здравствуйте ").append(userName).append(" (данные есть в базе).\n");
        } else {
            response.append("Здравствуйте (данные есть в базе).\n");
        }

        if (dto.recommendations().isEmpty()) {
            response.append("Новые продукты для вас:\nНет рекомендаций на данный момент.");
        } else {
            response.append("Новые продукты для вас:\n");
            for (var rec : dto.recommendations()) {
                response.append("- ").append(rec.name()).append(": ").append(rec.text()).append("\n");
            }
        }

        sendMessage(chatId, response.toString());
    }

    /**
     * Ищет пользователя в базе данных по имени и фамилии.
     *
     * <p>Выполняет SQL-запрос к таблице USERS, ища пользователя,
     * у которого FIRST_NAME и LAST_NAME содержат указанные значения.
     *
     * @param username имя и фамилия пользователя (например, "Иван Иванов")
     * @return UUID пользователя, если найден ровно один; null иначе
     */
    private UUID findUserByName(String username) {
        String[] parts = username.trim().split("\\s+");
        if (parts.length < 2) {
            return null;
        }

        String firstName = parts[0];
        String lastName = parts[1];

        // Use the main JdbcTemplate to query USERS table
        // We need to access the main datasource - let's use a workaround
        // by querying through the TransactionRepository's jdbcTemplate
        // Actually, we need a dedicated method. Let's add it to TransactionRepository.
        return transactionRepository.findUserByName(firstName, lastName);
    }

    /**
     * Получает полное имя пользователя по его идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return строка "FirstName LastName", если пользователь найден; null иначе
     */
    private String getUserNameById(UUID userId) {
        return transactionRepository.getUserNameById(userId);
    }

    /**
     * Отправляет сообщение в чат.
     *
     * @param chatId идентификатор чата
     * @param text текст сообщения
     */
    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Обрабатывает команду старта (первое обращение).
     *
     * <p>Приветствует пользователя и выводит справку по использованию бота.
     *
     * @param chatId идентификатор чата
     */
    private void handleStartCommand(long chatId) {
        String helpText = """
                Здравствуйте! Я бот для получения рекомендаций по продуктам банка.

                Доступные команды:
                /recommend <Имя Фамилия> — получить рекомендации для пользователя

                Пример:
                /recommend Иван Иванов""";
        sendMessage(chatId, helpText);
    }
}
