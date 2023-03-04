package com.woorinpang.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class MessagesDirectory {

    @Value("${messages.directory}")
    private String messagesDirectory;

    @Value("${spring.profiles.active:default}")
    private String profile;

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        final String MESSAGES = "/messages";
        if ("default".equals(profile)) {
            Path fileStorageLocation = Paths.get(messagesDirectory).toAbsolutePath().normalize();
            String dbMessages = StringUtils.cleanPath("file://" + fileStorageLocation + MESSAGES);
            log.info("DB MessageSource location = {}", dbMessages);
            messageSource.setBasenames(dbMessages);
        } else {
            messageSource.setBasenames(messagesDirectory + MESSAGES);
        }
        messageSource.getBasenameSet().forEach(s -> log.info("messageSource getBasenameSet = {}", s));

        messageSource.setCacheSeconds(60); //메시지 파일 변경 감지 간격
        messageSource.setUseCodeAsDefaultMessage(true); //메시지가 없으면 코드를 메세지로 리턴한다.
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        return messageSource;
    }
}
