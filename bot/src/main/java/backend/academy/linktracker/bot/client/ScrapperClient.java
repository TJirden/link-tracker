package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.client.dto.AddLinkRequest;
import backend.academy.linktracker.bot.client.dto.LinkResponse;
import backend.academy.linktracker.bot.client.dto.ListLinksResponse;
import backend.academy.linktracker.bot.client.dto.RemoveLinkRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/links")
public interface ScrapperClient {

    @PostExchange
    LinkResponse addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest request);

    @DeleteExchange
    LinkResponse removeLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest request);

    @GetExchange
    ListLinksResponse getLinks(@RequestHeader("Tg-Chat-Id") Long chatId);
}
