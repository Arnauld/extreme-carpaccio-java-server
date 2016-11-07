package fr.arolla.web;

import fr.arolla.command.InvalidCredentialException;
import fr.arolla.command.InvalidParametersException;
import fr.arolla.web.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@ControllerAdvice(basePackageClasses = WebController.class)
public class WebControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidParametersException.class)
    @ResponseBody
    ResponseEntity<?> invalidParameters(HttpServletRequest request, Throwable ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(new ErrorDto("http-" + status.value(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCredentialException.class)
    @ResponseBody
    ResponseEntity<?> invalidCredential(HttpServletRequest request, Throwable ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(new ErrorDto("http-" + status.value(), ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.valueOf(statusCode);
    }

}
