package ru.practicum.shareit.error;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(final NotFoundException e) {
        log.warn("Исключение NotFoundException по причине: {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleValidation(final ValidationException e) {
        log.warn("Исключение ValidationException по причине: {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(final MethodArgumentNotValidException e) {
        log.warn("Исключение MethodArgumentNotValidException по причине: {}", e.getMessage());
        return new ErrorResponse(e.getBindingResult().getFieldErrors().getFirst().getDefaultMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Optional<ErrorResponse> handleConstraintViolation(final ConstraintViolationException e) {
        Optional<ErrorResponse> errorResponse = e.getConstraintViolations().stream()
                .map(
                        violation -> {
                            String propertyName = "";
                            for (Path.Node node : violation.getPropertyPath()) {
                                propertyName = node.getName();
                            }
                            return new ErrorResponse(
                                    propertyName + " " + violation.getMessage()
                            );
                        }
                )
                .findFirst();
        errorResponse.ifPresent(response ->
                log.warn("Исключение ConstraintViolationException по причине: {}", response.getError()));
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable e) {
        log.error("Исключение Throwable по причине: {}", e.getMessage());
        return new ErrorResponse("Произошла непредвиденная ошибка.");
    }
}
