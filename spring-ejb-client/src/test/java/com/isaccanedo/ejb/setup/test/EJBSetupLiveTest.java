package com.isaccanedo.ejb.setup.test;

import com.isaccanedo.ejb.client.EJBClient;
import com.isaccanedo.ejb.tutorial.HelloWorldBean;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * This Live Test requires:
 * * run the `spring-ejb-remote` module with the following command: `mvn clean package cargo:run -Pwildfly-standalone`
 *
 */
public class EJBSetupLiveTest {

    @Test
    public void EJBClientTest() {
        EJBClient ejbClient = new EJBClient();
        HelloWorldBean bean = new HelloWorldBean();
        assertEquals(bean.getHelloWorld(), ejbClient.getEJBRemoteMessage());
    }

}
