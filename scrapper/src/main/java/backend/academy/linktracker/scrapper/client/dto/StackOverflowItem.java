package backend.academy.linktracker.scrapper.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record StackOverflowItem(
        @JsonProperty("question_id") Long questionId,
        String title,
        @JsonProperty("last_activity_date") Instant lastActivityDate,
        @JsonProperty("creation_date") Instant creationDate) {}
