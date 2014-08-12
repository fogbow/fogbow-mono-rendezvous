package org.fogbowcloud.rendezvous.xmpp.handler;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.Rendezvous;
import org.fogbowcloud.rendezvous.core.RendezvousImpl;
import org.fogbowcloud.rendezvous.core.RendezvousItem;
import org.fogbowcloud.rendezvous.core.model.Flavor;
import org.fogbowcloud.rendezvous.xmpp.util.RSM;
import org.fogbowcloud.rendezvous.xmpp.util.RSMElement;
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
		RendezvousImpl rendezvousImpl = (RendezvousImpl) rendezvous;
		int maxNeighbors = rendezvousImpl.getMaxWhoisaliveSyncNeighborCount();

		Element queryEl = iq.getElement().element("query");
		Element neighborsEl = queryEl.element("neighbors");
		RSM neighborsRSM = RSM.parse(neighborsEl, maxNeighbors);

		Element managersEl = queryEl.element("managers");
		int maxManagers = rendezvousImpl.getMaxWhoisaliveSyncNeighborCount();
		RSM managersRSM = RSM.parse(managersEl, maxManagers);

		Set<String> neighbors = rendezvousImpl.getNeighborIds();
		List<RSMElement> neighborsList = new LinkedList<RSMElement>();
		for (final String neighbor : neighbors) {
			neighborsList.add(new RSMElement() {
				@Override
				public String getId() {
					return neighbor;
				}
			});
		}
		neighbors.add(iq.getFrom().toBareJID());

		Map<String, RendezvousItem> managersAlive = rendezvousImpl
				.getManagersAlive();
		List<RendezvousItem> managersList = new LinkedList<RendezvousItem>(
				managersAlive.values());
		return createResponse(iq, rendezvousImpl, neighborsList, managersList,
				neighborsRSM, managersRSM);
	}

	@SuppressWarnings("unchecked")
	private IQ createResponse(IQ iq, RendezvousImpl rendezvousImpl,
			List<RSMElement> neighbors, List<RendezvousItem> managers,
			RSM neighborsRsm, RSM managersRsm) {
		IQ response = IQ.createResultIQ(iq);
		Element queryElement = response.getElement().addElement("query",
				WHOISALIVESYNC_NAMESPACE);

		Element neighborsEl = queryElement.addElement("neighbors");
		List<RSMElement> filteredNeighbors = (List<RSMElement>) neighborsRsm
				.filter(neighbors);
		for (RSMElement neighbor : filteredNeighbors) {
			Element neighborEl = neighborsEl.addElement("neighbor");
			neighborEl.addElement("id").setText(neighbor.getId());
		}
		neighborsRsm.appendSetElements(queryElement.element("neighbors"),
				filteredNeighbors);

		Element managersEl = queryElement.addElement("managers");
		List<RSMElement> filteredManagers = (List<RSMElement>) managersRsm
				.filter(managers);
		for (RSMElement rsmItem : filteredManagers) {
			RendezvousItem item = (RendezvousItem) rsmItem;
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
		managersRsm.appendSetElements(queryElement.element("managers"),
				filteredManagers);
		return response;
	}
}
