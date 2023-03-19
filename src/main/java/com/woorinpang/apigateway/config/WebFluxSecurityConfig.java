package com.woorinpang.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authorization.AuthorizationContext;

@Slf4j
@EnableWebFluxSecurity
@Configuration
public class WebFluxSecurityConfig {

    private final static String[] PERMITALL_ANTPATTERNS = {
            ReactiveAuthorization.AUTHORIZATION_URI, "/", "/csrf",
            "/user-service/login", "/?*-service/api/v1/messages/**", "/api/v1/messages/**",
            "/?*-service/actuator/?*", "/actuator/?*",
            "/v3/api-docs/**", "/?*-service/v3/api-docs", "**/configuration/*", "/swagger*/**", "/webjars/**"
    };

    private final static String USER_JOIN_ANTPATTERNS = "/user-service/api/v1/auth";

    @Bean
    public SecurityWebFilterChain chain(ServerHttpSecurity http, ReactiveAuthorizationManager<AuthorizationContext> check) throws Exception {
        http
                    .csrf().disable()
                    .headers().frameOptions().disable()
                .and()
                    .formLogin().disable()
                    .httpBasic().authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)) //login dialog disabled & 401 HttpStatus return
                .and()
                    .authorizeExchange()
                    .pathMatchers(PERMITALL_ANTPATTERNS).permitAll()
                    .pathMatchers(HttpMethod.POST, USER_JOIN_ANTPATTERNS).permitAll()
                    .anyExchange().access(check);
        return http.build();
    }
}
