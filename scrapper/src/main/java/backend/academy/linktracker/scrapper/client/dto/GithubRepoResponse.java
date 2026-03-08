package backend.academy.linktracker.scrapper.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GithubRepoResponse(
        Long id,
        String name,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("updated_at") OffsetDateTime updatedAt) {}
