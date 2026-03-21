package com.soonaemyoback.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/admin/login")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/member/stamps")
                        .permitAll()
                        .requestMatchers("/h2-console/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admins")
                        .hasRole("ROOT")
                        .requestMatchers(HttpMethod.DELETE, "/api/admins/**")
                        .hasRole("ROOT")
                        .requestMatchers(HttpMethod.GET, "/api/admins")
                        .hasRole("ROOT")
                        .requestMatchers(HttpMethod.POST, "/api/members")
                        .hasAnyRole("ADMIN", "ROOT")
                        .requestMatchers(HttpMethod.GET, "/api/stamps")
                        .hasAnyRole("ADMIN", "ROOT")
                        .requestMatchers(HttpMethod.POST, "/api/stamps/**")
                        .hasAnyRole("ADMIN", "ROOT")
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) ->
                        res.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.maximumSessions(1).maxSessionsPreventsLogin(false))
                .logout(logout -> logout.logoutUrl("/api/admin/logout")
                        .logoutSuccessHandler((req, res, auth) -> res.setStatus(200))
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"));

        http.headers(h -> h.frameOptions(f -> f.sameOrigin()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
