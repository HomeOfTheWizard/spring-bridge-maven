package com.homeofthewizard.plugin;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import com.homeofthewizard.friends.MySpringConfiguration;

@PropertySource("application.properties")
@Import(MySpringConfiguration.class)
public class MyConfiguration {
}