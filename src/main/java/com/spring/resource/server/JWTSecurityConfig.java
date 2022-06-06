package com.spring.resource.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class JWTSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.authorizeRequests(authz -> authz
						.antMatchers(HttpMethod.GET, "/foos/**").hasAuthority("SCOPE_read")
						.antMatchers(HttpMethod.POST, "/foos").hasAuthority("SCOPE_write")
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt
								.jwkSetUri("http://localhost:9000/oauth2/jwks")
						)
				);
	}
}