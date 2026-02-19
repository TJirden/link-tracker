package backend.academy.linktracker.bot.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Command Tests")
class CommandTest {

    @Nested
    @DisplayName("StartCommand")
    class StartCommandTest {

        private final StartCommand command = new StartCommand();

        @Test
        @DisplayName("Должна возвращать правильное имя команды")
        void shouldReturnCorrectCommandName() {
            assertThat(command.command()).isEqualTo("start");
        }

        @Test
        @DisplayName("Должна возвращать описание")
        void shouldReturnCorrectDescription() {
            assertThat(command.description()).isNotBlank();
        }

        @Test
        @DisplayName("Должна обрабатывать команду /start с приветствием")
        void shouldHandleStartCommand() {
            Update update = mockUpdate(123L, "Иван", "/start");

            SendMessage response = command.handle(update);

            assertThat(response.getParameters().get("chat_id")).isEqualTo(123L);
            assertThat((String) response.getParameters().get("text")).contains("Привет");
        }

        @Test
        @DisplayName("Должна поддерживать команду /start")
        void shouldSupportStartCommand() {
            Update update = mockUpdate(123L, "User", "/start");
            assertThat(command.supports(update)).isTrue();
        }

        @Test
        @DisplayName("Должна поддерживать команду /start с упоминанием бота")
        void shouldSupportStartCommandWithMention() {
            Update update = mockUpdate(123L, "User", "/start@mybot");
            assertThat(command.supports(update)).isTrue();
        }

        @Test
        @DisplayName("Не должна поддерживать другие команды")
        void shouldNotSupportOtherCommands() {
            Update update = mockUpdate(123L, "User", "/help");
            assertThat(command.supports(update)).isFalse();
        }

        @Test
        @DisplayName("Не должна поддерживать null message")
        void shouldNotSupportNullMessage() {
            Update update = mock(Update.class);
            when(update.message()).thenReturn(null);
            assertThat(command.supports(update)).isFalse();
        }
    }

    @Nested
    @DisplayName("HelpCommand")
    class HelpCommandTest {

        @Test
        @DisplayName("Должна возвращать правильное имя команды")
        void shouldReturnCorrectCommandName() {
            HelpCommand command = new HelpCommand(List.of());
            assertThat(command.command()).isEqualTo("help");
        }

        @Test
        @DisplayName("Должна возвращать описание")
        void shouldReturnCorrectDescription() {
            HelpCommand command = new HelpCommand(List.of());
            assertThat(command.description()).isNotBlank();
        }

        @Test
        @DisplayName("Должна выводить список всех доступных команд")
        void shouldListAllAvailableCommands() {
            Command startCmd = mock(Command.class);
            when(startCmd.command()).thenReturn("start");
            when(startCmd.description()).thenReturn("Начать работу");

            Command helpCmd = mock(Command.class);
            when(helpCmd.command()).thenReturn("help");
            when(helpCmd.description()).thenReturn("Показать помощь");

            HelpCommand command = new HelpCommand(List.of(startCmd, helpCmd));
            Update update = mockUpdateSimple(123L, "/help");

            SendMessage response = command.handle(update);

            String text = (String) response.getParameters().get("text");
            assertThat(text)
                    .contains("/start")
                    .contains("Начать работу")
                    .contains("/help")
                    .contains("Показать помощь");
        }

        @Test
        @DisplayName("Должна поддерживать команду /help")
        void shouldSupportHelpCommand() {
            HelpCommand command = new HelpCommand(List.of());
            Update update = mockUpdateSimple(123L, "/help");
            assertThat(command.supports(update)).isTrue();
        }

        @Test
        @DisplayName("Не должна поддерживать другие команды")
        void shouldNotSupportOtherCommands() {
            HelpCommand command = new HelpCommand(List.of());
            Update update = mockUpdateSimple(123L, "/start");
            assertThat(command.supports(update)).isFalse();
        }
    }

    @Nested
    @DisplayName("GolCommand")
    class GolCommandTest {

        private final GolCommand command = new GolCommand();

        @Test
        @DisplayName("Должна возвращать правильное имя команды")
        void shouldReturnCorrectCommandName() {
            assertThat(command.command()).isEqualTo("gol");
        }

        @Test
        @DisplayName("Должна возвращать описание")
        void shouldReturnCorrectDescription() {
            assertThat(command.description()).isNotBlank();
        }

        @Test
        @DisplayName("Должна возвращать случайную фразу")
        void shouldReturnRandomPhrase() {
            Update update = mockUpdateSimple(123L, "/gol");

            SendMessage response = command.handle(update);

            assertThat(response.getParameters().get("chat_id")).isEqualTo(123L);
            assertThat((String) response.getParameters().get("text")).isNotBlank();
        }

        @Test
        @DisplayName("Должна поддерживать команду /gol")
        void shouldSupportGolCommand() {
            Update update = mockUpdateSimple(123L, "/gol");
            assertThat(command.supports(update)).isTrue();
        }

        @Test
        @DisplayName("Не должна поддерживать другие команды")
        void shouldNotSupportOtherCommands() {
            Update update = mockUpdateSimple(123L, "/start");
            assertThat(command.supports(update)).isFalse();
        }
    }

    private static Update mockUpdate(long chatId, String firstName, String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        User user = mock(User.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(message.from()).thenReturn(user);
        when(message.text()).thenReturn(text);
        when(chat.id()).thenReturn(chatId);
        when(user.firstName()).thenReturn(firstName);

        return update;
    }

    private static Update mockUpdateSimple(long chatId, String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn(text);
        when(chat.id()).thenReturn(chatId);

        return update;
    }
}
