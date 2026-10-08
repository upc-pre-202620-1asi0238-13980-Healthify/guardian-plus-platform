package com.healthify.guardian.platform.shared.interfaces.rest;

import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import com.healthify.guardian.platform.shared.interfaces.rest.resources.ErrorResource;
import com.healthify.guardian.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API.
 * Provides centralized exception handling for the entire application,
 * ensuring all unhandled exceptions are translated to consistent
 * HTTP responses via the shared error assembly pattern.
 */
@RestControllerAdvice
@NullMarked
public class GlobalExceptionHandler {

    /**
     * Handles validation exceptions from Spring's request body validation.
     * Maps validation failure to a standardized error response.
     *
     * @param ex the validation exception from @Valid binding
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors();
        var validationPrefix = MessageResolver.resolveOrDefault("validation.field.prefix", "Field");
        var errorDetails = fieldErrors.isEmpty()
                ? MessageResolver.resolveOrDefault("validation.request.failed", "Request validation failed")
                : fieldErrors.stream()
                  .map(error -> "%s %s: %s".formatted(
                          validationPrefix,
                          error.getField(),
                          error.getDefaultMessage()
                  ))
                  .reduce((a, b) -> a + "; " + b)
                  .orElse(MessageResolver.resolveOrDefault("validation.request.failed", "Request validation failed"));

        var applicationError = ApplicationError.validationError("request-body", errorDetails);
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles invalid request arguments such as malformed UUID path or payload values,
     * and domain/value-object guard violations, whose message is a bundle key
     * (e.g. {@code "reminder.person-under-care-id.invalid"}) resolved here.
     *
     * @param ex the illegal argument exception
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        var reason = ex.getMessage() != null
                ? MessageResolver.resolveOrDefault(ex.getMessage(), ex.getMessage())
                : MessageResolver.resolveOrDefault("validation.request.failed", "Request validation failed");
        var applicationError = ApplicationError.validationError(
                MessageResolver.resolveOrDefault("validation.request.argument", "request-argument"),
                reason
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles a path variable or query parameter that cannot be converted to its declared type,
     * such as a malformed UUID or an unknown enum constant.
     *
     * @param ex the type mismatch exception
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var requiredType = ex.getRequiredType();
        var reason = requiredType != null && requiredType.isEnum()
                ? MessageResolver.resolveOrDefault(
                        "validation.parameter.invalid-value",
                        "Parameter '%s' has an invalid value".formatted(ex.getName()),
                        ex.getName(),
                        Arrays.stream(requiredType.getEnumConstants())
                                .map(constant -> ((Enum<?>) constant).name())
                                .collect(Collectors.joining(", ")))
                : MessageResolver.resolveOrDefault(
                        "validation.parameter.invalid-type",
                        "Parameter '%s' has an invalid value".formatted(ex.getName()),
                        ex.getName(),
                        requiredType != null ? requiredType.getSimpleName() : "value");
        return toValidationErrorResponse(reason);
    }

    /**
     * Handles a required query parameter that is missing from the request.
     *
     * @param ex the missing parameter exception
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        return toValidationErrorResponse(MessageResolver.resolveOrDefault(
                "validation.parameter.missing",
                "Required parameter '%s' is missing".formatted(ex.getParameterName()),
                ex.getParameterName()));
    }

    /**
     * Handles a request body that is missing or cannot be parsed (malformed JSON, unknown enum
     * constant, unparseable date...). The parser's own message is deliberately not exposed, since
     * it reveals internal class names.
     *
     * @param ex the unreadable message exception
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return toValidationErrorResponse(MessageResolver.resolveOrDefault(
                "validation.body.unreadable", "Request body is missing or malformed"));
    }

    /**
     * Handles requests Spring MVC rejects before reaching a controller (unknown endpoint, unsupported
     * HTTP method, content type or response format), keeping the client-error status and headers
     * (e.g. {@code Allow}) Spring assigns to each of them instead of reporting an unexpected server error.
     *
     * @param ex the framework exception, which carries its own HTTP status
     * @return error response with the exception's status
     */
    @ExceptionHandler({
            NoResourceFoundException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            HttpMediaTypeNotAcceptableException.class
    })
    public ResponseEntity<ErrorResource> handleRejectedRequest(Exception ex) {
        var errorResponse = (ErrorResponse) ex;
        var status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
        var fallbackMessage = MessageResolver.resolveOrDefault(
                "error.http.request-rejected.message", "The request could not be processed");
        var message = MessageResolver.resolveOrDefault(
                "error.http.%s.message".formatted(status.name().toLowerCase(Locale.ROOT).replace('_', '-')),
                fallbackMessage);
        return ResponseEntity.status(status)
                .headers(errorResponse.getHeaders())
                .body(new ErrorResource(status.name(), message, errorResponse.getBody().getDetail()));
    }

    private static ResponseEntity<?> toValidationErrorResponse(String reason) {
        var applicationError = ApplicationError.validationError(
                MessageResolver.resolveOrDefault("validation.request.argument", "request-argument"),
                reason
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles unexpected runtime exceptions not caught by specific handlers.
     * Maps to a generic unexpected error response.
     *
     * @param ex the unhandled runtime exception
     * @return error response with INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        var applicationError = ApplicationError.unexpected(
                MessageResolver.resolveOrDefault("error.unexpected.context", "global-exception-handler"),
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred"
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles all other exceptions not matched by specific handlers.
     * Provides a final fallback for any unexpected exception type.
     *
     * @param ex the exception
     * @return error response with INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
        var applicationError = ApplicationError.unexpected(
                MessageResolver.resolveOrDefault("error.unexpected.context", "global-exception-handler"),
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred"
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }
}
