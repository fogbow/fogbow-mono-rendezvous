package xmpp.handler;

import org.jamppa.component.handler.AbstractQueryHandler;
import org.ourgrid.cloud.Rendezvous;
import org.xmpp.packet.IQ;

public class IAmAliveHandler extends AbstractQueryHandler {

    final static String NAMESPACE = "iamalive";
    Rendezvous rendezvous;
    
    public IAmAliveHandler(Rendezvous rendezvous) {
        super(NAMESPACE);
        this.rendezvous = rendezvous;
    }
    
    public IQ handle(IQ iq) {
        String id = iq.getFrom().toBareJID();
        rendezvous.iAmAlive(id);
        IQ response = IQ.createResultIQ(iq);
        return response;
    }    
}
