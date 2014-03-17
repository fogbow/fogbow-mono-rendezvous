package org.fogbowcloud.rendezvous.xmpp.handler;

import java.util.List;

import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.Rendezvous;
import org.fogbowcloud.rendezvous.core.RendezvousItem;
import org.jamppa.component.handler.AbstractQueryHandler;
import org.xmpp.packet.IQ;

public class WhoIsAliveHandler extends AbstractQueryHandler {

    private final static String NAMESPACE = "http://fogbowcloud.org/rendezvous/whoisalive";
    private Rendezvous rendezvous;

    public WhoIsAliveHandler(Rendezvous rendezvous) {
        super(NAMESPACE);
        this.rendezvous = rendezvous;
    }

    public IQ handle(IQ iq) {
        List<RendezvousItem> aliveIds = rendezvous.whoIsAlive();
        return createResponse(iq, aliveIds);
    }

    private IQ createResponse(IQ iq, List<RendezvousItem> aliveIds) {
        IQ resultIQ = IQ.createResultIQ(iq);

        Element queryElement = resultIQ.getElement().addElement("query",
                NAMESPACE);
        for (RendezvousItem rendezvouItem : aliveIds) {
            Element itemEl = queryElement.addElement("item");
            itemEl.addAttribute("id", rendezvouItem.getResourcesInfo().getId());
            
            Element statusEl = itemEl.addElement("status");
            statusEl.addElement("cpu-idle").setText(
                    rendezvouItem.getResourcesInfo().getCpuIdle());
            statusEl.addElement("cpu-inuse").setText(
                    rendezvouItem.getResourcesInfo().getCpuInUse());
            statusEl.addElement("mem-idle").setText(
                    rendezvouItem.getResourcesInfo().getMemIdle());
            statusEl.addElement("mem-inuse").setText(
                    rendezvouItem.getResourcesInfo().getMemInUse());           
            statusEl.addElement("updated").setText(
                    String.valueOf(rendezvouItem.getFormattedTime())); 
        }
        return resultIQ;
    }
}
