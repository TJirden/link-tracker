package backend.academy.linktracker.bot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.dto.LinkResponse;
import backend.academy.linktracker.bot.client.dto.LinkUpdate;
import backend.academy.linktracker.bot.client.dto.ListLinksResponse;
import backend.academy.linktracker.bot.command.CancelCommand;
import backend.academy.linktracker.bot.command.DialogCommand;
import backend.academy.linktracker.bot.command.NonDialogCommand;
import backend.academy.linktracker.bot.command.NotagsCommand;
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

    @Mock
    private LinkProcessor linkProcessor;

    @Captor
    private ArgumentCaptor<SendMessage> messageCaptor;

    private LinkService linkService;
    private UpdateHandler updateHandler;
    private BotUpdateListener botUpdateListener;
    private UpdateProcessor updateProcessor;

    private TrackCommand trackCommand;
    private UntrackCommand untrackCommand;
    private CancelCommand cancelCommand;
    private NotagsCommand notagsCommand;

    @BeforeEach
    void setUp() {
        linkService = new LinkService(scrapperClient);
        updateHandler = new UpdateHandler(telegramBot);

        trackCommand = new TrackCommand(sessionService);
        untrackCommand = new UntrackCommand(sessionService);
        cancelCommand = new CancelCommand(sessionService);
        notagsCommand = new NotagsCommand(linkProcessor, sessionService);

        List<NonDialogCommand> nonDialogCommands = List.of(trackCommand, untrackCommand, cancelCommand);
        List<DialogCommand> dialogCommands = List.of(notagsCommand, cancelCommand);

        updateProcessor = new UpdateProcessor(nonDialogCommands, dialogCommands, sessionService, linkProcessor);
        botUpdateListener =
                new BotUpdateListener(telegramBot, nonDialogCommands, dialogCommands, updateProcessor, sessionService);
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
        assertThat((String) result.getParameters().get("text")).contains("ототслеживать");
    }

    @Test
    @DisplayName("Должен обрабатывать команду /cancel")
    void shouldHandleCancelCommand() {
        // given
        long chatId = 123L;
        Update update = mockUpdate(chatId, "/cancel");

        // when
        SendMessage result = cancelCommand.handle(update);

        // then
        verify(sessionService).clearSession(chatId);
        assertThat(result.getParameters().get("chat_id")).isEqualTo(chatId);
        assertThat((String) result.getParameters().get("text")).contains("все хорошо");
    }

    @Test
    @DisplayName("Должен обрабатывать команду /notags")
    void shouldHandleNotagsCommand() {
        // given
        long chatId = 123L;
        Update update = mockUpdate(chatId, "/notags");
        SendMessage expectedMessage = new SendMessage(chatId, "Ссылка добавлена для отслеживания");

        when(linkProcessor.withoutTags(chatId)).thenReturn(expectedMessage);

        // when
        SendMessage result = notagsCommand.handle(update);

        // then
        verify(linkProcessor).withoutTags(chatId);
        assertThat(result).isSameAs(expectedMessage);
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
        UserSessionService realSessionService = new UserSessionService();
        long chatId = 123L;

        // when - then
        assertThat(realSessionService.isWaitingForInput(chatId)).isFalse();
        assertThat(realSessionService.getState(chatId)).isEqualTo(UserState.NONE);

        realSessionService.setState(chatId, UserState.WAITING_FOR_TRACK_LINK);
        assertThat(realSessionService.isWaitingForInput(chatId)).isTrue();
        assertThat(realSessionService.getState(chatId)).isEqualTo(UserState.WAITING_FOR_TRACK_LINK);

        realSessionService.saveTempLink(chatId, "https://github.com/test");
        assertThat(realSessionService.getTempLink(chatId)).isEqualTo("https://github.com/test");

        realSessionService.clearSession(chatId);
        assertThat(realSessionService.isWaitingForInput(chatId)).isFalse();
        assertThat(realSessionService.getState(chatId)).isEqualTo(UserState.NONE);
        assertThat(realSessionService.getTempLink(chatId)).isNull();
    }

    @Test
    @DisplayName("Должен инициализировать команды бота")
    void shouldInitializeBotCommands() {
        // given
        when(telegramBot.execute(any(SetMyCommands.class))).thenReturn(mock(BaseResponse.class));

        // when
        botUpdateListener.init();

        // then
        verify(telegramBot, times(1)).execute(any(SetMyCommands.class));
        verify(telegramBot, times(1)).setUpdatesListener(botUpdateListener);
    }

    @Test
    @DisplayName("Должен обрабатывать команды через UpdateProcessor с реальным процессором")
    void shouldProcessCommandsThroughUpdateProcessor() {
        // given
        long chatId = 123L;
        Update update = mockUpdate(chatId, "/track");

        when(sessionService.isWaitingForInput(chatId)).thenReturn(false);

        // when
        SendMessage result = updateProcessor.processUpdate(update);

        // then
        verify(sessionService).setState(chatId, UserState.WAITING_FOR_TRACK_LINK);
        assertThat(result).isNotNull();
        assertThat(result.getParameters().get("chat_id")).isEqualTo(chatId);
        assertThat((String) result.getParameters().get("text")).contains("ссылку");
    }

    @Test
    @DisplayName("Должен обрабатывать диалоговый ввод через UpdateProcessor")
    void shouldProcessDialogInputThroughUpdateProcessor() {
        // given
        long chatId = 123L;
        String input = "some input";
        Update update = mockUpdate(chatId, input);
        SendMessage expectedMessage = new SendMessage(chatId, "Обработано");

        when(sessionService.isWaitingForInput(chatId)).thenReturn(true);
        when(linkProcessor.process(update)).thenReturn(expectedMessage);

        // when
        SendMessage result = updateProcessor.processUpdate(update);

        // then
        verify(linkProcessor).process(update);
        assertThat(result).isSameAs(expectedMessage);
    }

    @Test
    @DisplayName("Должен обрабатывать диалоговую команду /notags через UpdateProcessor")
    void shouldProcessDialogCommandThroughUpdateProcessor() {
        // given
        long chatId = 123L;
        Update update = mockUpdate(chatId, "/notags");
        SendMessage expectedMessage = new SendMessage(chatId, "Ссылка добавлена");

        when(sessionService.isWaitingForInput(chatId)).thenReturn(true);
        when(linkProcessor.withoutTags(chatId)).thenReturn(expectedMessage);

        // when
        SendMessage result = updateProcessor.processUpdate(update);

        // then
        verify(linkProcessor).withoutTags(chatId);
        assertThat(result).isSameAs(expectedMessage);
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
