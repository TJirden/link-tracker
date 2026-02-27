package backend.academy.linktracker.bot.service;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
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

        UserState state = sessionService.getState(chatId);

        return switch (state) {
            case WAITING_FOR_TRACK_LINK -> processTrackLink(chatId, text);
            case WAITING_FOR_TAGS -> processTags(chatId, text);
            case WAITING_FOR_UNTRACK_LINK -> processUntrackLink(chatId, text);
            default -> {
                log.warn("Пользователь {} попал в неизвестное состояние {}", chatId, state);
                sessionService.clearSession(chatId);
                yield new SendMessage(chatId, "Произошла ошибка состояния. Попробуйте начать заново через /help.");
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

            sessionService.saveTempLink(chatId, text);

            sessionService.setState(chatId, UserState.WAITING_FOR_TAGS);

            return new SendMessage(
                    chatId,
                    "Ссылка принята! \nТеперь отправьте __теги__ (через запятую/пробел) или напишите /cancel для отмены.");

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

        // TODO: сделать поддержку тегов)
        SendMessage response = linkService.trackLink(chatId, uri);

        sessionService.clearSession(chatId);

        return response;
    }

    private SendMessage processUntrackLink(long chatId, String text) {
        try {
            URI uri = URI.create(text);

            SendMessage response = linkService.untrackLink(chatId, uri);

            sessionService.clearSession(chatId);

            return response;

        } catch (IllegalArgumentException e) {
            return new SendMessage(chatId, "Некорректный формат ссылки. Попробуйте еще раз.");
        }
    }
}
