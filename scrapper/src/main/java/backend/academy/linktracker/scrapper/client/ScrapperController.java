package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.client.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.client.dto.LinkResponse;
import backend.academy.linktracker.scrapper.client.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.client.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.service.LinksKeeper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ScrapperController {

    private final LinksKeeper linksKeeper;

    @PostMapping("/links")
    public LinkResponse addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest request) {
        log.info("Добавление ссылки {} для чата {}", request.link(), chatId);
        return linksKeeper.addLink(chatId, request);
    }

    @DeleteMapping("/links")
    public LinkResponse removeLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest request) {
        log.info("Удаление ссылки {} для чата {}", request.link(), chatId);
        return linksKeeper.removeLink(chatId, request);
    }

    @GetMapping("/links")
    public ListLinksResponse getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        log.info("Получение списка ссылок для чата {}", chatId);
        return linksKeeper.getLinks(chatId);
    }

    @PostMapping("/tg-chat/{id}")
    public ResponseEntity<Void> registerChat(@PathVariable("id") Long id) {
        log.info("Регистрация нового чата: {}", id);
        linksKeeper.registerChat(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tg-chat/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable("id") Long id) {
        log.info("Удаление чата: {}", id);
        linksKeeper.deleteChat(id);
        return ResponseEntity.ok().build();
    }
}
