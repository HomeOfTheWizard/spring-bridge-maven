package com.homeofthewizard;

import java.io.IOException;
import java.util.Properties;

import javax.inject.Named;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.guice.module.SpringModule;
import org.springframework.util.StringUtils;

@Named
class PropertyBasedConfigurableSpringModule extends SpringModule {

    private final static String SPRING_EXT = "spring-ext";
    private final static String CONFIG_FILE_PATH = SPRING_EXT + "." + "configFilePath";
    private final static String CONTEXT_CONFIG_CLASS = SPRING_EXT + "." + "contextConfigClass";
    private final static String SPRING_MAVEN_EXTENSION_PROPERTIES = "SpringMavenExtensionProperties";
    private final static String DEFAULT_PROPERTIES_FILE_PATH = SPRING_EXT + ".properties";
    private final static Properties properties = new Properties();

    static {
        loadProperties();
    }

    static void loadProperties() {
        try {
            var path = StringUtils.cleanPath(System.getProperty(CONFIG_FILE_PATH, DEFAULT_PROPERTIES_FILE_PATH));
            var resource = path.equals(DEFAULT_PROPERTIES_FILE_PATH)
                    ? new ClassPathResource(DEFAULT_PROPERTIES_FILE_PATH,
                            PropertyBasedConfigurableSpringModule.class.getClassLoader())
                    : path.contains(":") ? new UrlResource(System.getProperty(CONFIG_FILE_PATH))
                            : new FileSystemResource(path);
            PropertiesLoaderUtils.fillProperties(properties, resource);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public PropertyBasedConfigurableSpringModule()
            throws ClassNotFoundException, IOException {
        super(new SpringApplicationBuilder()
                .sources(loadSpringConfigClass())
                .initializers(context -> context
                        .getEnvironment()
                        .getPropertySources()
                        .addLast(new SpringExtensionPropertySource(SPRING_MAVEN_EXTENSION_PROPERTIES)))
                .run());
    }

    static private Class<?> loadSpringConfigClass()
            throws ClassNotFoundException, IOException {
        var classLoader = PropertyBasedConfigurableSpringModule.class.getClassLoader();
        return classLoader.loadClass(properties.getProperty(CONTEXT_CONFIG_CLASS));
    }

    private static class SpringExtensionPropertySource extends PropertySource<String> {
        public SpringExtensionPropertySource(String name) {
            super(name);
        }

        @Override
        public Object getProperty(String name) {
            return properties.getProperty(name);
        }
    }
}
