package com.homeofthewizard;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.guice.module.SpringModule;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named
class AnnotationBasedConfigurableSpringModule extends SpringModule {

    public AnnotationBasedConfigurableSpringModule()
            throws ClassNotFoundException {
        super(new SpringApplicationBuilder()
                .resourceLoader(new DefaultResourceLoader(AnnotationBasedConfigurableSpringModule.class.getClassLoader()))
                .sources(loadSpringConfigClass())
                .run());
    }

    static private Class<?>[] loadSpringConfigClass()
            throws ClassNotFoundException {
        var classLoader = AnnotationBasedConfigurableSpringModule.class.getClassLoader();
        List<String> names = SpringFactoriesLoader.loadFactoryNames(SpringBootPlugin.class, classLoader);
        List<Class<?>> types = new ArrayList<>();
        for (String name : names) {
            types.add(classLoader.loadClass(name));
        }
        return types.toArray(new Class<?>[0]);
    }

}
