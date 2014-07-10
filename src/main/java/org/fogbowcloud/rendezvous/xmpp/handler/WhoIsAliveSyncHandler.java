package org.fogbowcloud.rendezvous.xmpp.handler;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.Rendezvous;
import org.fogbowcloud.rendezvous.core.RendezvousImpl;
import org.fogbowcloud.rendezvous.core.RendezvousItem;
import org.fogbowcloud.rendezvous.core.model.Flavor;
import org.jamppa.component.handler.AbstractQueryHandler;
import org.xmpp.packet.IQ;

public class WhoIsAliveSyncHandler extends AbstractQueryHandler {

	private final static String WHOISALIVESYNC_NAMESPACE = "http://fogbowcloud.org/rendezvous/synch/whoisalive";
	private Rendezvous rendezvous;

	public WhoIsAliveSyncHandler(Rendezvous rendezvous) {
		super(WHOISALIVESYNC_NAMESPACE);
		this.rendezvous = rendezvous;
	}

	@Override
	public IQ handle(IQ iq) {		
		Set<String> neighbors = ((RendezvousImpl) rendezvous).getNeighborIds();
		ConcurrentHashMap<String, RendezvousItem> managersAlive = ((RendezvousImpl) rendezvous)
	 			.getManagersAlive();
 		IQ response = IQ.createResultIQ(iq);
		Element queryElement = response.getElement().addElement("query",
				WHOISALIVESYNC_NAMESPACE);
		Element neighborsEl = queryElement.addElement("neighbors");
		for (Iterator<String> iterator = neighbors.iterator(); iterator
				.hasNext();) {
			String neighbor = iterator.next();
			Element neighborEl = neighborsEl.addElement("neighbor");
			neighborEl.addElement("id").setText(neighbor);
		}

		Element managersEl = queryElement.addElement("managers");
		for (Entry<String, RendezvousItem> entryitem: managersAlive.entrySet()) {
			RendezvousItem item = entryitem.getValue();
			Element managerEl = managersEl.addElement("manager");
			managerEl.addAttribute("id", item.getResourcesInfo().getId());
			managerEl.addElement("cert").setText(
					item.getResourcesInfo().getCert());
			Element statusEl = managerEl.addElement("status");
			statusEl.addElement("cpu-idle").setText(
					item.getResourcesInfo().getCpuIdle());
			statusEl.addElement("cpu-inuse").setText(
					item.getResourcesInfo().getCpuInUse());
			statusEl.addElement("mem-idle").setText(
					item.getResourcesInfo().getMemIdle());
			statusEl.addElement("mem-inuse").setText(
					item.getResourcesInfo().getMemInUse());
			for (Flavor flavor : item.getResourcesInfo().getFlavours()) {
				Element flavorElement = statusEl.addElement("flavor");
				flavorElement.addElement("name").setText(flavor.getName());
				flavorElement.addElement("cpu").setText(flavor.getCpu());
				flavorElement.addElement("mem").setText(flavor.getMem());
				flavorElement.addElement("capacity").setText(
						flavor.getCapacity().toString());
			}
			statusEl.addElement("updated").setText(item.getFormattedTime());

		}
		
		if(!neighbors.contains(iq.getFrom())) {
			neighbors.add(iq.getFrom().toFullJID());
		}
		
		return response;
	}

}
