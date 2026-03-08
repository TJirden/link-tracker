package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.client.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.UpdateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BotController {

    private final UpdateHandler updateHandler;

    @PostMapping("/updates")
    public ResponseEntity<Void> sendUpdate(@RequestBody LinkUpdate update) {
        log.info("Получено обновление для ссылки: {}", update.url());

        try {
            updateHandler.handleUpdate(update);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Ошибка при обработке обновления: {}", update, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
