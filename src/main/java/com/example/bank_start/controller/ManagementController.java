package com.example.bank_start.controller;

import com.example.bank_start.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST-контроллер для административных операций управления сервисом.
 *
 * <p>Предоставляет эндпоинты для:
 * <ul>
 *   <li>Очистки всех кешей ({@code POST /management/clear-caches}).</li>
 *   <li>Получения информации о сервисе ({@code GET /management/info}).</li>
 * </ul>
 *
 * <h3>API Endpoints:</h3>
 * <table border="1">
 *   <tr>
 *     <th>Метод</th>
 *     <th>Путь</th>
 *     <th>Описание</th>
 *     <th>Ответ</th>
 *   </tr>
 *   <tr>
 *     <td>POST</td>
 *     <td>/management/clear-caches</td>
 *     <td>Очистка всех кешей</td>
 *     <td>200 OK</td>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>/management/info</td>
 *     <td>Информация о сервисе</td>
 *     <td>200 OK с name и version</td>
 *   </tr>
 * </table>
 */
@RestController
@RequestMapping("/management")
public class ManagementController {

    private final TransactionRepository transactionRepository;
    private final String serviceName;
    private final String serviceVersion;

    /**
     * Создаёт контроллер с указанными зависимостями.
     *
     * @param transactionRepository репозиторий транзакций (для очистки кешей)
     * @param serviceName название сервиса из конфигурации
     * @param serviceVersion версия сервиса из файла build.gradle
     */
    public ManagementController(TransactionRepository transactionRepository,
                                 @Value("${spring.application.name}") String serviceName,
                                 @Value("${spring.application.version:0.0.1-SNAPSHOT}") String serviceVersion) {
        this.transactionRepository = transactionRepository;
        this.serviceName = serviceName;
        this.serviceVersion = serviceVersion;
    }

    /**
     * Очищает все кеші репозитория транзакций.
     *
     * <p>Вызывает {@link TransactionRepository#clearAllCaches()},
     * который инвалидирует все кеші Caffeine:
     * <ul>
     *   <li>productTypeCache — кеш проверки наличия продуктов.</li>
     *   <li>transactionSumCache — кеш сумм транзакций.</li>
     *   <li>transactionCountCache — кеш подсчёта транзакций.</li>
 * </ul>
     *
     * <h3>Ответ:</h3>
     * <p>200 OK с сообщением об успешной очистке.
     *
     * @return ResponseEntity со статусом 200 OK
     */
    @PostMapping("/clear-caches")
    public ResponseEntity<Map<String, String>> clearCaches() {
        transactionRepository.clearAllCaches();
        return ResponseEntity.ok(Map.of("message", "Caches cleared successfully"));
    }

    /**
     * Возвращает информацию о сервисе.
     *
     * <p>Используется для мониторинга и отображения сервиса
     * в общем списке сервисов банка.
     *
     * <h3>Ответ:</h3>
     * <pre>
     * {
     *   "name": "bank-start",
     *   "version": "0.0.1-SNAPSHOT"
     * }
     * </pre>
     *
     * @return ResponseEntity с информацией о сервисе и статусом 200 OK
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getInfo() {
        return ResponseEntity.ok(Map.of(
                "name", serviceName,
                "version", serviceVersion
        ));
    }
}
