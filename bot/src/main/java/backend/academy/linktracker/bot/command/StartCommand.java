package backend.academy.linktracker.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
public class StartCommand implements Command {

    @Override
    public String command() {
        return "start";
    }

    @Override
    public String description() {
        return "Старт!";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        String text = "Привет!";

        return new SendMessage(chatId, text);
    }
}
