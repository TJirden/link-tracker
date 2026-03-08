package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.dto.AddLinkRequest;
import backend.academy.linktracker.bot.client.dto.LinkResponse;
import backend.academy.linktracker.bot.client.dto.ListLinksResponse;
import backend.academy.linktracker.bot.client.dto.RemoveLinkRequest;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {

    private final ScrapperClient scrapperClient;

    /**
     * Возвращает готовое сообщение со списком ссылок
     */
    public SendMessage getList(long chatId) {
        try {
            ListLinksResponse response = scrapperClient.getLinks(chatId).getBody();
            List<LinkResponse> links = response.links();

            if (links.isEmpty()) {
                return new SendMessage(chatId, "Список отслеживаемых ссылок пуст.");
            }

            StringBuilder sb = new StringBuilder("*Ваши ссылки:*\n\n");
            for (int i = 0; i < links.size(); i++) {
                sb.append(i + 1).append(". `").append(links.get(i).url()).append("`\n");
            }

            return new SendMessage(chatId, sb.toString()).parseMode(ParseMode.Markdown);

        } catch (Exception e) {
            log.error("Ошибка при получении списка ссылок для чата {}", chatId, e);
            return new SendMessage(chatId, "Не удалось получить список ссылок. Сервис временно недоступен.");
        }
    }

    /**
     * Пытается добавить ссылку и возвращает результат текстом
     */
    public SendMessage trackLink(long chatId, URI url) {
        try {
            scrapperClient.addLink(chatId, new AddLinkRequest(url, null));
            return new SendMessage(chatId, "Ссылка успешно добавлена в отслеживание!");
        } catch (Exception e) {
            log.error("Ошибка при добавлении ссылки {} для чата {}", url, chatId, e);
            // TODO: реализовать ответ на разные коды
            return new SendMessage(
                    chatId, "Не удалось добавить ссылку. Возможно, она уже отслеживается или сервис недоступен.");
        }
    }

    /**
     * Пытается удалить ссылку и возвращает результат текстом
     */
    public SendMessage untrackLink(long chatId, URI url) {
        try {
            scrapperClient.removeLink(chatId, new RemoveLinkRequest(url));
            return new SendMessage(chatId, "Ссылка удалена из отслеживания.");
        } catch (Exception e) {
            log.error("Ошибка при удалении ссылки {} для чата {}", url, chatId, e);
            return new SendMessage(chatId, "Не удалось удалить ссылку. Попробуйте позже.");
        }
    }
}
