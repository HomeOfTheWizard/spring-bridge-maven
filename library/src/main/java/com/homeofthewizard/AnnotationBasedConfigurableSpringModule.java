package com.homeofthewizard;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.guice.module.SpringModule;

import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Named
class AnnotationBasedConfigurableSpringModule extends SpringModule {

    private static final ClassLoader classLoader = AnnotationBasedConfigurableSpringModule.class.getClassLoader();

    public AnnotationBasedConfigurableSpringModule()
            throws ClassNotFoundException {
        super(new SpringApplicationBuilder()
                .resourceLoader(new DefaultResourceLoader())
                .sources(loadSpringConfigClass())
                .properties(loadApplicationProperties())
                .run());
    }

    static private Properties loadApplicationProperties() {
        var properties = new Properties();
        try(var inputStream = classLoader.getResourceAsStream("application.properties")) {
            if(inputStream != null){
                properties.load(inputStream);
                return properties;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    static private Class<?>[] loadSpringConfigClass()
            throws ClassNotFoundException {
        List<String> names = SpringFactoriesLoader.loadFactoryNames(SpringBootPlugin.class, classLoader);
        List<Class<?>> types = new ArrayList<>();
        for (String name : names) {
            types.add(classLoader.loadClass(name));
        }
        return types.toArray(new Class<?>[0]);
    }

}
