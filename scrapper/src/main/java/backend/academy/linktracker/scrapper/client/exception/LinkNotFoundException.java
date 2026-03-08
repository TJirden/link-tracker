package backend.academy.linktracker.scrapper.client.exception;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(String url) {
        super("Link %s not found".formatted(url));
    }
}
