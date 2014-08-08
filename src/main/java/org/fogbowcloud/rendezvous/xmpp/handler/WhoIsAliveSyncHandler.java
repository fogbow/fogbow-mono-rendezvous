package org.fogbowcloud.rendezvous.xmpp.handler;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

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
		RendezvousImpl rendezvousImpl = (RendezvousImpl) rendezvous;

		Set<String> neighbors = rendezvousImpl.getNeighborIds();
		String[] orderedNeighbors = (String[]) neighbors
				.toArray(new String[neighbors.size()]);
		Arrays.sort(orderedNeighbors);
		neighbors.add(iq.getFrom().toBareJID());

		Map<String, RendezvousItem> managersAlive = rendezvousImpl
				.getManagersAlive();
		RendezvousItem[] orderedManagers = (RendezvousItem[]) managersAlive
				.values().toArray(new RendezvousItem[managersAlive.size()]);
		Arrays.sort(orderedManagers);
		int maxNeighbors = Math.min(getMaxElement(iq, "neighbors"),
				rendezvousImpl.getMaxWhoisaliveSyncNeighborCount());
		int maxManagers = Math.min(getMaxElement(iq, "managers"),
				rendezvousImpl.getMaxWhoisaliveSyncNeighborCount());
		int afterNeighbors = getAfterNeighbors(iq, "neighbors",
				orderedNeighbors);
		int afterManagers = getAfterManager(iq, "managers", orderedManagers);
		return createResponse(iq, rendezvousImpl, orderedNeighbors,
				orderedManagers, afterNeighbors, afterManagers, maxNeighbors,
				maxManagers);
	}

	private IQ createResponse(IQ iq, RendezvousImpl rendezvousImpl,
			String[] orderedNeighbors, RendezvousItem[] orderedManagers,
			int afterNeighbors, int afterManager, int maxNeighbors,
			int maxManagers) {
		IQ response = IQ.createResultIQ(iq);
		Element queryElement = response.getElement().addElement("query",
				WHOISALIVESYNC_NAMESPACE);

		Element neighborsEl = queryElement.addElement("neighbors");
		int i;
		for (i = afterNeighbors; i < afterNeighbors + maxNeighbors
				&& i < orderedNeighbors.length; i++) {
			Element neighborEl = neighborsEl.addElement("neighbor");
			neighborEl.addElement("id").setText(orderedNeighbors[i]);
		}
		if (i > 0) {
			addSet(neighborsEl, orderedNeighbors[afterNeighbors],
					orderedNeighbors[i - 1], i - afterNeighbors);
		} else {
			addEmptySet(neighborsEl);
		}

		Element managersEl = queryElement.addElement("managers");
		for (i = afterManager; i < maxManagers + afterManager
				&& i < orderedManagers.length; i++) {
			RendezvousItem item = orderedManagers[i];
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
		if (i > 0) {
			addSet(managersEl, orderedManagers[afterManager].getResourcesInfo()
					.getId(),
					orderedManagers[i - 1].getResourcesInfo().getId(), i
							- afterManager);
		} else {
			addEmptySet(managersEl);
		}
		return response;
	}

	private int getAfterNeighbors(IQ iq, String element,
			String[] orderedNeighbors) {
		Element queryEl = iq.getElement().element("query");
		Element neighborsEl = queryEl.element("neighbors");
		Element setEl = neighborsEl.element("set");
		Element lastEl = setEl.element("after");
		String after = null;
		if (lastEl != null) {
			after = lastEl.getText();
			for (int i = 0; i < orderedNeighbors.length; i += 1) {
				if (orderedNeighbors[i].equals(after)) {
					return i + 1;
				}
			}
			return 1;
		} else {
			return 0;
		}
	}

	private int getAfterManager(IQ iq, String element,
			RendezvousItem[] orderedManagers) {
		Element queryEl = iq.getElement().element("query");
		Element managersEl = queryEl.element("managers");
		Element setEl = managersEl.element("set");
		Element lastEl = setEl.element("after");
		String after = null;
		if (lastEl != null) {
			after = lastEl.getText();
			for (int i = 0; i < orderedManagers.length; i++) {
				if (orderedManagers[i].getResourcesInfo().getId().equals(after)) {
					return i + 1;
				}
			}
			return 1;
		} else {
			return 0;
		}
	}

	private void addEmptySet(Element el) {
		Element setEl = el.addElement("set");
		setEl.addElement("count").setText("0");
	}

	private int getMaxElement(IQ iq, String element) {
		Element queryEl = iq.getElement().element("query");
		Element neighborsEl = queryEl.element(element);
		Element setEl = neighborsEl.element("set");
		String max = setEl.element("max").getText();
		return Integer.parseInt(max);
	}

	private void addSet(Element el, String first, String last, int count) {
		Element setEl = el.addElement("set");
		setEl.addElement("first").setText(first);
		setEl.addElement("last").setText(last);
		setEl.addElement("count").setText(count + "");
	}
}
