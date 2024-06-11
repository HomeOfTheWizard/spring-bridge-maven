package com.homeofthewizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.guice.module.SpringModule;

@Named
class PropertyBasedConfigurableSpringModule extends SpringModule {

    public PropertyBasedConfigurableSpringModule()
            throws ClassNotFoundException, IOException {
        super(new SpringApplicationBuilder()
                .resourceLoader(new DefaultResourceLoader(PropertyBasedConfigurableSpringModule.class.getClassLoader()))
                .sources(loadSpringConfigClass())
                .run());
    }

    static private Class<?>[] loadSpringConfigClass()
            throws ClassNotFoundException {
        var classLoader = PropertyBasedConfigurableSpringModule.class.getClassLoader();
        List<String> names = SpringFactoriesLoader.loadFactoryNames(SpringBootPlugin.class, classLoader);
        List<Class<?>> types = new ArrayList<>();
        for (String name : names) {
            types.add(classLoader.loadClass(name));
        }
        return types.toArray(new Class<?>[0]);
    }

}
