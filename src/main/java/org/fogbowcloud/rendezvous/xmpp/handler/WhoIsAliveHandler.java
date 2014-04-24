package org.fogbowcloud.rendezvous.xmpp.handler;

import java.util.List;

import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.Rendezvous;
import org.fogbowcloud.rendezvous.core.RendezvousItem;
import org.fogbowcloud.rendezvous.core.model.Flavor;
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
        for (RendezvousItem rendezvousItem : aliveIds) {
            Element itemEl = queryElement.addElement("item");
            itemEl.addAttribute("id", rendezvousItem.getResourcesInfo().getId());
            
            Element statusEl = itemEl.addElement("status");
            statusEl.addElement("cpu-idle").setText(
                    rendezvousItem.getResourcesInfo().getCpuIdle());
            statusEl.addElement("cpu-inuse").setText(
                    rendezvousItem.getResourcesInfo().getCpuInUse());
            statusEl.addElement("mem-idle").setText(
                    rendezvousItem.getResourcesInfo().getMemIdle());
            statusEl.addElement("mem-inuse").setText(
                    rendezvousItem.getResourcesInfo().getMemInUse());    
            
            List<Flavor> flavours = rendezvousItem.getResourcesInfo()
					.getFlavours();
			for (Flavor f : flavours) {
				Element flavorElement = statusEl.addElement("flavor");
				flavorElement.addElement("name").setText(f.getName());
				flavorElement.addElement("cpu").setText(f.getCpu());
				flavorElement.addElement("mem").setText(f.getMem());
				flavorElement.addElement("capacity").setText(
						f.getCapacity().toString());
			}
			statusEl.addElement("cert");
			statusEl.addElement("updated").setText(
					String.valueOf(rendezvousItem.getFormattedTime()));
        }
        return resultIQ;
    }
}
