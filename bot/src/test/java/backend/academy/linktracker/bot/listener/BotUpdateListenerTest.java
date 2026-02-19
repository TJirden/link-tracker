package backend.academy.linktracker.bot.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.command.Command;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BotUpdateListener Tests")
class BotUpdateListenerTest {

    @Mock
    private TelegramBot bot;

    @Test
    @DisplayName("Должен обрабатывать команду /start")
    void shouldProcessStartCommand() {

        Command startCommand = mock(Command.class);
        when(startCommand.command()).thenReturn("start");
        when(startCommand.supports(any())).thenReturn(true);
        when(startCommand.handle(any())).thenReturn(new SendMessage(123L, "Привет!"));

        BotUpdateListener listener = new BotUpdateListener(bot, List.of(startCommand));
        Update update = mockUpdate(123L, "/start");

        listener.process(List.of(update));

        verify(startCommand).handle(update);
        verify(bot).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Должен обрабатывать команду /help")
    void shouldProcessHelpCommand() {

        Command startCommand = mock(Command.class);
        when(startCommand.supports(any())).thenReturn(false);

        Command helpCommand = mock(Command.class);
        when(helpCommand.command()).thenReturn("help");
        when(helpCommand.supports(any())).thenReturn(true);
        when(helpCommand.handle(any())).thenReturn(new SendMessage(123L, "Помощь"));

        BotUpdateListener listener = new BotUpdateListener(bot, List.of(startCommand, helpCommand));
        Update update = mockUpdate(123L, "/help");

        listener.process(List.of(update));

        verify(helpCommand).handle(update);
        verify(bot).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Должен отвечать на неизвестную команду")
    void shouldHandleUnknownCommand() {

        Command startCommand = mock(Command.class);
        when(startCommand.supports(any())).thenReturn(false);

        BotUpdateListener listener = new BotUpdateListener(bot, List.of(startCommand));
        Update update = mockUpdate(123L, "/unknown");

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

        listener.process(List.of(update));

        verify(bot).execute(captor.capture());
        SendMessage sentMessage = captor.getValue();

        assertThat(sentMessage.getParameters().get("chat_id")).isEqualTo(123L);
        assertThat((String) sentMessage.getParameters().get("text")).containsIgnoringCase("неизвестная команда");
    }

    @Test
    @DisplayName("Должен отвечать на обычный текст")
    void shouldHandleNonCommandText() {

        Command command = mock(Command.class);
        when(command.supports(any())).thenReturn(false);

        BotUpdateListener listener = new BotUpdateListener(bot, List.of(command));
        Update update = mockUpdate(123L, "Просто текст");

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

        listener.process(List.of(update));

        verify(bot).execute(captor.capture());
        SendMessage sentMessage = captor.getValue();

        assertThat((String) sentMessage.getParameters().get("text")).containsIgnoringCase("команд");
    }

    @Test
    @DisplayName("Должен игнорировать update без message")
    void shouldIgnoreNullMessage() {

        BotUpdateListener listener = new BotUpdateListener(bot, List.of());
        Update update = mock(Update.class);
        when(update.message()).thenReturn(null);

        listener.process(List.of(update));

        verify(bot, never()).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Должен игнорировать message без текста")
    void shouldIgnoreNullText() {

        BotUpdateListener listener = new BotUpdateListener(bot, List.of());
        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(null);

        listener.process(List.of(update));

        verify(bot, never()).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Должен продолжать обработку при ошибке в одном update")
    void shouldContinueProcessingOnError() {

        Command command = mock(Command.class);
        when(command.command()).thenReturn("test");
        when(command.supports(any())).thenReturn(true);
        when(command.handle(any())).thenThrow(new RuntimeException("Ошибка")).thenReturn(new SendMessage(456L, "OK"));

        BotUpdateListener listener = new BotUpdateListener(bot, List.of(command));

        Update badUpdate = mockUpdate(123L, "/test");
        Update goodUpdate = mockUpdate(456L, "/test");

        listener.process(List.of(badUpdate, goodUpdate));

        verify(bot).execute(any(SendMessage.class));
    }

    private Update mockUpdate(long chatId, String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        lenient().when(update.message()).thenReturn(message);
        lenient().when(message.chat()).thenReturn(chat);
        lenient().when(message.text()).thenReturn(text);
        lenient().when(chat.id()).thenReturn(chatId);

        return update;
    }
}
