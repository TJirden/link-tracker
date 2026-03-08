package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.client.dto.GithubRepoResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(accept = "application/vnd.github+json")
public interface GithubClient {

    @GetExchange("/repos/{owner}/{repo}")
    GithubRepoResponse fetchRepository(@PathVariable("owner") String owner, @PathVariable("repo") String repo);
}
