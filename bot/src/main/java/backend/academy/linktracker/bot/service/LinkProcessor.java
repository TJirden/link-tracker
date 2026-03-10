package backend.academy.linktracker.bot.service;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkProcessor {

    private final UserSessionService sessionService;
    private final LinkService linkService;

    /**
     * Главный метод обработки текстового ввода
     */
    public SendMessage process(Update update) {
        long chatId = update.message().chat().id();
        String text = update.message().text();

        if (text.startsWith("/")) {
            log.info("Получено команда в диалоге chatId={}, text={}", chatId, text);
            return new SendMessage(chatId, "Не жду команды Введи /cancel для отмены ввода.");
        }

        UserState state = sessionService.getState(chatId);

        return switch (state) {
            case WAITING_FOR_TRACK_LINK -> processTrackLink(chatId, text);
            case WAITING_FOR_TAGS -> processTags(chatId, text);
            case WAITING_FOR_UNTRACK_LINK -> processUntrackLink(chatId, text);
            default -> {
                log.warn("Пользователь {} попал в неизвестное состояние {}", chatId, state);
                sessionService.clearSession(chatId);
                yield new SendMessage(chatId, "Произошла ошибка состояния. Попробуйте начать заново.");
            }
        };
    }

    private SendMessage processTrackLink(long chatId, String text) {
        try {
            URI uri = URI.create(text);
            if (!uri.isAbsolute()) {
                return new SendMessage(
                        chatId, "Это не похоже на ссылку. Укажите полный URL (например, https://github.com/...).");
            }

            String host = uri.getHost();
            if (host == null || (!host.contains("github.com") && !host.contains("stackoverflow.com"))) {
                return new SendMessage(
                        chatId,
                        "Поддерживаются только ссылки на GitHub и StackOverflow.\n"
                                + "Попробуйте еще раз или введите /cancel для отмены.");
            }

            sessionService.saveTempLink(chatId, text);
            sessionService.setState(chatId, UserState.WAITING_FOR_TAGS);

            return new SendMessage(chatId, """
                    Ссылка принята!
                    Теперь отправьте **теги** через запятую.
                    Например: `java, spring, boot`
                    Или используйте /notags, если теги не нужны.
                    Для отмены введите /cancel""");

        } catch (IllegalArgumentException e) {
            return new SendMessage(chatId, "Некорректный формат ссылки. Попробуйте еще раз.");
        }
    }

    private SendMessage processTags(long chatId, String text) {
        String urlString = sessionService.getTempLink(chatId);

        if (urlString == null) {
            sessionService.clearSession(chatId);
            return new SendMessage(chatId, "Ошибка сессии: ссылка потеряна. Начните заново с /track.");
        }

        URI uri = URI.create(urlString);
        Set<String> tags = parseTags(text);

        SendMessage response = linkService.trackLink(chatId, uri, tags);
        sessionService.clearSession(chatId);

        return response;
    }

    private SendMessage processUntrackLink(long chatId, String text) {
        try {
            URI uri = URI.create(text);

            if (!uri.isAbsolute()) {
                return new SendMessage(
                        chatId, "Это не похоже на ссылку. Укажите полный URL (например, https://github.com/...).");
            }

            SendMessage response = linkService.untrackLink(chatId, uri);
            sessionService.clearSession(chatId);

            return response;

        } catch (IllegalArgumentException e) {
            return new SendMessage(chatId, "Некорректный формат ссылки. Попробуйте еще раз.");
        }
    }

    /**
     * Парсит теги из текста
     * Разделители: запятая
     */
    private Set<String> parseTags(String text) {
        if (text == null || text.trim().isEmpty() || text.equalsIgnoreCase("/notags")) {
            return new HashSet<>();
        }

        String[] parts = text.split(",");

        return Arrays.stream(parts)
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public SendMessage withoutTags(long chatId) {
        return processTags(chatId, null);
    }
}
