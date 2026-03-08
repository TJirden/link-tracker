package backend.academy.linktracker.scrapper.client.exception;

public class ChatAlreadyExistsException extends RuntimeException {
    public ChatAlreadyExistsException(Long id) {
        super("Chat %d already registered".formatted(id));
    }
}
