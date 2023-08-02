package com.homeofthewizard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MySpringConfiguration {

    @Bean
    public MyHelloer getFriend(){
        return new MyFriend();
    }
}
