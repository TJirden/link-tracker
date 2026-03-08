package backend.academy.linktracker.scrapper.client.dto;

import java.util.List;

public record StackOverflowResponse(List<StackOverflowItem> items) {}
