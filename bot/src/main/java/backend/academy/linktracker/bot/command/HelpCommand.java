package backend.academy.linktracker.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HelpCommand implements NonDialogCommand {

    private final List<NonDialogCommand> commands;

    @Override
    public String command() {
        return "help";
    }

    @Override
    public String description() {
        return "Показать список доступных команд";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();

        StringBuilder sb = new StringBuilder("*Доступные команды:*\n\n");

        for (NonDialogCommand cmd : commands) {
            sb.append(String.format("/%s — %s%n", cmd.command(), cmd.description()));
        }

        return new SendMessage(chatId, sb.toString()).parseMode(ParseMode.Markdown);
    }
}
