package com.homeofthewizard.friends;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MySpringConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Bean
    public MyHelloer getFriend(){
        return new MyFriend(getProperty("friend.name"));
    }

    private String getProperty(String propertyKey){
        return this.applicationContext.getEnvironment().getProperty(propertyKey);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
