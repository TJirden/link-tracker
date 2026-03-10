package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.exception.ApiException;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StartCommand implements NonDialogCommand {

    private final ScrapperClient scrapperClient;

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
        try {
            scrapperClient.registerChat(chatId);
            return new SendMessage(chatId, "Привет!");
        } catch (ApiException e) {
            return new SendMessage(chatId, e.getApiErrorResponse().description());
        }
    }
}
