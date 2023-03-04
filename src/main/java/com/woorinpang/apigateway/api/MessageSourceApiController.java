package com.woorinpang.apigateway.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MessageSourceApiController {

    private final MessageSource messageSource;

    @GetMapping("/api/v1/messages/{code}/{lang}")
    public String getMessage(@PathVariable("code") String code, @PathVariable("lang") String lang) {
        Locale locale = "en".equals(lang) ? Locale.ENGLISH : Locale.KOREAN;
        String message = messageSource.getMessage(code, null, locale);
        log.info("code={}, lang={}, message={}", code, lang, message);
        return message;
    }
}
