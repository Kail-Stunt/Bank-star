package com.example.bank_start.bot;

import com.example.bank_start.dto.RecommendationDTO;
import com.example.bank_start.dto.RecommendationDTO.RecommendationItem;
import com.example.bank_start.repository.UserRepository;
import com.example.bank_start.service.RecommendationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.UUID;

/**
 * Telegram-бот для выдачи рекомендаций пользователям по имени.
 *
 * <p>Бот работает по простому протоколу:
 * <ul>
 *   <li>При первом обращении приветствует пользователя и показывает справку.</li>
 *   <li>Команда {@code /recommend username} ищет пользователя по имени и фамилии
 *       в базе данных и возвращает рекомендации.</li>
 * </ul>
 *
 * <h3>Формат ответа:</h3>
 * <p>Если пользователь найден:
 * <pre>
 * Здравствуйте <Имя и фамилия пользователя> (данные есть в базе).
 * Новые продукты для вас:
 * - Название продукта 1
 * - Название продукта 2
 * </pre>
 *
 * <p>Если пользователь не найден или найдено несколько:
 * <pre>
 * Пользователь не найден
 * </pre>
 *
 * <h3>Конфигурация:</h3>
 * <p>Бот создаётся только если свойство {@code telegram.bot.enabled=true}.
 * Токен бота задаётся через свойство {@code telegram.bot.token}.
 */
@Component
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = false)
public class RecommendationBot extends TelegramLongPollingBot {

    private final UserRepository userRepository;
    private final RecommendationService recommendationService;
    private final String botToken;
    private final String botUsername;

    /**
     * Создаёт бот с указанными параметрами.
     *
     * @param userRepository репозиторий для поиска пользователей
     * @param recommendationService сервис для получения рекомендаций
     * @param botToken токен бота от BotFather
     * @param botUsername имя бота (без @)
     */
    public RecommendationBot(UserRepository userRepository,
                              RecommendationService recommendationService,
                              @Value("${telegram.bot.token}") String botToken,
                              @Value("${telegram.bot.username}") String botUsername) {
        this.userRepository = userRepository;
        this.recommendationService = recommendationService;
        this.botToken = botToken;
        this.botUsername = botUsername;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    /**
     * Обрабатывает входящие обновления от Telegram.
     *
     * <p>Поддерживает следующие команды:
     * <ul>
     *   <li>{@code /start} — приветствие и справка.</li>
     *   <li>{@code /recommend username} — поиск пользователя и выдача рекомендаций.</li>
 * </ul>
     *
     * @param update входящее обновление от Telegram
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText();

            if (text.startsWith("/recommend ")) {
                handleRecommend(chatId, text.substring("/recommend ".length()));
            } else if (text.equals("/start") || text.equals("/help")) {
                handleStart(chatId);
            }
        }
    }

    /**
     * Обрабатывает команду /recommend username.
     *
     * <p>Ищет пользователя в базе данных по имени и фамилии.
     * Если найден ровно один пользователь, возвращает рекомендации.
     * Если не найден или найдено несколько — выводит сообщение об ошибке.
     *
     * @param chatId идентификатор чата для ответа
     * @param username имя и фамилия пользователя для поиска
     */
    private void handleRecommend(String chatId, String username) {
        List<String> nameParts = List.of(username.trim().split("\\s+"));
        if (nameParts.size() < 2) {
            sendMessage(chatId, "Пожалуйста, введите имя и фамилию пользователя.\nПример: /recommend Иван Иванов");
            return;
        }

        String firstName = nameParts.get(0);
        String lastName = nameParts.get(1);

        // Find users by first and last name
        List<UUID> matchingUserIds = userRepository.findByFirstNameAndLastName(firstName, lastName);

        if (matchingUserIds.isEmpty()) {
            sendMessage(chatId, "Пользователь не найден");
        } else if (matchingUserIds.size() > 1) {
            sendMessage(chatId, "Пользователь не найден");
        } else {
            UUID userId = matchingUserIds.get(0);
            String userName = userRepository.getFullNameById(userId);
            if (userName == null || userName.isBlank()) {
                userName = firstName + " " + lastName;
            }

            // Get recommendations for the user
            RecommendationDTO dto = recommendationService.evaluateRecommendations(userId);
            List<RecommendationItem> recommendations = dto.recommendations();

            StringBuilder response = new StringBuilder();
            response.append("Здравствуйте ").append(userName).append(" (данные есть в базе).\n");
            response.append("Новые продукты для вас:\n");

            if (recommendations.isEmpty()) {
                response.append("Нет рекомендаций на данный момент.");
            } else {
                for (RecommendationItem rec : recommendations) {
                    response.append("- ").append(rec.name()).append("\n");
                }
            }

            sendMessage(chatId, response.toString());
        }
    }

    /**
     * Отправляет сообщение в Telegram.
     *
     * @param chatId идентификатор чата
     * @param text текст сообщения
     */
    private void sendMessage(String chatId, String text) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(text);
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Обрабатывает команду /start.
     *
     * <p>Приветствует пользователя и показывает справку по командам.
     *
     * @param chatId идентификатор чата
     */
    private void handleStart(String chatId) {
        String helpText = "Добро пожаловать! Я бот для получения рекомендаций по продуктам банка.\n\n" +
                "Доступные команды:\n" +
                "/recommend <Имя> <Фамилия> - получить рекомендации для пользователя\n" +
                "/help - показать эту справку";
        sendMessage(chatId, helpText);
    }
}
