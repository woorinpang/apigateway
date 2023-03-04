package com.woorinpang.apigateway.exception;

import com.woorinpang.apigateway.exception.dto.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Configuration
public class ReactiveExceptionHandlerConfig {

    private final MessageSource messageSource;

    public ReactiveExceptionHandlerConfig(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 에러 발생 시 에러 정보 중 필요한 내용만 반환한다.
     */
    @Bean
    public ErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
                Map<String, Object> defaultMap = super.getErrorAttributes(request, options);
                Map<String, Object> errorAttributes = new LinkedHashMap<>();

                int status = (int) defaultMap.get("status");
                ErrorCode errorCode = getErrorCode(status);
                String message = messageSource.getMessage(errorCode.getMessage(), null, LocaleContextHolder.getLocale());
                errorAttributes.put("timestamp", LocalDateTime.now());
                errorAttributes.put("message", message);
                errorAttributes.put("status", status);
                errorAttributes.put("code", errorCode.getCode());
                // API Gateway 에서 FieldError 는 처리하지 않는다.

                log.error("getErrorAttributes = {}", defaultMap);
                return errorAttributes;

            }
        };
    }

    /**
     * 상태코드로부터 ErrorCode 를 매핑하여 반환한다.
     */
    private ErrorCode getErrorCode(int status) {
        return switch (status) {
            case 400 -> ErrorCode.ENTITY_NOT_FOUND;
            case 401 -> ErrorCode.UNAUTHORIZED;
            case 403 -> ErrorCode.ACCESS_DENIED;
            case 404 -> ErrorCode.NOT_FOUND;
            case 405 -> ErrorCode.METHOD_NOT_ALLOWED;
            case 422 -> ErrorCode.UNPROCESSABLE_ENTITY;
            default -> ErrorCode.INTERNAL_SERVER_ERROR;
        };
    }
}
