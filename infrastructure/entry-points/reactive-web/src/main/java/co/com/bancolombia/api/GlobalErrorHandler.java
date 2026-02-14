package co.com.bancolombia.api;

import co.com.bancolombia.model.exception.ConflictException;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.exception.ValidationException;
import org.springframework.http.*;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail notFound(NotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Not Found");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail conflict(ConflictException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Conflict");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail badRequest(ValidationException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Bad Request");
        pd.setDetail(ex.getMessage());
        return pd;
    }
}

