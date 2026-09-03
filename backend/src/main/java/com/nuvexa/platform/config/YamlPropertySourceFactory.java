package com.nuvexa.platform.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Permite {@code @PropertySource} carregar arquivos YAML (Spring só suporta
 * {@code .properties} nativamente); usado para vincular {@code menu.yml} a
 * {@code @ConfigurationProperties} sem misturar com {@code application.yml}.
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());

        Properties properties = factory.getObject();
        String sourceName = resource.getResource().getFilename();
        return new PropertiesPropertySource(sourceName != null ? sourceName : name, properties);
    }
}
