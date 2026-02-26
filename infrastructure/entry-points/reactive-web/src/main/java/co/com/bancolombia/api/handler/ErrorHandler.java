package co.com.bancolombia.api.handler;

import co.com.bancolombia.model.error.ErrorResponse;
import co.com.bancolombia.model.exception.ConflictException;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ErrorHandler {

    public Mono<ServerResponse> handleError(Throwable throwable) {
        ErrorResponse errorResponse;

        // Handle ValidationException
        if (throwable instanceof ValidationException) {
            errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Validation Error",
                    throwable.getMessage()
            );
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(errorResponse);
        }
        // Handle NotFoundException
        else if (throwable instanceof NotFoundException) {
            errorResponse = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    throwable.getMessage()
            );
            return ServerResponse.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(errorResponse);
        }
        // Handle ConflictException
        else if (throwable instanceof ConflictException) {
            errorResponse = new ErrorResponse(
                    HttpStatus.CONFLICT.value(),
                    "Conflict",
                    throwable.getMessage()
            );
            return ServerResponse.status(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(errorResponse);
        }
        // Handle generic errors
        else {
            errorResponse = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error",
                    "An unexpected error occurred"
            );
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(errorResponse);
        }
    }
}
