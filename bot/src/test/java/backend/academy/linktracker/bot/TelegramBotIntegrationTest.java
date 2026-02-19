package backend.academy.linktracker.bot;

import backend.academy.linktracker.bot.properties.TelegramProperties;
import com.pengrad.telegrambot.TelegramBot;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@EnableWireMock
class TelegramBotIntegrationTest implements WithAssertions {

    @Autowired
    TelegramBot telegramBot;

    @Autowired
    TelegramProperties telegramProperties;

    @AfterEach
    void clearUpdatesListener() {
        telegramBot.removeGetUpdatesListener();
    }

    //    @Test
    //    void nonExistingTokenRequest() {
    //        stubFor(post(urlMatching("/bot[^/]+/getUpdates"))
    //                .willReturn(aResponse()
    //                        .withStatus(404)
    //                        .withBody("{\"ok\":false,\"error_code\":404,\"description\":\"Not Found\"}")));
    //
    //        var getUpdatesRequest = new GetUpdates();
    //        var getUpdatesResponse = telegramBot.execute(getUpdatesRequest);
    //
    //        assertFalse(getUpdatesResponse.isOk());
    //        assertEquals(404, getUpdatesResponse.errorCode());
    //
    //        verify(
    //                1,
    //                postRequestedFor(urlPathTemplate("/bot{token}/getUpdates"))
    //                        .withPathParam("token", equalTo(telegramProperties.getToken())));
    //    }
    //
    //    @Test
    //    void updatesListenerReceivesUpdates() throws InterruptedException {
    //        stubFor(post(urlMatching("/bot[^/]+/getUpdates"))
    //                .inScenario("Updates Listener")
    //                .whenScenarioStateIs(STARTED)
    //                .willReturn(aResponse()
    //                        .withStatus(200)
    //                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    //                        .withBody("""
    //                                {
    //                                  "ok": true,
    //                                  "result": [
    //                                    {
    //                                      "update_id": 123456,
    //                                      "message": {
    //                                        "message_id": 1,
    //                                        "from": {
    //                                          "id": 987654321,
    //                                          "is_bot": false,
    //                                          "first_name": "Test",
    //                                          "username": "testuser"
    //                                        },
    //                                        "chat": {
    //                                          "id": 987654321,
    //                                          "type": "private"
    //                                        },
    //                                        "date": 1234567890,
    //                                        "text": "Hello Bot"
    //                                      }
    //                                    }
    //                                  ]
    //                                }
    //                                """))
    //                .willSetStateTo("Updates Received"));
    //
    //        stubFor(post(urlMatching("/bot[^/]+/getUpdates"))
    //                .inScenario("Updates Listener")
    //                .whenScenarioStateIs("Updates Received")
    //                .willReturn(aResponse()
    //                        .withStatus(200)
    //                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    //                        .withBody("""
    //                                {
    //                                  "ok": true,
    //                                  "result": []
    //                                }
    //                                """)));
    //
    //        List<Update> receivedUpdates = new CopyOnWriteArrayList<>();
    //        CountDownLatch latch = new CountDownLatch(1);
    //
    //        telegramBot.setUpdatesListener(updates -> {
    //            receivedUpdates.addAll(updates);
    //            latch.countDown();
    //            return UpdatesListener.CONFIRMED_UPDATES_ALL;
    //        });
    //
    //        boolean received = latch.await(10, TimeUnit.SECONDS);
    //
    //        assertTrue(received);
    //        assertThat(receivedUpdates)
    //                .hasSize(1)
    //                .first()
    //                .returns(123456, Update::updateId)
    //                .extracting(Update::message)
    //                .returns("Hello Bot", Message::text)
    //                .extracting(Message::from)
    //                .returns("testuser", User::username);
    //
    //        verify(postRequestedFor(urlPathTemplate("/bot{token}/getUpdates"))
    //                .withPathParam("token", equalTo(telegramProperties.getToken())));
    //    }
}
