package org.fogbowcloud.rendezvous.xmpp;

import org.fogbowcloud.rendezvous.core.Rendezvous;
import org.fogbowcloud.rendezvous.core.RendezvousImpl;
import org.fogbowcloud.rendezvous.xmpp.handler.IAmAliveHandler;
import org.fogbowcloud.rendezvous.xmpp.handler.WhoIsAliveHandler;
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