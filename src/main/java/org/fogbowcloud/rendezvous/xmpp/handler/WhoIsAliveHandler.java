package org.fogbowcloud.rendezvous.xmpp.handler;

import java.util.List;

import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.Rendezvous;
import org.fogbowcloud.rendezvous.core.RendezvousImpl;
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
		Element queryEl = iq.getElement().element("query");
		Element setEl = queryEl.element("set");
		int max = Integer.parseInt(setEl.element("max").getText());
		max = Math.min(max, ((RendezvousImpl)rendezvous).getMaxWhoisaliveManagerCount());
		return createResponse(iq, aliveIds, max);
	}

	private IQ createResponse(IQ iq, List<RendezvousItem> aliveIds, int max) {
		IQ resultIQ = IQ.createResultIQ(iq);

		Element queryElement = resultIQ.getElement().addElement("query",
				NAMESPACE);
		int i;
		for (i = 0; i < max && i < aliveIds.size(); i++) {
			RendezvousItem rendezvousItem = aliveIds.get(i);
			Element itemEl = queryElement.addElement("item");
			itemEl.addAttribute("id", rendezvousItem.getResourcesInfo().getId());
			String cert = rendezvousItem.getResourcesInfo().getCert();
			if (cert != null) {
				itemEl.addElement("cert").setText(cert);
			}

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
			statusEl.addElement("updated").setText(
					String.valueOf(rendezvousItem.getFormattedTime()));
		}
		String first = "";
		String last = "";
		String count = "0";
		if (aliveIds.size() > 0) {
			first = aliveIds.get(0).getResourcesInfo().getId();
			last = aliveIds.get(i-1).getResourcesInfo().getId();
			count = "" + i;
		}
		
		Element setEl = queryElement.addElement("set", "http://jabber.org/protocol/rsm");
		setEl.addElement("first").setText(first);
		setEl.addElement("last").setText(last);
		setEl.addElement("count").setText(count);
		return resultIQ;
	}
}
