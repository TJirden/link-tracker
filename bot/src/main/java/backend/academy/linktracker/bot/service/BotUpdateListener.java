package backend.academy.linktracker.bot.service;

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
    private final UserSessionService sessionService;
    private final LinkProcessor linkProcessor;

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

        String text = update.message().text();
        long chatId = update.message().chat().id();
        SendMessage request = null;

        if (text.startsWith("/")) {
            if (sessionService.isWaitingForInput(chatId)) {
                log.info("Пользователь ввел команду во время диалога: chatId={}, command={}", chatId, text);
                sessionService.clearSession(chatId);
            }
            request = processCommand(update);
        } else {
            request = processLine(update);
        }

        if (request != null) {
            bot.execute(request);
            log.info("Отправлен ответ пользователю: chatId={}", chatId);
        }
    }

    private SendMessage processCommand(Update update) {
        long chatId = update.message().chat().id();
        String text = update.message().text();

        for (Command command : commands) {
            if (command.supports(text)) {
                log.info("Обработана команда: command=/{}, chatId={}, text={}", command.command(), chatId, text);
                return command.handle(update);
            }
        }

        log.warn("Получена неизвестная команда: chatId={}, text={}", chatId, text);
        return new SendMessage(chatId, "Неизвестная команда. Используй /help для списка команд.");
    }

    private SendMessage processLine(Update update) {
        long chatId = update.message().chat().id();
        String text = update.message().text();

        if (sessionService.isWaitingForInput(chatId)) {
            log.info("Обработка ввода в рамках диалога: chatId={}, text={}", chatId, text);
            return linkProcessor.process(update);
        }

        log.info("Получено сообщение без команды: chatId={}, text={}", chatId, text);
        return new SendMessage(chatId, "Я понимаю только команды. Введи /help для справки.");
    }
}
