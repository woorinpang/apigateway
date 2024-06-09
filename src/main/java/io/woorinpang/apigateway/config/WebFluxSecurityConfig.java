package io.woorinpang.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;

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
            "/login/oauth2/code/google",
            "/login/oauth2/code/kakao",
            "/login/oauth2/code/naver",
            "/user-service/auth/username/exists",
            "/?*-service/actuator/?*",
            "/actuator/?*",
            "/?*-service/docs/index.html",
            "/favicon.ico"
    };

    private final static String USER_JOIN_ANTPATTERNS = "/user-service/auth/join";

    @Bean
    public SecurityWebFilterChain chain(ServerHttpSecurity http, ReactiveAuthorizationManager<AuthorizationContext> check) throws Exception {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
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
