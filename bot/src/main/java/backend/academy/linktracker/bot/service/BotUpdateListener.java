package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.command.DialogCommand;
import backend.academy.linktracker.bot.command.NonDialogCommand;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotUpdateListener implements UpdatesListener {

    private final TelegramBot bot;
    private final List<NonDialogCommand> nonDialogCommands;
    private final List<DialogCommand> dialogCommands;
    private final UpdateProcessor updateProcessor;
    private final UserSessionService sessionService;

    private BotCommand[] nonDialogBotCommands;
    private BotCommand[] dialogBotCommands;
    private boolean currentMenuIsDialog = false;

    @PostConstruct
    public void init() {
        nonDialogBotCommands = nonDialogCommands.stream()
                .map(cmd -> new BotCommand("/" + cmd.command(), cmd.description()))
                .toArray(BotCommand[]::new);

        dialogBotCommands = dialogCommands.stream()
                .map(cmd -> new BotCommand("/" + cmd.command(), cmd.description()))
                .toArray(BotCommand[]::new);

        bot.execute(new SetMyCommands(nonDialogBotCommands));
        bot.setUpdatesListener(this);
        log.info("Бот запущен. Non-dialog: {}, Dialog: {}", nonDialogCommands.size(), dialogCommands.size());
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            try {
                if (update.message() == null || update.message().text() == null) {
                    continue;
                }

                long chatId = update.message().chat().id();

                SendMessage message = updateProcessor.processUpdate(update);

                if (message != null) {
                    bot.execute(message);
                    log.info(
                            "Отправлен ответ пользователю: chatId={}",
                            message.getParameters().get("chat_id"));
                }

                updateMenu(sessionService.isWaitingForInput(chatId));
            } catch (Exception e) {
                log.error("Ошибка обработки обновления: updateId={}", update.updateId(), e);
            }
        }
        return CONFIRMED_UPDATES_ALL;
    }

    private void updateMenu(boolean inDialog) {
        if (inDialog == currentMenuIsDialog) {
            return;
        }

        BotCommand[] commands = inDialog ? dialogBotCommands : nonDialogBotCommands;
        bot.execute(new SetMyCommands(commands));
        currentMenuIsDialog = inDialog;
    }
}
