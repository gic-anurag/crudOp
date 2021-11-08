package com.fadv.cspi.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fadv.cspi.pojo.ResponseStatusPOJO;

@ControllerAdvice
public class ExceptionHandlerControllerAdvice {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ResponseStatusPOJO> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		return new ResponseEntity<>(
				new ResponseStatusPOJO(false, "Field validation failed",
						e.getBindingResult().getFieldErrors().stream()
								.collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage))),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ResponseStatusPOJO> onBindException(BindException e) {
		return new ResponseEntity<>(
				new ResponseStatusPOJO(false, "Field binding failed",
						e.getBindingResult().getFieldErrors().stream()
								.collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage))),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ResponseStatusPOJO> onMissingRequestHeaderException(MissingRequestHeaderException e) {
		Map<String, String> errorMap = new HashMap<>();
		errorMap.put(e.getHeaderName(), e.getMessage());
		return new ResponseEntity<>(new ResponseStatusPOJO(false, "Header field missing", errorMap),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ResponseStatusPOJO> onException(Exception e) {
		return new ResponseEntity<>(new ResponseStatusPOJO(false, e.getMessage(), "Some error occurred"),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
