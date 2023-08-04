package com.homeofthewizard.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import com.homeofthewizard.friends.MyFriend;

import javax.inject.Inject;

@Mojo(name = "run", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class MyMojo extends AbstractMojo {

    private final MyFriend myFriend;

    @Inject
    public MyMojo(MyFriend myFriend) {
        this.myFriend = myFriend;
    }

    public void execute() {
        myFriend.hello();
    }

}
