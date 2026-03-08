package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.client.GithubClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.client.dto.GithubRepoResponse;
import backend.academy.linktracker.scrapper.client.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.dto.StackOverflowItem;
import backend.academy.linktracker.scrapper.client.dto.StackOverflowResponse;
import backend.academy.linktracker.scrapper.service.LinksKeeper;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkUpdaterScheduler {

    private final LinksKeeper linksKeeper;
    private final GithubClient githubClient;
    private final StackOverflowClient stackOverflowClient;
    private final BotClient botClient;

    private final Map<String, OffsetDateTime> lastGithubUpdates = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastStackoverflowUpdates = new ConcurrentHashMap<>();

    @Scheduled(fixedDelayString = "#{@schedulerConfiguration.interval.toMillis()}")
    public void update() {
        log.info("Запуск планировщика: проверка обновлений...");

        Map<String, Set<Long>> links = linksKeeper.getAllLinksWithSubscribers();

        if (links.isEmpty()) {
            log.info("Нет активных ссылок для проверки.");
            return;
        }

        for (var entry : links.entrySet()) {
            String url = entry.getKey();
            Set<Long> chatIds = entry.getValue();

            try {
                processLink(url, chatIds);
            } catch (Exception e) {
                log.error("Ошибка при обработке ссылки: {}", url, e);
            }
        }
    }

    private void processLink(String url, Set<Long> chatIds) {
        URI uri = URI.create(url);
        String host = uri.getHost();

        if (host.contains("github.com")) {
            processGithubLink(url, uri, chatIds);
        } else if (host.contains("stackoverflow.com") || host.contains("stackexchange.com")) {
            processStackoverflowLink(url, uri, chatIds);
        }
    }

    private void processGithubLink(String url, URI uri, Set<Long> chatIds) {
        try {
            String[] pathParts = uri.getPath().split("/");
            if (pathParts.length < 3) {
                log.warn("Некорректный GitHub URL: {}", url);
                return;
            }

            String owner = pathParts[1];
            String repo = pathParts[2];

            GithubRepoResponse response = githubClient.fetchRepository(owner, repo);
            OffsetDateTime lastUpdate = response.updatedAt();

            OffsetDateTime previousUpdate = lastGithubUpdates.get(url);

            if (previousUpdate == null) {
                lastGithubUpdates.put(url, lastUpdate);
                log.debug("Ссылка добавлена в мониторинг GitHub: {}", url);
            } else if (lastUpdate.isAfter(previousUpdate)) {
                String description = String.format("Обновление в репозитории %s/%s: %s", owner, repo, response.name());

                sendUpdate(url, chatIds, description);
                lastGithubUpdates.put(url, lastUpdate);
            }

        } catch (Exception e) {
            log.error("Ошибка при проверке GitHub ссылки: {}", url, e);
        }
    }

    private void processStackoverflowLink(String url, URI uri, Set<Long> chatIds) {
        try {
            String path = uri.getPath();
            if (!path.contains("/questions/")) {
                log.warn("Некорректный StackOverflow URL: {}", url);
                return;
            }

            String[] pathParts = path.split("/");
            Long questionId = null;

            for (int i = 0; i < pathParts.length - 1; i++) {
                if ("questions".equals(pathParts[i])) {
                    questionId = Long.parseLong(pathParts[i + 1]);
                    break;
                }
            }

            if (questionId == null) {
                log.warn("Не удалось извлечь ID вопроса из URL: {}", url);
                return;
            }

            StackOverflowResponse response = stackOverflowClient.fetchQuestion(questionId);

            if (response.items() == null || response.items().isEmpty()) {
                log.warn("Вопрос не найден: {}", url);
                return;
            }

            StackOverflowItem item = response.items().getFirst();
            Instant lastActivity = item.lastActivityDate();

            Instant previousActivity = lastStackoverflowUpdates.get(url);

            if (previousActivity == null) {
                lastStackoverflowUpdates.put(url, lastActivity);
                log.debug("Ссылка добавлена в мониторинг StackOverflow: {}", url);
            } else if (lastActivity.isAfter(previousActivity)) {
                String description = String.format("Новая активность в вопросе: %s", item.title());

                sendUpdate(url, chatIds, description);
                lastStackoverflowUpdates.put(url, lastActivity);
            }

        } catch (Exception e) {
            log.error("Ошибка при проверке StackOverflow ссылки: {}", url, e);
        }
    }

    private void sendUpdate(String url, Set<Long> chatIds, String description) {
        log.info("Обнаружено обновление для ссылки: {} - {}", url, description);

        LinkUpdate update =
                new LinkUpdate((long) url.hashCode(), URI.create(url), description, new ArrayList<>(chatIds));

        try {
            botClient.sendUpdate(update);
            log.info("Уведомление отправлено в Бот для {} чатов", chatIds.size());
        } catch (Exception e) {
            log.error("Ошибка при отправке обновления в Бот для ссылки: {}", url, e);
        }
    }
}
