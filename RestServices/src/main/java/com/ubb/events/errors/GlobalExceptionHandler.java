package com.ubb.events.errors;

import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.ForbiddenException;
import com.ubb.events.common.errors.InvalidCredentialsException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.common.errors.ValidationException;
import com.ubb.events.dto.ErrorDetail;
import com.ubb.events.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessRuleException.class)
    @ApiResponse(responseCode = "400", description = "Business rule violation",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<ErrorResponse> business(BusinessRuleException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), exception.getErrors());
    }

    @ExceptionHandler(NotFoundException.class)
    @ApiResponse(responseCode = "404", description = "Resource not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<ErrorResponse> notFound(NotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), exception.getErrors());
    }

    @ExceptionHandler(ConflictException.class)
    @ApiResponse(responseCode = "409", description = "Resource state conflict",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<ErrorResponse> conflict(ConflictException exception) {
        return response(HttpStatus.CONFLICT, exception.getMessage(), exception.getErrors());
    }

    @ExceptionHandler(ValidationException.class)
    @ApiResponse(responseCode = "400", description = "Request validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<ErrorResponse> validation(ValidationException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), exception.getErrors());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ApiResponse(responseCode = "403", description = "Action forbidden",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<ErrorResponse> forbidden(ForbiddenException exception) {
        return response(HttpStatus.FORBIDDEN, exception.getMessage(), exception.getErrors());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<ErrorResponse> invalidCredentials(InvalidCredentialsException exception) {
        return response(HttpStatus.UNAUTHORIZED, exception.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ApiResponse(responseCode = "400", description = "Request validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<ErrorResponse> methodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<ErrorDetail> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "Request validation failed", details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ApiResponse(responseCode = "400", description = "Request validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<ErrorResponse> constraintViolation(ConstraintViolationException exception) {
        List<ErrorDetail> details = exception.getConstraintViolations().stream()
                .map(error -> new ErrorDetail(error.getPropertyPath().toString(), error.getMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "Request validation failed", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ApiResponse(responseCode = "400", description = "Malformed request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<ErrorResponse> malformedRequest(HttpMessageNotReadableException exception) {
        return response(HttpStatus.BAD_REQUEST, "Malformed request", List.of());
    }

    @ExceptionHandler(Exception.class)
    @ApiResponse(responseCode = "500", description = "Unexpected server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<ErrorResponse> unexpected(Exception exception) {
        LOGGER.error("Unhandled exception while processing request", exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", List.of());
    }

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String message, List<ErrorDetail> errors) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message, errors == null ? List.of() : errors));
    }
}
