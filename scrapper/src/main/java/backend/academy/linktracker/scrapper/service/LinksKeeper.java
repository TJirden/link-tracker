package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.client.dto.LinkResponse;
import backend.academy.linktracker.scrapper.client.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.client.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.client.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.client.exception.LinkNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class LinksKeeper {

    private final Set<Long> registeredChats = ConcurrentHashMap.newKeySet();
    private final Map<Long, Map<String, Set<String>>> chatLinks = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> urlSubscribers = new ConcurrentHashMap<>();

    public void registerChat(Long chatId) {
        if (registeredChats.contains(chatId)) {
            return;
        }
        registeredChats.add(chatId);
        chatLinks.putIfAbsent(chatId, new ConcurrentHashMap<>());
    }

    public synchronized void deleteChat(Long chatId) {
        verifyChatExists(chatId);

        Map<String, Set<String>> links = chatLinks.get(chatId);
        if (links != null) {
            for (String url : links.keySet()) {
                removeSubscriber(url, chatId);
            }
        }

        registeredChats.remove(chatId);
        chatLinks.remove(chatId);
    }

    public ListLinksResponse getLinks(Long chatId) {
        verifyChatExists(chatId);

        Map<String, Set<String>> linksMap = chatLinks.get(chatId);
        List<LinkResponse> list = new ArrayList<>();

        for (var entry : linksMap.entrySet()) {
            String url = entry.getKey();
            Set<String> tags = entry.getValue();
            list.add(new LinkResponse((long) url.hashCode(), URI.create(url), new ArrayList<>(tags)));
        }

        return new ListLinksResponse(list, list.size());
    }

    public synchronized LinkResponse addLink(Long chatId, AddLinkRequest request) {
        verifyChatExists(chatId);

        String url = request.link().toString();
        Map<String, Set<String>> linksMap = chatLinks.get(chatId);

        Set<String> newTags = request.tags() != null ? new HashSet<>(request.tags()) : new HashSet<>();
        linksMap.put(url, newTags);

        urlSubscribers.computeIfAbsent(url, k -> ConcurrentHashMap.newKeySet()).add(chatId);

        return new LinkResponse((long) url.hashCode(), request.link(), new ArrayList<>(newTags));
    }

    public synchronized LinkResponse removeLink(Long chatId, RemoveLinkRequest request) {
        verifyChatExists(chatId);

        String url = request.link().toString();
        Map<String, Set<String>> linksMap = chatLinks.get(chatId);

        Set<String> removedTags = linksMap.remove(url);

        if (removedTags == null) {
            throw new LinkNotFoundException(url);
        }

        removeSubscriber(url, chatId);

        return new LinkResponse((long) url.hashCode(), request.link(), new ArrayList<>(removedTags));
    }

    public Map<String, Set<Long>> getAllLinksWithSubscribers() {
        return urlSubscribers;
    }

    private void removeSubscriber(String url, Long chatId) {
        Set<Long> subs = urlSubscribers.get(url);
        if (subs != null) {
            subs.remove(chatId);
            if (subs.isEmpty()) {
                urlSubscribers.remove(url);
            }
        }
    }

    private void verifyChatExists(Long chatId) {
        if (!registeredChats.contains(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
    }
}
