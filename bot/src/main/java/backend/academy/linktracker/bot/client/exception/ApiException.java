package backend.academy.linktracker.bot.client.exception;

import backend.academy.linktracker.bot.client.dto.ApiErrorResponse;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ApiErrorResponse apiErrorResponse;

    public ApiException(ApiErrorResponse apiErrorResponse) {
        super(apiErrorResponse.description());
        this.apiErrorResponse = apiErrorResponse;
    }
}
