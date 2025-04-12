package com.aiswift.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {
	
	@Autowired
	private TenantFilter tenantFilter;
	
	@Autowired
	private JwtFilter jwtFilter;
	
	@Autowired
	private CorsConfigurationSource corsConfig;
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {	
		http
	     // Tenant filter runs before JWT filter
        .addFilterBefore(tenantFilter, CustomUsernamePasswordAuthenticationFilter.class)
		// JWT filter runs after tenant filter but before authentication
        .addFilterBefore(jwtFilter, CustomUsernamePasswordAuthenticationFilter.class)
		.authorizeHttpRequests(
				(requests) -> requests
//				.requestMatchers("/api/login").permitAll()	
				.requestMatchers("/stripe/webhook").permitAll() //allow Stripe.exe
				.requestMatchers("/api/public/**").permitAll()
				.requestMatchers("/api/developer/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
				.requestMatchers("/api/admin/**").hasAnyRole("OWNER", "ADMIN")
				.requestMatchers("/api/owner/**").hasRole("OWNER")
				.requestMatchers("/api/tenant/**").hasAnyRole("STAFF_LEVEL_1", "STAFF_LEVEL_2","ADMIN","SUPERVISOR", "OWNER")
				.anyRequest().authenticated()) 
		// logout is managed on client's side : remove JWT, redirect user to the login page
		//can add server-side validation later (blacklist) .. to stop validate jwt after logout
				.sessionManagement(session -> session
			            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				        )
				.cors(cors -> cors.configurationSource(corsConfig))
				.csrf(csrf -> csrf.disable());
		
		return http.build();		
	}

}
