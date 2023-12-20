package dev.paulosouza.bingo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

@Configuration
@Slf4j
public class ObjectMapperConfig {

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper().registerModules(
				new ProblemModule(),
				new ConstraintViolationProblemModule(),
				new JavaTimeModule()
		)
				.disable(FAIL_ON_UNKNOWN_PROPERTIES);
	}

}
