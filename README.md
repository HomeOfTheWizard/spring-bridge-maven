# spring-bridge-maven

This project is library that allows injecting Spring Beans into maven's DI system, [Sisu](https://eclipse.dev/sisu/).   
It is intended to help maven plugin developments by allowing the usage of spring libraries.  
It uses [Spring-Guice](https://github.com/spring-projects/spring-guice) to brigde the two systems, and allows the usage of spring beans via [JSR-330](https://maven.apache.org/maven-jsr330.html) annotations within the plugin in development.   
To see how to use JSR-330 annotations, this [documentation](https://eclipse-sisu.github.io/sisu-project/plexus/index.html) is a good start.  
:warning: Beware, there is an important [declaration](https://eclipse-sisu.github.io/sisu-project/plexus/index.html#custombinding) on this wiki about plugins. Hence the usage of an extension instead of a plugin to do this bridge.  

## How to use it ?

Let's say you are building a plugin like below
```java

@Mojo(name = "hello")
public class MyMojo extends AbstractMojo {

    @Inject
    private final MyHelloer myFriend;
    
    public void execute() {
        myFriend.hello();
    }

}
```

You want to use a class that implements Helloer interface, coming from a spring library.

#### 1. Add the extension
You should add this extension in the `extensions.xml` file located in your maven config folder `.mvn` of your application (the one that uses the custom plugin). Like below.    
```xml
<extensions xmlns="http://maven.apache.org/EXTENSIONS/1.0.0"...>
	<extension>
		<groupId>com.homeofthewizard</groupId>
		<artifactId>guice-exporter-maven-extension</artifactId>
		<version>1.0-SNAPSHOT</version>
	</extension>
</extensions>
```
You can see how to use extensions in more details [here](https://maven.apache.org/guides/mini/guide-using-extensions.html).  
  
#### 2. Add an annotation
Somewhere in your plugin add `@SpringBootPlugin` pointing to the main configuration class of your spring library. As you can guess, this defines the configuration sources for the the spring application context to be used in the plugin. You can specify a class with `@EnableAutoConfiguration` annotation, or just a plain `@Configuration` class. There is an APT processor in the library that writes these out to `spring.factories` for use at runtime.

#### 3. Add a configuration file

Also create an `application.properties` file like below
```properties
friend.name=Bob
```
Here our library takes a property as parameter to create our `Friend` Bean.  

Place this under `src/main/resources` folder. This is the default place Spring Boot will look it up as normal.

#### 4. Add the extension as a dependency of your plugin
This is necessary for classpath sharing.  
In your plugin's `pom.xml`  
```xml
    ...
    <dependencies>
        ...
        <!-- the bridge library -->
        <dependency>
            <groupId>com.homeofthewizard</groupId>
            <artifactId>spring-bridge-maven</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <!-- your spring library -->
        <dependency>
            <groupId>com.people</groupId>
            <artifactId>friends-lib</artifactId>
            <version>1.0</version>
        </dependency>
        ...
    </dependencies>
    ...
```
Now you can build your plugin.

#### **That is it!** You are good to go :rocket:   
When you run your plugin, the Bean will be automatically injected into it.  
