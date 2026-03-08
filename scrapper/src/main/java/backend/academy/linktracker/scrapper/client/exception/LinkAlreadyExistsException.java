package backend.academy.linktracker.scrapper.client.exception;

public class LinkAlreadyExistsException extends RuntimeException {
    public LinkAlreadyExistsException(String url) {
        super("Link %s already tracked".formatted(url));
    }
}
