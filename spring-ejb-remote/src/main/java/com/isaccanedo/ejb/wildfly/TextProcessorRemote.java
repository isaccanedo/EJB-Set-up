package com.isaccanedo.ejb.wildfly;

import javax.ejb.Remote;

@Remote
public interface TextProcessorRemote {

    String processText(String text);
}