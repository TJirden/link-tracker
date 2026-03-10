package backend.academy.linktracker.bot.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.service.LinkService;
import backend.academy.linktracker.bot.service.UserSessionService;
import backend.academy.linktracker.bot.service.UserState;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NonDialogCommand Tests")
class NonDialogCommandTest {

    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private LinkService linkService;

    @Mock
    private UserSessionService sessionService;

    @Nested
    @DisplayName("StartCommand")
    class StartCommandTest {

        private StartCommand command;

        @BeforeEach
        void setUp() {
            command = new StartCommand(scrapperClient);
        }

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
        @DisplayName("Должна успешно регистрировать чат и возвращать приветствие")
        void shouldRegisterChatAndReturnGreeting() {
            // given
            long chatId = 123L;
            Update update = createMockUpdate(chatId, "/start");
            when(scrapperClient.registerChat(chatId))
                    .thenReturn(ResponseEntity.ok().build());

            // when
            SendMessage response = command.handle(update);

            // then
            verify(scrapperClient).registerChat(chatId);
            assertThat(response.getParameters().get("chat_id")).isEqualTo(chatId);
            assertThat((String) response.getParameters().get("text")).contains("Привет");
        }

        @Test
        @DisplayName("Должна поддерживать команду /start")
        void shouldSupportStartCommand() {
            assertThat(command.supports("/start")).isTrue();
        }
    }

    @Nested
    @DisplayName("HelpCommand")
    class HelpCommandTest {

        private HelpCommand command;

        @BeforeEach
        void setUp() {
            NonDialogCommand mockCommand1 = mock(NonDialogCommand.class);
            when(mockCommand1.command()).thenReturn("start");
            when(mockCommand1.description()).thenReturn("Начать работу");

            NonDialogCommand mockCommand2 = mock(NonDialogCommand.class);
            when(mockCommand2.command()).thenReturn("help");
            when(mockCommand2.description()).thenReturn("Показать помощь");

            command = new HelpCommand(List.of(mockCommand1, mockCommand2));
        }

        @Test
        @DisplayName("Должна возвращать правильное имя команды")
        void shouldReturnCorrectCommandName() {
            assertThat(command.command()).isEqualTo("help");
        }

        @Test
        @DisplayName("Должна возвращать описание")
        void shouldReturnCorrectDescription() {
            assertThat(command.description()).isNotBlank();
        }

        @Test
        @DisplayName("Должна выводить список всех доступных команд")
        void shouldListAllAvailableCommands() {
            // given
            long chatId = 123L;
            Update update = createMockUpdate(chatId, "/help");

            // when
            SendMessage response = command.handle(update);

            // then
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
            assertThat(command.supports("/help")).isTrue();
        }
    }

    @Nested
    @DisplayName("ListCommand")
    class ListCommandTest {

        private ListCommand command;

        @BeforeEach
        void setUp() {
            command = new ListCommand(linkService);
        }

        @Test
        @DisplayName("Должна возвращать правильное имя команды")
        void shouldReturnCorrectCommandName() {
            assertThat(command.command()).isEqualTo("list");
        }

        @Test
        @DisplayName("Должна возвращать описание")
        void shouldReturnCorrectDescription() {
            assertThat(command.description()).isNotBlank();
        }

        @Test
        @DisplayName("Должна вызывать сервис для получения списка ссылок")
        void shouldCallLinkService() {
            // given
            long chatId = 123L;
            Update update = createMockUpdate(chatId, "/list");
            SendMessage expectedResponse = new SendMessage(chatId, "Список ссылок");
            when(linkService.getList(chatId)).thenReturn(expectedResponse);

            // when
            SendMessage response = command.handle(update);

            // then
            verify(linkService).getList(chatId);
            assertThat(response).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("Должна поддерживать команду /list")
        void shouldSupportListCommand() {
            assertThat(command.supports("/list")).isTrue();
        }
    }

    @Nested
    @DisplayName("TrackCommand")
    class TrackCommandTest {

        private TrackCommand command;

        @BeforeEach
        void setUp() {
            command = new TrackCommand(sessionService);
        }

        @Test
        @DisplayName("Должна возвращать правильное имя команды")
        void shouldReturnCorrectCommandName() {
            assertThat(command.command()).isEqualTo("track");
        }

        @Test
        @DisplayName("Должна возвращать описание")
        void shouldReturnCorrectDescription() {
            assertThat(command.description()).isNotBlank();
        }

        @Test
        @DisplayName("Должна устанавливать состояние WAITING_FOR_TRACK_LINK")
        void shouldSetWaitingForTrackLinkState() {
            // given
            long chatId = 123L;
            Update update = createMockUpdate(chatId, "/track");

            // when
            SendMessage response = command.handle(update);

            // then
            verify(sessionService).setState(chatId, UserState.WAITING_FOR_TRACK_LINK);
            assertThat(response.getParameters().get("chat_id")).isEqualTo(chatId);
            assertThat((String) response.getParameters().get("text")).contains("ссылку, которую вы хотите отслеживать");
        }

        @Test
        @DisplayName("Должна поддерживать команду /track")
        void shouldSupportTrackCommand() {
            assertThat(command.supports("/track")).isTrue();
        }
    }

    @Nested
    @DisplayName("UntrackCommand")
    class UntrackCommandTest {

        private UntrackCommand command;

        @BeforeEach
        void setUp() {
            command = new UntrackCommand(sessionService);
        }

        @Test
        @DisplayName("Должна возвращать правильное имя команды")
        void shouldReturnCorrectCommandName() {
            assertThat(command.command()).isEqualTo("untrack");
        }

        @Test
        @DisplayName("Должна возвращать описание")
        void shouldReturnCorrectDescription() {
            assertThat(command.description()).isNotBlank();
        }

        @Test
        @DisplayName("Должна устанавливать состояние WAITING_FOR_UNTRACK_LINK")
        void shouldSetWaitingForUntrackLinkState() {
            // given
            long chatId = 123L;
            Update update = createMockUpdate(chatId, "/untrack");

            // when
            SendMessage response = command.handle(update);

            // then
            verify(sessionService).setState(chatId, UserState.WAITING_FOR_UNTRACK_LINK);
            assertThat(response.getParameters().get("chat_id")).isEqualTo(chatId);
            assertThat((String) response.getParameters().get("text"))
                    .contains("ссылку, которую вы хотите ототслеживать");
        }

        @Test
        @DisplayName("Должна поддерживать команду /untrack")
        void shouldSupportUntrackCommand() {
            assertThat(command.supports("/untrack")).isTrue();
        }
    }

    private Update createMockUpdate(long chatId, String text) {
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
