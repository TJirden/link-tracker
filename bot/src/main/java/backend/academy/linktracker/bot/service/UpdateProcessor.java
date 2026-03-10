package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.command.DialogCommand;
import backend.academy.linktracker.bot.command.NonDialogCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateProcessor {
    private final List<NonDialogCommand> nonDialogCommands;
    private final List<DialogCommand> dialogCommands;
    private final UserSessionService sessionService;
    private final LinkProcessor linkProcessor;

    public SendMessage processUpdate(Update update) {
        long chatId = update.message().chat().id();
        String text = update.message().text();
        if (sessionService.isWaitingForInput(chatId)) {
            log.info("Обработка ввода в рамках диалога: chatId={}, text={}", chatId, text);
            return processOtherInput(update);
        } else {
            return processCommand(update);
        }
    }

    private SendMessage processOtherInput(Update update) {
        long chatId = update.message().chat().id();
        String text = update.message().text();

        for (DialogCommand command : dialogCommands) {
            if (command.supports(text)) {
                log.info(
                        "Обработана команда в диалоговом режиме: command=/{}, chatId={}, text={}",
                        command.command(),
                        chatId,
                        text);
                return command.handle(update);
            }
        }

        return linkProcessor.process(update);
    }

    private SendMessage processCommand(Update update) {
        long chatId = update.message().chat().id();
        String text = update.message().text();

        for (NonDialogCommand command : nonDialogCommands) {
            if (command.supports(text)) {
                log.info("Обработана команда: command=/{}, chatId={}, text={}", command.command(), chatId, text);
                return command.handle(update);
            }
        }

        log.warn("Получена неизвестная команда: chatId={}, text={}", chatId, text);
        return new SendMessage(chatId, "Неизвестная команда. Используй /help для списка команд.");
    }
}
