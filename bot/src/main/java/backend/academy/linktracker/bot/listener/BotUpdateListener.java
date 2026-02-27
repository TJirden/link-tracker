package backend.academy.linktracker.bot.listener;

import backend.academy.linktracker.bot.command.Command;
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
    private final List<Command> commands;

    @PostConstruct
    public void init() {
        BotCommand[] botCommands = commands.stream()
                .map(cmd -> new BotCommand("/" + cmd.command(), cmd.description()))
                .toArray(BotCommand[]::new);

        bot.execute(new SetMyCommands(botCommands));

        bot.setUpdatesListener(this);

        log.info("Бот запущен. Количество зарегистрированных команд: {}", commands.size());
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            try {
                processUpdate(update);
            } catch (Exception e) {
                log.error("Ошибка обработки обновления: updateId={}", update.updateId(), e);
            }
        }
        return CONFIRMED_UPDATES_ALL;
    }

    private void processUpdate(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return;
        }

        long chatId = update.message().chat().id();
        String text = update.message().text();

        for (Command command : commands) {
            if (command.supports(update)) {
                SendMessage response = command.handle(update);
                bot.execute(response);
                log.info("Обработана команда: command=/{}, chatId={}, text={}", command.command(), chatId, text);
                return;
            }
        }

        if (text.startsWith("/")) {
            bot.execute(new SendMessage(chatId, "Неизвестная команда. Используй /help для списка команд."));
            log.warn("Получена неизвестная команда: chatId={}, text={}", chatId, text);
        } else {
            bot.execute(new SendMessage(chatId, "Я понимаю только команды. Введи /help для справки."));
            log.info("Получено сообщение без команды: chatId={}, text={}", chatId, text);
        }
    }
}
