package org.fogbowcloud.rendezvous.xmpp;

import org.xmpp.component.ComponentException;

public class Main {

    public static void main(String[] args) throws ComponentException {
        
        
        RendezvousXMPPComponent rendezvousXmppComponent = new RendezvousXMPPComponent(
                "rendezvous.test.com", "password", "127.0.0.1", 5347, 10000);
        
        rendezvousXmppComponent.setDescription("Rendezvous Component");
        rendezvousXmppComponent.setName("rendezvous");
        rendezvousXmppComponent.connect();
        rendezvousXmppComponent.process();
        System.out.println("Após o run");

    }

}
