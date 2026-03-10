package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.dto.AddLinkRequest;
import backend.academy.linktracker.bot.client.dto.LinkResponse;
import backend.academy.linktracker.bot.client.dto.ListLinksResponse;
import backend.academy.linktracker.bot.client.dto.RemoveLinkRequest;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
            if (response == null) {
                return new SendMessage(chatId, "Не пришел ответ от сервера, попробуйте еще раз.");
            }
            List<LinkResponse> links = response.links();

            if (links == null || links.isEmpty()) {
                return new SendMessage(chatId, "Список отслеживаемых ссылок пуст.");
            }

            StringBuilder sb = new StringBuilder("*Ваши ссылки:*\n\n");
            for (int i = 0; i < links.size(); i++) {
                LinkResponse link = links.get(i);
                sb.append(i + 1).append(". `").append(link.url()).append("`\n");

                if (link.tags() != null && !link.tags().isEmpty()) {
                    String tags = String.join(" ", link.tags());
                    sb.append("Теги: ").append(tags).append("\n");
                }
                sb.append("\n");
            }

            return new SendMessage(chatId, sb.toString()).parseMode(ParseMode.Markdown);

        } catch (Exception e) {
            log.error("Ошибка при получении списка ссылок для чата {}", chatId, e);
            return new SendMessage(chatId, "Не удалось получить список ссылок. Сервис временно недоступен.");
        }
    }

    /**
     * Пытается добавить ссылку с тегами и возвращает результат текстом
     */
    public SendMessage trackLink(long chatId, URI url, Set<String> tags) {
        try {
            scrapperClient.addLink(chatId, new AddLinkRequest(url, new ArrayList<>(tags)));

            StringBuilder message = new StringBuilder("Ссылка успешно добавлена в отслеживание!\n\n");
            message.append("`").append(url).append("`\n");

            if (tags != null && !tags.isEmpty()) {
                String tagsStr = String.join(" ", tags);
                message.append("Теги: ").append(tagsStr);
            } else {
                message.append("Теги не указаны");
            }

            return new SendMessage(chatId, message.toString()).parseMode(ParseMode.Markdown);

        } catch (Exception e) {
            log.error("Ошибка при добавлении ссылки {} для чата {}", url, chatId, e);
            // TODO: реализовать ответ на разные коды
            return new SendMessage(chatId, "Не удалось добавить ссылку. Сервис временно недоступен.");
        }
    }

    /**
     * Пытается удалить ссылку и возвращает результат текстом
     */
    public SendMessage untrackLink(long chatId, URI url) {
        try {
            scrapperClient.removeLink(chatId, new RemoveLinkRequest(url));

            return new SendMessage(chatId, "Ссылка удалена из отслеживания!\n" + "`" + url + "`")
                    .parseMode(ParseMode.Markdown);

        } catch (Exception e) {
            log.error("Ошибка при удалении ссылки {} для чата {}", url, chatId, e);
            return new SendMessage(chatId, "Не удалось удалить ссылку. Попробуйте позже.");
        }
    }
}
