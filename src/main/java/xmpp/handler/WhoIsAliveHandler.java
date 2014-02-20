package xmpp.handler;

import java.util.List;

import org.dom4j.Element;
import org.jamppa.component.handler.AbstractQueryHandler;
import org.ourgrid.cloud.Rendezvous;
import org.xmpp.packet.IQ;

public class WhoIsAliveHandler extends AbstractQueryHandler {

    final static String NAMESPACE = "whoisalive";
    private Rendezvous rendezvous;

    public WhoIsAliveHandler(Rendezvous rendezvous) {
        super(NAMESPACE);
        this.rendezvous = rendezvous;
    }

    public IQ handle(IQ iq) {
        List<String> aliveIds = rendezvous.whoIsAlive();
        return createResponse(iq, aliveIds);
    }
    
    private IQ createResponse(IQ iq, List<String> aliveIds) {
        IQ resultIQ = IQ.createResultIQ(iq);
        Element queryElement = resultIQ.getElement().addElement("query", NAMESPACE);
        for (String id : aliveIds) {
            Element itemEl = queryElement.addElement("item");
            itemEl.addAttribute("id", id);
        }
        return resultIQ;
    }
}
