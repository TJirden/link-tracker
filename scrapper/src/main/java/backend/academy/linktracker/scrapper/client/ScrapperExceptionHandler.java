package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.client.dto.ApiErrorResponse;
import backend.academy.linktracker.scrapper.client.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.client.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.client.exception.LinkAlreadyExistsException;
import backend.academy.linktracker.scrapper.client.exception.LinkNotFoundException;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ScrapperExceptionHandler {

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<@NotNull ApiErrorResponse> handleChatNotFound(ChatNotFoundException ex) {
        return buildResponse(ex, HttpStatus.NOT_FOUND, "Чат не найден");
    }

    @ExceptionHandler(ChatAlreadyExistsException.class)
    public ResponseEntity<@NotNull ApiErrorResponse> handleChatAlreadyExists(ChatAlreadyExistsException ex) {
        return buildResponse(ex, HttpStatus.CONFLICT, "Чат уже зарегистрирован");
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<@NotNull ApiErrorResponse> handleLinkNotFound(LinkNotFoundException ex) {
        return buildResponse(ex, HttpStatus.NOT_FOUND, "Ссылка не найдена");
    }

    @ExceptionHandler(LinkAlreadyExistsException.class)
    public ResponseEntity<@NotNull ApiErrorResponse> handleLinkAlreadyExists(LinkAlreadyExistsException ex) {
        return buildResponse(ex, HttpStatus.CONFLICT, "Ссылка уже отслеживается");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NotNull ApiErrorResponse> handleGeneral(Exception ex) {
        return buildResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервиса");
    }

    private ResponseEntity<@NotNull ApiErrorResponse> buildResponse(
            Exception ex, HttpStatus status, String description) {
        ApiErrorResponse body = new ApiErrorResponse(
                description,
                String.valueOf(status.value()),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                Arrays.stream(ex.getStackTrace())
                        .map(StackTraceElement::toString)
                        .toList());
        return ResponseEntity.status(status).body(body);
    }
}
