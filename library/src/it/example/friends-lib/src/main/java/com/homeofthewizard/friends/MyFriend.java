package com.homeofthewizard.friends;

public class MyFriend implements MyHelloer{

    private final String name;

    public MyFriend(String name) {
        this.name = name;
    }

    @Override
    public void hello() {
        System.out.println("hello buddy ! It's " + name );
    }
}
