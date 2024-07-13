package com.homeofthewizard.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import com.homeofthewizard.SpringBootPlugin;
import com.homeofthewizard.friends.MyHelloer;
import com.homeofthewizard.friends.MySpringConfiguration;

import javax.inject.Inject;

@Mojo(name = "run", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
@SpringBootPlugin(MySpringConfiguration.class)
public class MyMojo extends AbstractMojo {

    private final MyHelloer myFriend;

    @Inject
    public MyMojo(MyHelloer myFriend) {
        this.myFriend = myFriend;
    }

    public void execute() {
        myFriend.hello();
    }

}
