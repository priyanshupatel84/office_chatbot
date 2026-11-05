package com.office.office_chatbot.config;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.office_chatbot.dto.ApiError;

@Configuration
public class SecurityConfig {
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
		return http.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.anyRequest().permitAll())
				.httpBasic(Customizer.withDefaults())
				.exceptionHandling(errors -> errors
						.authenticationEntryPoint((request, response, exception) -> writeError(objectMapper,
								request, response, HttpStatus.UNAUTHORIZED, "Authentication required"))
						.accessDeniedHandler((request, response, exception) -> writeError(objectMapper,
								request, response, HttpStatus.FORBIDDEN, "Administrator access required")))
				.build();
	}

	@Bean
	UserDetailsService adminUserDetailsService(@Value("${ADMIN_USERNAME:admin}") String username,
			@Value("${ADMIN_PASSWORD:}") String password) {
		if (username.isBlank() || password.isBlank()) {
			throw new IllegalStateException("ADMIN_USERNAME and ADMIN_PASSWORD must be configured");
		}
		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		return new InMemoryUserDetailsManager(User.withUsername(username).password(encoder.encode(password))
				.roles("ADMIN").build());
	}

	private void writeError(ObjectMapper objectMapper, HttpServletRequest request, HttpServletResponse response,
			HttpStatus status, String message) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		if (status == HttpStatus.UNAUTHORIZED) {
			response.setHeader("WWW-Authenticate", "Basic realm=\"CPP Admin API\"");
		}
		objectMapper.writeValue(response.getOutputStream(), new ApiError(Instant.now(), status.value(),
				status.getReasonPhrase(), message, request.getRequestURI()));
	}
}
