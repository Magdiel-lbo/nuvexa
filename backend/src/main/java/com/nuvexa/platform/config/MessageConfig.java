package com.nuvexa.platform.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Locale;

@Configuration
public class MessageConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("mensagens/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    /**
     * Locale fixo em pt-BR (único suportado hoje) carregado aqui uma vez, em vez de cada service
     * repetir {@code Locale.of("pt", "BR")} e passá-lo em toda chamada de
     * {@code messageSource.getMessage(...)}.
     */
    @Bean
    public MessageSourceAccessor mensagens(MessageSource messageSource) {
        return new MessageSourceAccessor(messageSource, Locale.of("pt", "BR"));
    }

    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        factoryBean.setValidationMessageSource(messageSource);
        return factoryBean;
    }
}
