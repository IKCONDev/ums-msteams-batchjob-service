package com.ikn.ums.msteams.exception;

import java.util.NoSuchElementException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


	/**
	 * EntityNotFoundException handles the exception when the Object is NUll
	 * @param emptyInputException
	 * @return
	 */
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException entityNotFoundException) {
		log.info("GlobalExceptionHandler.handleEntityNotFoundException() ENTERED" + entityNotFoundException.getMessage());
		return new ResponseEntity<String>(entityNotFoundException.getErrorMessage(), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(UserPrincipalNotFoundException.class)
	public ResponseEntity<String> handleUserPrincipalNotFoundException(UserPrincipalNotFoundException userPrincipalNotFoundException) {
		log.info("GlobalExceptionHandler.handleEntityNotFoundException() ENTERED" + userPrincipalNotFoundException.getMessage());
		return new ResponseEntity<String>(userPrincipalNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
	}

	
	@ExceptionHandler(UsersNotFoundException.class)
	public ResponseEntity<String> handleUsersNotFoundException(UsersNotFoundException usersNotFoundException) {
		log.info("GlobalExceptionHandler.handleEntityNotFoundException() ENTERED" + usersNotFoundException.getMessage());
		return new ResponseEntity<String>("Business Exception", HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	/**
	 * The EmptyInputException is a Custom Exception (created by us) written for
	 * handling the Business Scenarios. defined
	 * @param emptyInputException
	 * @return
	 */
	@ExceptionHandler(EmptyInputException.class)
	public ResponseEntity<String> handleEmptyInput(EmptyInputException emptyInputException) {
		log.info("GlobalExceptionHandler.handleEmptyInput() ENTERED" + emptyInputException.getMessage());
		return new ResponseEntity<String>("Input field is empty. Please look into it.", HttpStatus.BAD_REQUEST);
	}

	/**
	 * The NoSuchElementException is a Pre-defined default handler for the
	 * SpringBoot. No class required to be created for pre-defined.
	 * 
	 * @param noSuchElementException
	 * @return
	 */
	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException noSuchElementException) {
		log.info("GlobalExceptionHandler.handleNoSuchElementException() ENTERED" + noSuchElementException.getMessage());
		return new ResponseEntity<String>("No Value is Present in DB.", HttpStatus.NOT_FOUND);
	}

	/**
	 * Handling the Business Exceptions global to reduce boiler plate code
	 * @param noSuchElementException
	 * @return
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<String> handleBusinessException(BusinessException businessException) {
		log.info("GlobalExceptionHandler.handleBusinessException() ENTERED");
		log.info("Business Exception Occurred." + businessException.getMessage());
		return new ResponseEntity<String>("Business Exception.", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Handling the Controller Exceptions global to reduce boiler plate code
	 * @param noSuchElementException
	 * @return
	 */
	@ExceptionHandler(ControllerException.class)
	public ResponseEntity<String> handleControllerException(ControllerException controllerException) {
		log.info("GlobalExceptionHandler.handleControllerException() ENTERED");
		log.info("Controller Exception Occurred" + controllerException.getMessage());
		return new ResponseEntity<String>("Business Exception.", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		log.info("GlobalExceptionHandler.handleEmptyInput() ENTERED" + ex.getMessage());
		return new ResponseEntity<Object>("Please change your http method type.", HttpStatus.NOT_FOUND);
	}

}
