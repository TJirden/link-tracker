package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.client.dto.AddLinkRequest;
import backend.academy.linktracker.bot.client.dto.LinkResponse;
import backend.academy.linktracker.bot.client.dto.ListLinksResponse;
import backend.academy.linktracker.bot.client.dto.RemoveLinkRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface ScrapperClient {

    @PostExchange("/links")
    ResponseEntity<LinkResponse> addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest request);

    @DeleteExchange("/links")
    ResponseEntity<LinkResponse> removeLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest request);

    @GetExchange("/links")
    ResponseEntity<ListLinksResponse> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId);

    @PostExchange("/tg-chat/{id}")
    ResponseEntity<Void> registerChat(@PathVariable("id") Long id);

    @DeleteExchange("/tg-chat/{id}")
    ResponseEntity<Void> deleteChat(@PathVariable("id") Long id);
}
