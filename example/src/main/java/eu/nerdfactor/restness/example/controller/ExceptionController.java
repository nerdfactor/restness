package eu.nerdfactor.restness.example.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Controller for exceptions within the application.<br> Turns specific
 * exceptions into matching {@link ResponseEntity ResponseEntitites} with proper
 * http response codes.
 */
@ControllerAdvice
@Controller
public class ExceptionController extends ResponseEntityExceptionHandler implements ErrorController {

	/**
	 * Handler for access violations.
	 *
	 * @param request The current request object.
	 * @return ResponseEntity
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<?> handleAccessDeniedException(HttpServletRequest request, Exception e) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}

	/**
	 * Handler null pointer exceptions.
	 *
	 * @param request The current request object.
	 * @return ResponseEntity
	 */
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<?> handleEntityNotFoundException(HttpServletRequest request, Exception e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	/**
	 * Handler null pointer exceptions.
	 *
	 * @param request The current request object.
	 * @return ResponseEntity
	 */
	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<?> handleNullPointerException(HttpServletRequest request, Exception e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}

	/**
	 * Handler for exceptions in controllers.
	 *
	 * @return ResponseEntity
	 * @throws Exception Throws exception if class has a response status
	 *                   annotation.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> defaultErrorHandler(Exception e) throws Exception {
		if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null) {
			throw e;
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}

	/**
	 * Handler for other exceptions in the application.
	 *
	 * @param request The current request object.
	 * @return ResponseEntity
	 */
	@RequestMapping("/error")
	public ResponseEntity<?> error(HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}
}
