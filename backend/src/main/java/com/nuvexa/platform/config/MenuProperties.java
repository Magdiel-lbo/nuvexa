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
        /** Especialidades (nome do enum) que veem o item; vazio = todas. */
        private List<String> especialidades = new ArrayList<>();
        /** Permissões (nome do enum Permissao) que o papel do usuário precisa ter; vazio = qualquer. */
        private List<String> permissoes = new ArrayList<>();
        /** Perfis de plataforma (ADMIN/PROFISSIONAL) que veem o item; vazio = todos. */
        private List<String> perfis = new ArrayList<>();
        private List<Item> children = new ArrayList<>();
    }
}
