package com.company.office_admin.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )
            .httpBasic(org.springframework.security.config.Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/login").permitAll()
                
                // 1. Master Admin Only Web Sections
                .requestMatchers("/accounts/**", "/channels/**", "/oauth2/**", "/analytics/**", "/reports/**", "/settings/**").hasRole("ADMIN")
                
                // 2. Sensitive Content & System Actions: Publishing, Approving, and Deleting are strictly Admin Only
                .requestMatchers(HttpMethod.POST, "/content/*/publish", "/content/*/approve", "/content/*/delete").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/dashboard/posts/*/publish", "/api/dashboard/posts/*/approve").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/dashboard/posts/*", "/api/folders/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/folders/*").hasRole("ADMIN")
                
                // 3. Designer Access (Designer Vault)
                .requestMatchers("/api/folders/designer/**").hasAnyRole("ADMIN", "DESIGNER")
                
                // 4. Creator Access (Creator Studio & AI Copilot)
                .requestMatchers("/api/folders/creator/**", "/api/ai/**").hasAnyRole("ADMIN", "CREATOR")
                
                // 5. Common dashboard, calendar, content browsing, and general API endpoints
                .requestMatchers("/", "/dashboard", "/content", "/calendar", "/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/dashboard?denied=true")
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}

