# demo-spring-maven-extension

This project is a POC for a maven extension that allows injecting Spring Beans into maven's DI system, [Sisu](https://eclipse.dev/sisu/).   
It is intended to help maven plugin developments by allowing the usage of spring libraries.  
It uses [Spring-Guice](https://github.com/spring-projects/spring-guice) to brigde the two systems, and allows the usage of spring beans via [JSR-330](https://maven.apache.org/maven-jsr330.html) annotations within the plugin in development.   
To see how to use JSR-330 annotations, this [wiki](https://github.com/eclipse/sisu.plexus/wiki/Plexus-to-JSR330) is a good start.  
:warning: Beware, there is a faulty [declaration](https://github.com/eclipse/sisu.plexus/issues/35) on this wiki about plugins. Hence the usage of an extension instead of a plugin to do this bridge.  

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
You should add this extension in the `extensions.xml` file located in your maven config folder `.mvn`. Like below.    
```xml
<extensions xmlns="http://maven.apache.org/EXTENSIONS/1.0.0"...>
	<extension>
		<groupId>com.homeofthewizard</groupId>
		<artifactId>demo-spring-maven-extension</artifactId>
		<version>1.0-SNAPSHOT</version>
	</extension>
</extensions>
```
You can see how to use extensions in more details [here](https://maven.apache.org/guides/mini/guide-using-extensions.html).  
  
#### 2. Add a configuration file
Create one like below  
```properties
spring-ext.groupId=com.homeofthewizard
spring-ext.artifactId=friends-lib
spring-ext.version=1.0-SNAPSHOT
spring-ext.contextConfigClass=com.homeofthewizard.friends.MySpringConfiguration

friend.name=Bob
```

As you can guess, the first tree properties are for defining the spring library to be used.   
The forth is the spring configuration class that will be used to initialise the spring context, and create the beans we need.  
:warning: pay attention to the prefix used for those properties.  
The rest of the properties are specific to the spring library we use. Here our library takes a property as parameter to create our `Friend` Bean.  

Place it under `src/main/resources` folder. this is the default place the extension will look up.    
You can change the path if you want, but you will need to precise the path via a system variable when running maven commands.
```shell
mvn .... -Dspring-ext.configFilePath=/home/user/workspace/myPlugin/spring-ext.properties
```
  
#### 3. Add the extension as a dependency of your plugin
This is necessary for classpath sharing.  
In your plugin's `pom.xml`  
```xml
    ...
    <dependencies>
        ...
        <!-- the extension -->
        <dependency>
            <groupId>com.homeofthewizard</groupId>
            <artifactId>demo-spring-maven-extension</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <!-- our spring library -->
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
