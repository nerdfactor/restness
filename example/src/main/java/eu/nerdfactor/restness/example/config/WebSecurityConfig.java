package eu.nerdfactor.restness.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
		prePostEnabled = true,
		securedEnabled = true,
		jsr250Enabled = true
)
public class WebSecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				.securityMatcher("/error/**");

		return http.build();
	}

	@Bean
	@org.springframework.core.annotation.Order(1)
	public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				.csrf(csrf -> csrf.disable());
		return http.build();
	}
}
