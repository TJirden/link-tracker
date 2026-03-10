package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.service.LinkService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListCommand implements NonDialogCommand {

    private final LinkService linkService;

    @Override
    public String command() {
        return "list";
    }

    @Override
    public String description() {
        return "Выводит список ссылок";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();

        return linkService.getList(chatId);
    }
}
