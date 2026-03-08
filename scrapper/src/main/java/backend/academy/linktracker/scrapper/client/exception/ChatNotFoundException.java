package backend.academy.linktracker.scrapper.client.exception;

public class ChatNotFoundException extends RuntimeException {
    public ChatNotFoundException(Long id) {
        super("Chat %d not found".formatted(id));
    }
}
