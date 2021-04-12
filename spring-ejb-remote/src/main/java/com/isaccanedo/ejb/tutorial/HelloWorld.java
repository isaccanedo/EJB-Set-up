package com.isaccanedo.ejb.tutorial;

import javax.ejb.Remote;

@Remote
public interface HelloWorld {
    String getHelloWorld();
}
