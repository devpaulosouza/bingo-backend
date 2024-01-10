package dev.paulosouza.bingo.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import org.zalando.problem.spring.common.AdviceTrait;

import java.net.URI;

public interface ForbiddenAdviceTrait extends AdviceTrait {

	Logger log = LoggerFactory.getLogger(ForbiddenAdviceTrait.class);

	URI TYPE = URI.create("https://company.seurole.com.br/forbidden");

	@ExceptionHandler
	default ResponseEntity<Problem> handleException(
			final InsufficientAuthenticationException exception,
			final NativeWebRequest request
	) {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request.getNativeRequest();

		log.warn("Access denied {}", httpServletRequest.getRequestURL());

		final ThrowableProblem problem = prepare(exception, Status.FORBIDDEN, TYPE).build();

		return new ResponseEntity<>(problem, HttpStatus.FORBIDDEN);
	}

}