package com.office.office_chatbot.exception;

import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.office.office_chatbot.dto.ApiError;

@RestControllerAdvice
public class ApiExceptionHandler {
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> invalidRequest(MethodArgumentNotValidException exception, HttpServletRequest request) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.findFirst().map(error -> error.getField() + " " + error.getDefaultMessage())
				.orElse("Request validation failed");
		return error(HttpStatus.BAD_REQUEST, message, request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> unreadableRequest(HttpMessageNotReadableException exception, HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, "Request body is malformed or contains an invalid value", request);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> invalidParameter(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, "A path or query parameter has an invalid value", request);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> invalidArgument(IllegalArgumentException exception, HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiError> notFound(ResourceNotFoundException exception, HttpServletRequest request) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler(KnowledgeFileException.class)
	public ResponseEntity<ApiError> invalidKnowledgeFile(KnowledgeFileException exception, HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	@ExceptionHandler(OllamaException.class)
	public ResponseEntity<ApiError> ollamaUnavailable(OllamaException exception, HttpServletRequest request) {
		return error(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request);
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ApiError> databaseUnavailable(DataAccessException exception, HttpServletRequest request) {
		return error(HttpStatus.SERVICE_UNAVAILABLE, "Database operation failed", request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> unexpected(Exception exception, HttpServletRequest request) {
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred", request);
	}

	private ResponseEntity<ApiError> error(HttpStatus status, String message, HttpServletRequest request) {
		return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(),
				message, request.getRequestURI()));
	}
}