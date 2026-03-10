package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.client.dto.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateHandler {

    private final TelegramBot telegramBot;

    public void handleUpdate(LinkUpdate update) {
        String text = String.format("Обновление ссылки\n%s\n%s", update.url(), update.description());

        for (long chatId : update.tgChatIds()) {
            try {
                SendMessage message = new SendMessage(chatId, text)
                        .parseMode(com.pengrad.telegrambot.model.request.ParseMode.Markdown);

                telegramBot.execute(message);
                log.info("Уведомление отправлено пользователю {}", chatId);
            } catch (Exception e) {
                log.error("Ошибка при отправке уведомления пользователю {}: {}", chatId, e.getMessage());
            }
        }
    }
}
