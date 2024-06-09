package io.woorinpang.apigateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class ReactiveAuthorization implements ReactiveAuthorizationManager<AuthorizationContext> {

    @Value("${apigateway.host:http://localhost:8000}")
    private String APIGATEWAY_HOST;

    @Value("${token.secret-key}")
    private String TOKEN_SECRET_KEY;

    public static final String AUTHORIZATION_URI = "/user-service" + "/auth/check";
    public static final String REFRESH_TOKEN_URI = "/user-service" + "/auth/token/refresh";

    /**
     * 요청에 대한 사용자의 권한여부 체크하여 true/false 로 리턴한다. 헤더에 토큰이 있으면 유효성 체크한다.
     */
    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {
        ServerHttpRequest request = context.getExchange().getRequest();
        RequestPath requestPath = request.getPath();
        HttpMethod httpMethod = request.getMethod();

        String baseUrl = APIGATEWAY_HOST + AUTHORIZATION_URI + "?httpMethod=" + httpMethod + "&requestPath=" + requestPath;
        log.info("baseUrl = {}", baseUrl);

        String authorizationHeader = "";

        List<String> authorizations = request.getHeaders().getOrDefault(HttpHeaders.AUTHORIZATION, null);

        if (authorizations != null && !authorizations.isEmpty() &&
                StringUtils.hasLength(authorizations.getFirst()) &&
                !"undefined".equals(authorizations.getFirst())) {

            try {
                authorizationHeader = authorizations.getFirst();
                String jwt = authorizationHeader.replace("Bearer ", "");
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode(TOKEN_SECRET_KEY)))
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody();

                String subject = claims.getSubject();

                //refresh token 요청 시 토큰 검증만 하고 인가 처리 한다.
                if (REFRESH_TOKEN_URI.equals(requestPath + "")) {
                    return Mono.just(new AuthorizationDecision(true));
                }

                if (subject == null || subject.isEmpty()) {
                    log.error("토큰 인증 오류");
                    throw new AuthorizationServiceException("토큰 인증 오류");
                }
            } catch (IllegalArgumentException e) {
                log.error("토큰 헤더 오류 = {}", e.getMessage());
                throw new AuthorizationServiceException("토큰 헤더 오류");
            } catch (ExpiredJwtException e) {
                log.error("토큰 유효기간이 만료되었습니다. = {}", e.getMessage());
                throw new AuthorizationServiceException("토큰 유효기간 만료");
            } catch (Exception e) {
                log.error("토큰 인증 오류 Exception = {}", e.getMessage());
                throw new AuthorizationServiceException("토큰 인증 오류");
            }
        }

        boolean granted = false;
        try {
            String token = authorizationHeader; //Variable used in lambda expression should be final or effectively final
            Mono<Boolean> body = WebClient.create(baseUrl)
                    .get()
                    .headers(httpHeaders -> httpHeaders.add(HttpHeaders.AUTHORIZATION, token))
                    .retrieve()
                    .bodyToMono(Boolean.class);
            granted = body.toFuture().get().booleanValue();
            log.info("Security AuthorizationDecision granted = {}", granted);
        } catch (Exception e) {
            log.error("인가 서버에 요청 중 오류 = {}", e.getMessage());
            throw new AuthorizationServiceException("인가 요청시 오류 발생");
        }
        return Mono.just(new AuthorizationDecision(granted));
    }
}
