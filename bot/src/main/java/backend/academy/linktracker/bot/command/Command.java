package backend.academy.linktracker.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

/**
 * Контракт для любой slash-команды бота.
 */
public interface Command {

    /** Имя команды без "/" */
    String command();

    String description();

    /** Обработка входящего обновления */
    SendMessage handle(Update update);

    /** Проверяет, подходит ли данное обновление этой команде */
    default boolean supports(String text) {
        if (text == null) {
            return false;
        }

        return text.equals("/" + command())
                || text.startsWith("/" + command() + " ")
                || text.startsWith("/" + command() + "@");
    }
}
