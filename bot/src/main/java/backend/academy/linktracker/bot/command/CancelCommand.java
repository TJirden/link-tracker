package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.service.UserSessionService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelCommand implements DialogCommand, NonDialogCommand {

    private final UserSessionService userSessionService;

    @Override
    public String command() {
        return "cancel";
    }

    @Override
    public String description() {
        return "Возвращает бот в нейтральное состояние";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        userSessionService.clearSession(chatId);
        return new SendMessage(chatId, "Теперь все хорошо!");
    }
}
