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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BotUpdateListener implements UpdatesListener {

    private final TelegramBot bot;
    private final List<Command> commands;

    public BotUpdateListener(TelegramBot bot, List<Command> commands) {
        this.bot = bot;
        this.commands = commands;
    }

    @PostConstruct
    public void init() {
        BotCommand[] botCommands = commands.stream()
                .map(cmd -> new BotCommand("/" + cmd.command(), cmd.description()))
                .toArray(BotCommand[]::new);

        bot.execute(new SetMyCommands(botCommands));

        bot.setUpdatesListener(this);

        log.info("Бот запущен. Зарегистрировано команд: {}", commands.size());
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            try {
                processUpdate(update);
            } catch (Exception e) {
                log.error("Ошибка обработки", e);
            }
        }
        return CONFIRMED_UPDATES_ALL;
    }

    private void processUpdate(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return;
        }

        for (Command command : commands) {
            if (command.supports(update)) {
                SendMessage response = command.handle(update);
                bot.execute(response);
                log.info("Обработана команда /{}", command.command());
                return;
            }
        }

        long chatId = update.message().chat().id();
        String text = update.message().text();
        if (text.startsWith("/")) {
            bot.execute(new SendMessage(chatId, "Неизвестная команда. Используй /help для списка команд."));
        } else {
            bot.execute(new SendMessage(chatId, "Я понимаю только команды. Введи /help для справки."));
        }
    }
}
