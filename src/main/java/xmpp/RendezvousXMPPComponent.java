package xmpp;

import org.jamppa.component.XMPPComponent;
import org.ourgrid.cloud.Rendezvous;
import org.ourgrid.cloud.RendezvousImpl;

import xmpp.handler.IAmAliveHandler;
import xmpp.handler.WhoIsAliveHandler;

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