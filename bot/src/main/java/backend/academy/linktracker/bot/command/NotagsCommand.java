package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.service.LinkProcessor;
import backend.academy.linktracker.bot.service.UserSessionService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotagsCommand implements DialogCommand {

    private final LinkProcessor linkProcessor;

    private final UserSessionService userSessionService;

    @Override
    public String command() {
        return "notags";
    }

    @Override
    public String description() {
        return "Без тегов";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        return linkProcessor.withoutTags(chatId);
    }
}
