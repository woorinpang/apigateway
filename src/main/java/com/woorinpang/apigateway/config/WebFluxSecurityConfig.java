package com.woorinpang.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;

import static org.springframework.security.config.Customizer.*;

@Slf4j
@EnableWebFluxSecurity
@Configuration
public class WebFluxSecurityConfig {

    private final static String[] PERMITALL_ANTPATTERNS = {
            ReactiveAuthorization.AUTHORIZATION_URI,
            "/",
            "/csrf",
            "/user-service/login",
            "/?*-service/actuator/?*",
            "/actuator/?*",
            "/?*-service/docs/index.html"
    };

    private final static String USER_JOIN_ANTPATTERNS = "/user-service/auth/join";

    @Bean
    public SecurityWebFilterChain chain(ServerHttpSecurity http, ReactiveAuthorizationManager<AuthorizationContext> check) throws Exception {
//        http
//                    .csrf().disable()
//                    .headers().frameOptions().disable()
//                .and()
//                    .formLogin().disable()
//                    .httpBasic().authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)) //login dialog disabled & 401 HttpStatus return
//                .and()
//                    .authorizeExchange()
//                    .pathMatchers(PERMITALL_ANTPATTERNS).permitAll()
//                    .pathMatchers(HttpMethod.POST, USER_JOIN_ANTPATTERNS).permitAll()
//                    .anyExchange().access(check);
        http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(PERMITALL_ANTPATTERNS).permitAll()
                        .pathMatchers(HttpMethod.POST, USER_JOIN_ANTPATTERNS).permitAll()
                        .pathMatchers(HttpMethod.POST, USER_JOIN_ANTPATTERNS).permitAll()
                        .anyExchange().access(check)
                )
                .httpBasic(withDefaults())
                .formLogin(withDefaults())
        ;
        return http.build();
    }
}
