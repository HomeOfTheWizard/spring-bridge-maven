package com.homeofthewizard;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.shared.invoker.*;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.env.PropertySource;
import org.springframework.guice.module.SpringModule;

import javax.inject.Named;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Named
class MySpringModule extends SpringModule {

    private final static String PREFIX_SPRING_EXT = "spring-ext";
    private final static String CONFIG_FILE_PATH = PREFIX_SPRING_EXT + "." + "configFilePath";
    private final static String CONTEXT_CONFIG_CLASS = PREFIX_SPRING_EXT + "." + "contextConfigClass";
    private final static String SPRING_MAVEN_EXTENSION_PROPERTIES = "SpringMavenExtensionProperties";
    private final static String DEFAULT_PROPERTIES_FILE_PATH = "/src/main/resources/" + PREFIX_SPRING_EXT + ".properties";
    private final static  String CLASSPATH_FILE = System.getProperty("java.io.tmpdir") + File.separator + "cp.txt";
    private final static Properties properties = new Properties();

    static{
        loadProperties();
    }

    static void loadProperties(){
        try {
            var filePath = Objects.requireNonNullElse(
                    System.getProperty(CONFIG_FILE_PATH),
                    new File(".").getCanonicalPath() + DEFAULT_PROPERTIES_FILE_PATH
            );
            var inStream = new FileInputStream(filePath);
            properties.load(inStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MySpringModule() throws ClassNotFoundException, IOException, MavenInvocationException {
        super(new SpringApplicationBuilder()
                .sources(loadClass())
                .initializers(context -> context
                        .getEnvironment()
                        .getPropertySources()
                        .addLast(new SpringExtensionPropertySource(SPRING_MAVEN_EXTENSION_PROPERTIES))
                )
                .run());
    }

    static private Class<?> loadClass() throws ClassNotFoundException, IOException, MavenInvocationException {

        var pomFile = createDependenciesPom();
        var filePath = createClassPathFile(pomFile);
        var paths = readClassPathFile(filePath);
        var classLoader = updateClassLoaderWithDependenciesClassPath(paths);

        return classLoader.loadClass(properties.getProperty(CONTEXT_CONFIG_CLASS));
    }

    private static ClassLoader updateClassLoaderWithDependenciesClassPath(String[] paths) {
        List<URL> urls = new ArrayList<>();
        try {
            for(var path : paths){
                urls.add(new File(path).toURI().toURL());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        ClassLoader contextClassLoader = URLClassLoader.newInstance(
                urls.toArray(new URL[0]),
                MySpringModule.class.getClassLoader()
        );

        return contextClassLoader;
    }

    private static String[] readClassPathFile(String filePath) throws IOException {
        var cpFile = Path.of(filePath);
        try(var bufferedReader = Files.newBufferedReader(cpFile)) {
            var line = bufferedReader.readLine();
            return line.split(File.pathSeparator);
        }
    }

    private static String createClassPathFile(File pomFile) throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( pomFile );
        request.setGoals( Arrays.asList( "dependency:resolve", "dependency:build-classpath") );
        request.setMavenOpts("-Dmdep.outputFile=" + CLASSPATH_FILE);
        Invoker invoker = new DefaultInvoker();
        invoker.execute( request );
        return CLASSPATH_FILE;
    }

    private static File createDependenciesPom() throws IOException {
        var modelCustom = new Model();
        modelCustom.setModelVersion("4.0.0");
        modelCustom.setGroupId("com.homeofthewizard");
        modelCustom.setArtifactId("spring-extension-dependencies");
        modelCustom.setVersion("1.0.0");

        var dependency = new Dependency();
        dependency.setGroupId(properties.getProperty(PREFIX_SPRING_EXT + ".groupId"));
        dependency.setArtifactId(properties.getProperty(PREFIX_SPRING_EXT + ".artifactId"));
        dependency.setVersion(properties.getProperty(PREFIX_SPRING_EXT + ".version"));
        modelCustom.addDependency(dependency);

        var writer = new MavenXpp3Writer();
        File file = File.createTempFile("pom", ".xml");
        writer.write(new FileOutputStream(file), modelCustom);
        return file;
    }

    private static class SpringExtensionPropertySource extends PropertySource<String>{
        public SpringExtensionPropertySource(String name) {
            super(name);
        }

        @Override
        public Object getProperty(String name) {
            return properties.getProperty(name);
        }
    }
}
