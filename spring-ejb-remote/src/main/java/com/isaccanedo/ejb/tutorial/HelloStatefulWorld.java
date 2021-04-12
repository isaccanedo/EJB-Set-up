package com.isaccanedo.ejb.tutorial;

import javax.ejb.Remote;

@Remote
public interface HelloStatefulWorld {

    int howManyTimes();
    String getHelloWorld();
    
}
