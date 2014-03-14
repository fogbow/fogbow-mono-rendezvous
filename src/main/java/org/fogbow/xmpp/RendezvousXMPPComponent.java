package org.fogbow.xmpp;

import org.fogbow.cloud.Rendezvous;
import org.fogbow.cloud.RendezvousImpl;
import org.fogbow.xmpp.handler.IAmAliveHandler;
import org.fogbow.xmpp.handler.WhoIsAliveHandler;
import org.jamppa.component.XMPPComponent;

public class RendezvousXMPPComponent extends XMPPComponent{
    
    Rendezvous rendezvous;
    
    public RendezvousXMPPComponent(String jid, String password, String server, int port) {
        this(jid, password, server, port, RendezvousImpl.TIMEOUT_DEFAULT);
    }
    
    public RendezvousXMPPComponent(String jid, String password, String server, int port, long timeout) {
        super(jid, password, server, port);
       
        rendezvous = new RendezvousImpl(timeout);
        addGetHandler(new IAmAliveHandler(rendezvous));
        addGetHandler(new WhoIsAliveHandler(rendezvous));
    }
}