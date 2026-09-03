package com.nuvexa.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;
import java.util.List;

/**
 * Fonte de configuração da estrutura de navegação do app, vinda de {@code menu.yml} (não de
 * {@code application.yml}) para manter o menu editável sem tocar em config de infraestrutura.
 */
@Configuration
@ConfigurationProperties(prefix = "menu")
@PropertySource(value = "classpath:menu.yml", factory = YamlPropertySourceFactory.class)
@Getter
@Setter
public class MenuProperties {

    private List<Item> itens = new ArrayList<>();

    @Getter
    @Setter
    public static class Item {
        private String id;
        private String labelKey;
        private String icon;
        private String route;
        private List<Item> children = new ArrayList<>();
    }
}
