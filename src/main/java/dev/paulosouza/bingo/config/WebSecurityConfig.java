package dev.paulosouza.bingo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

@Configuration
@Import(SecurityProblemSupport.class)
@RequiredArgsConstructor
public class WebSecurityConfig {

	private final SecurityProblemSupport problemSupport;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(this::authorizationConfig)
				.httpBasic(Customizer.withDefaults())
				.sessionManagement(this::sessionManagement)
				.exceptionHandling(this::exceptionHanding);

		return http.build();
	}

	private void exceptionHanding(ExceptionHandlingConfigurer<HttpSecurity> handling) {
		handling.authenticationEntryPoint(problemSupport)
				.accessDeniedHandler(problemSupport);
	}

	private void sessionManagement(SessionManagementConfigurer<HttpSecurity> session) {
		session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	private void authorizationConfig(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
		auth.requestMatchers(
						"/public/**",
						"/swagger-ui/**",
						"/swagger-resources/**",
						"/v3/api-docs/**",
						"/favicon.ico",
						"/games/bingo/**",
						"/games/admin/bingo/connect/**",
						"/games/stop/**",
						"/games/config/**",
						"/games/watch/**"
				)
				.permitAll()
				.anyRequest()
				.authenticated();
	}

}
