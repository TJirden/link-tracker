package backend.academy.linktracker.scrapper.client.dto;

import java.net.URI;
import java.util.List;

public record LinkResponse(Long id, URI url, List<String> tags) {}
