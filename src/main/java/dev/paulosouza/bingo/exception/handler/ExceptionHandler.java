package dev.paulosouza.bingo.exception.handler;

import org.springframework.context.annotation.Bean;
import org.springframework.security.web.firewall.HttpStatusRequestRejectedHandler;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.web.advice.ProblemHandling;

@ControllerAdvice
public class ExceptionHandler implements
		ProblemHandling,
		ForbiddenAdviceTrait,
		RequestRejectedExceptionAdviceTrait,
		IllegalArgumentExceptionAdviceTrait
{

	@Bean
	RequestRejectedHandler requestRejectedHandler() {
		return new HttpStatusRequestRejectedHandler();
	}

}
