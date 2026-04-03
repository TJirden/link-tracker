package backend.academy.linktracker.scrapper.service.orm;

import backend.academy.linktracker.scrapper.client.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.client.dto.LinkResponse;
import backend.academy.linktracker.scrapper.client.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.client.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.client.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.client.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.entity.Chat;
import backend.academy.linktracker.scrapper.entity.Link;
import backend.academy.linktracker.scrapper.entity.Subscription;
import backend.academy.linktracker.scrapper.entity.SubscriptionId;
import backend.academy.linktracker.scrapper.entity.Tag;
import backend.academy.linktracker.scrapper.repository.jpa.JpaChatRepository;
import backend.academy.linktracker.scrapper.repository.jpa.JpaLinkRepository;
import backend.academy.linktracker.scrapper.repository.jpa.JpaSubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.jpa.JpaTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrmLinksKeeper {

    private final JpaChatRepository chatRepository;
    private final JpaLinkRepository linkRepository;
    private final JpaSubscriptionRepository subscriptionRepository;
    private final JpaTagRepository tagRepository;

    public void registerChat(Long chatId) {
        if (!chatRepository.existsById(chatId)) {
            Chat chat = new Chat();
            chat.setId(chatId);
            chatRepository.save(chat);
        }
    }

    public void deleteChat(Long chatId) {
        if (!chatRepository.existsById(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
        chatRepository.deleteById(chatId);
    }

    @Transactional(readOnly = true)
    public ListLinksResponse getLinks(Long chatId) {
        if (!chatRepository.existsById(chatId)) {
            throw new ChatNotFoundException(chatId);
        }

        List<Subscription> subscriptions = subscriptionRepository.findByChatId(chatId);
        List<LinkResponse> responses = subscriptions.stream()
            .map(sub -> {
                Link link = sub.getLink();
                Set<String> tagNames = link.getTags().stream()
                    .map(Tag::getName)
                    .collect(Collectors.toSet());
                return new LinkResponse(link.getId(), URI.create(link.getUrl()), new ArrayList<>(tagNames));
            })
            .collect(Collectors.toList());

        return new ListLinksResponse(responses, responses.size());
    }

    public LinkResponse addLink(Long chatId, AddLinkRequest request) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ChatNotFoundException(chatId));

        String url = request.link().toString();
        Link link = linkRepository.findByUrl(url).orElseGet(() -> {
            Link newLink = new Link();
            newLink.setUrl(url);
            return linkRepository.save(newLink);
        });

        if (request.tags() != null) {
            for (String tagName : request.tags()) {
                Tag tag = tagRepository.findByName(tagName).orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setName(tagName);
                    return tagRepository.save(newTag);
                });
                link.getTags().add(tag);
            }
            linkRepository.save(link);
        }

        SubscriptionId subId = new SubscriptionId(chatId, link.getId());
        if (!subscriptionRepository.existsById(subId)) {
            Subscription subscription = new Subscription();
            subscription.setId(subId);
            subscription.setChat(chat);
            subscription.setLink(link);
            subscriptionRepository.save(subscription);
        }

        Set<String> tagNames = link.getTags().stream()
            .map(Tag::getName)
            .collect(Collectors.toSet());

        return new LinkResponse(link.getId(), request.link(), new ArrayList<>(tagNames));
    }

    public LinkResponse removeLink(Long chatId, RemoveLinkRequest request) {
        String url = request.link().toString();
        Link link = linkRepository.findByUrl(url)
            .orElseThrow(() -> new LinkNotFoundException(url));

        if (!subscriptionRepository.existsByChatIdAndLinkId(chatId, link.getId())) {
            throw new LinkNotFoundException(url);
        }

        subscriptionRepository.deleteByChatIdAndLinkId(chatId, link.getId());

        Set<String> tagNames = link.getTags().stream()
            .map(Tag::getName)
            .collect(Collectors.toSet());

        return new LinkResponse(link.getId(), request.link(), new ArrayList<>(tagNames));
    }

    @Transactional(readOnly = true)
    public Map<String, Set<Long>> getAllLinksWithSubscribers() {
        List<Link> links = linkRepository.findAll();
        return links.stream()
            .collect(Collectors.toMap(
                Link::getUrl,
                link -> subscriptionRepository.findByChatId(link.getId()).stream()
                    .map(sub -> sub.getChat().getId())
                    .collect(Collectors.toSet())
            ));
    }
}
