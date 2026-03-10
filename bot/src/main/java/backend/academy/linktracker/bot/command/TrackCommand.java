package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.service.UserSessionService;
import backend.academy.linktracker.bot.service.UserState;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrackCommand implements NonDialogCommand {

    private final UserSessionService sessionService;

    @Override
    public String command() {
        return "track";
    }

    @Override
    public String description() {
        return "Ввести ссылку на отслед";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();

        sessionService.setState(chatId, UserState.WAITING_FOR_TRACK_LINK);

        return new SendMessage(chatId, "Пожалуйста, отправьте ссылку, которую вы хотите отслеживать.");
    }
}
