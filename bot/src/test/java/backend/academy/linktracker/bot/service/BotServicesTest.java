package backend.academy.linktracker.bot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.dto.LinkResponse;
import backend.academy.linktracker.bot.client.dto.LinkUpdate;
import backend.academy.linktracker.bot.client.dto.ListLinksResponse;
import backend.academy.linktracker.bot.client.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.command.TrackCommand;
import backend.academy.linktracker.bot.command.UntrackCommand;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервисов бота")
class BotServicesTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private UserSessionService sessionService;

    @Captor
    private ArgumentCaptor<SendMessage> messageCaptor;

    private LinkProcessor linkProcessor;
    private LinkService linkService;
    private UpdateHandler updateHandler;
    private BotUpdateListener botUpdateListener;
    private TrackCommand trackCommand;
    private UntrackCommand untrackCommand;

    @BeforeEach
    void setUp() {
        linkService = new LinkService(scrapperClient);
        linkProcessor = new LinkProcessor(sessionService, linkService);
        updateHandler = new UpdateHandler(telegramBot);
        trackCommand = new TrackCommand(sessionService);
        untrackCommand = new UntrackCommand(sessionService);

        List<Command> commands = List.of(trackCommand, untrackCommand);
        botUpdateListener = new BotUpdateListener(telegramBot, commands, sessionService, linkProcessor);
    }

    @Test
    @DisplayName("Должен обрабатывать команду /track")
    void shouldHandleTrackCommand() {
        // given
        long chatId = 123L;
        Update update = mockUpdate(chatId, "/track");

        // when
        SendMessage result = trackCommand.handle(update);

        // then
        verify(sessionService).setState(chatId, UserState.WAITING_FOR_TRACK_LINK);
        assertThat(result.getParameters().get("chat_id")).isEqualTo(chatId);
        assertThat((String) result.getParameters().get("text")).contains("ссылку, которую вы хотите отслеживать");
    }

    @Test
    @DisplayName("Должен обрабатывать команду /untrack")
    void shouldHandleUntrackCommand() {
        // given
        long chatId = 123L;
        Update update = mockUpdate(chatId, "/untrack");

        // when
        SendMessage result = untrackCommand.handle(update);

        // then
        verify(sessionService).setState(chatId, UserState.WAITING_FOR_UNTRACK_LINK);
        assertThat(result.getParameters().get("chat_id")).isEqualTo(chatId);
        assertThat((String) result.getParameters().get("text")).contains("ссылку, которую вы хотите ототслеживать");
    }

    @Test
    @DisplayName("Должен обрабатывать ввод ссылки для отслеживания")
    void shouldProcessTrackLink() {
        // given
        long chatId = 123L;
        String url = "https://github.com/user/repo";
        Update update = mockUpdate(chatId, url);

        when(sessionService.getState(chatId)).thenReturn(UserState.WAITING_FOR_TRACK_LINK);

        // when
        SendMessage result = linkProcessor.process(update);

        // then
        verify(sessionService).saveTempLink(eq(chatId), eq(url));
        verify(sessionService).setState(eq(chatId), eq(UserState.WAITING_FOR_TAGS));

        verify(scrapperClient, never()).addLink(anyLong(), any());

        assertThat((String) result.getParameters().get("text"))
                .contains("Ссылка принята")
                .contains("теги");
    }

    @Test
    @DisplayName("Должен обрабатывать некорректную ссылку для отслеживания")
    void shouldHandleInvalidTrackLink() {
        // given
        long chatId = 123L;
        String invalidUrl = "not-a-url";
        Update update = mockUpdate(chatId, invalidUrl);

        when(sessionService.getState(chatId)).thenReturn(UserState.WAITING_FOR_TRACK_LINK);

        // when
        SendMessage result = linkProcessor.process(update);

        // then
        verify(sessionService, never()).saveTempLink(anyLong(), anyString());
        assertThat((String) result.getParameters().get("text")).contains("Укажите полный URL");
    }

    @Test
    @DisplayName("Должен обрабатывать удаление ссылки")
    void shouldProcessUntrackLink() {
        // given
        long chatId = 123L;
        String url = "https://github.com/user/repo";
        Update update = mockUpdate(chatId, url);

        when(sessionService.getState(chatId)).thenReturn(UserState.WAITING_FOR_UNTRACK_LINK);
        when(scrapperClient.removeLink(eq(chatId), any()))
                .thenReturn(ResponseEntity.ok().build());

        // when
        SendMessage result = linkProcessor.process(update);

        // then
        verify(sessionService).clearSession(chatId);
        verify(scrapperClient).removeLink(eq(chatId), any(RemoveLinkRequest.class));
        assertThat((String) result.getParameters().get("text")).contains("Ссылка удалена");
    }

    @Test
    @DisplayName("Должен обрабатывать получение списка ссылок")
    void shouldGetLinksList() {
        // given
        long chatId = 123L;
        List<LinkResponse> links = List.of(
                new LinkResponse(1L, URI.create("https://github.com/user/repo1"), null),
                new LinkResponse(2L, URI.create("https://github.com/user/repo2"), null));
        ListLinksResponse response = new ListLinksResponse(links, 2);

        when(scrapperClient.getLinks(chatId)).thenReturn(ResponseEntity.ok(response));

        // when
        SendMessage result = linkService.getList(chatId);

        // then
        assertThat((String) result.getParameters().get("text"))
                .contains("Ваши ссылки:")
                .contains("github.com/user/repo1")
                .contains("github.com/user/repo2");
    }

    @Test
    @DisplayName("Должен обрабатывать пустой список ссылок")
    void shouldHandleEmptyLinksList() {
        // given
        long chatId = 123L;
        ListLinksResponse response = new ListLinksResponse(List.of(), 0);

        when(scrapperClient.getLinks(chatId)).thenReturn(ResponseEntity.ok(response));

        // when
        SendMessage result = linkService.getList(chatId);

        // then
        assertThat((String) result.getParameters().get("text")).contains("Список отслеживаемых ссылок пуст");
    }

    @Test
    @DisplayName("Должен обрабатывать ошибку при получении списка ссылок")
    void shouldHandleErrorWhenGettingLinks() {
        // given
        long chatId = 123L;

        when(scrapperClient.getLinks(chatId)).thenThrow(new RuntimeException("Service unavailable"));

        // when
        SendMessage result = linkService.getList(chatId);

        // then
        assertThat((String) result.getParameters().get("text")).contains("Не удалось получить список ссылок");
    }

    @Test
    @DisplayName("Должен обрабатывать обновления от Scrapper")
    void shouldHandleLinkUpdates() {
        // given
        LinkUpdate update =
                new LinkUpdate(1L, URI.create("https://github.com/user/repo"), "Новый коммит", List.of(123L, 456L));

        // when
        updateHandler.handleUpdate(update);

        // then
        verify(telegramBot, times(2)).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Должен корректно управлять сессиями пользователей")
    void shouldManageUserSessions() {
        // given
        UserSessionService sessionService = new UserSessionService();
        long chatId = 123L;

        // when - then
        assertThat(sessionService.isWaitingForInput(chatId)).isFalse();
        assertThat(sessionService.getState(chatId)).isEqualTo(UserState.NONE);

        sessionService.setState(chatId, UserState.WAITING_FOR_TRACK_LINK);
        assertThat(sessionService.isWaitingForInput(chatId)).isTrue();
        assertThat(sessionService.getState(chatId)).isEqualTo(UserState.WAITING_FOR_TRACK_LINK);

        sessionService.saveTempLink(chatId, "https://github.com/test");
        assertThat(sessionService.getTempLink(chatId)).isEqualTo("https://github.com/test");

        sessionService.clearSession(chatId);
        assertThat(sessionService.isWaitingForInput(chatId)).isFalse();
        assertThat(sessionService.getState(chatId)).isEqualTo(UserState.NONE);
        assertThat(sessionService.getTempLink(chatId)).isNull();
    }

    @Test
    @DisplayName("Должен инициализировать команды бота")
    void shouldInitializeBotCommands() {
        // given
        when(telegramBot.execute(any(SetMyCommands.class))).thenReturn(mock(BaseResponse.class));

        // when
        botUpdateListener.init();

        // then
        verify(telegramBot).execute(any(SetMyCommands.class));
        verify(telegramBot).setUpdatesListener(botUpdateListener);
    }

    private Update mockUpdate(long chatId, String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        lenient().when(update.message()).thenReturn(message);
        lenient().when(message.chat()).thenReturn(chat);
        lenient().when(message.text()).thenReturn(text);
        lenient().when(chat.id()).thenReturn(chatId);
        lenient().when(update.updateId()).thenReturn((int) chatId);

        return update;
    }
}
