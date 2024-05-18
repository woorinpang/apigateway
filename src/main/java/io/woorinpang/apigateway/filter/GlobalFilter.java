package io.woorinpang.apigateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Component
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config> {
    @Value("${token.secret-key}")
    private String TOKEN_SECRET_KEY;

    private final ObjectMapper objectMapper;

    public GlobalFilter(ObjectMapper objectMapper) {
        super(Config.class);
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        //pre filter
        return ((exchange, chain) -> {
            //Netty 비동기 방식 서버 사용시에는 ServerHttpRequest 를 사용해야 한다.
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Global baseMessage: {}", config.getBaseMessage());

            String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (hasText(authorizationHeader)) {
                String jwt = authorizationHeader.replace("Bearer ", "");
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode(TOKEN_SECRET_KEY)))
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody();
                String user = (String) claims.get("user");
                String encodedJson = URLEncoder.encode(user, StandardCharsets.UTF_8);
                request.mutate().header("Authenticated-User", encodedJson).build();
            }

            if (config.isPostLogger()) {
                log.info("[GlobalFilter Start] request ID: {}, method: {}, pth: {}", request.getId(), request.getMethod(), request.getPath());
            }

            //post filter
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if (config.isPostLogger()) {
                    log.info("[GlobalFilter End] request ID: {}, method: {}, path: {}, statusCode: {}", request.getId(), request.getMethod(), request.getPath(), response.getStatusCode());
                }
            }));
        });
    }

    @Data
    public static class Config {
        //put the configure
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}
