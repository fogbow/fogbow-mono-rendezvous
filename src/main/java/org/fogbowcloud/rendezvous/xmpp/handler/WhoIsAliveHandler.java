package org.fogbowcloud.rendezvous.xmpp.handler;

import java.util.List;
import java.util.concurrent.locks.Condition;

import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.Rendezvous;
import org.fogbowcloud.rendezvous.core.RendezvousImpl;
import org.fogbowcloud.rendezvous.core.RendezvousItem;
import org.fogbowcloud.rendezvous.core.model.Flavor;
import org.fogbowcloud.rendezvous.xmpp.util.RSM;
import org.jamppa.component.handler.AbstractQueryHandler;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

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
		int defaultMax = ((RendezvousImpl) rendezvous)
				.getMaxWhoisaliveManagerCount();
		RSM rsm = RSM.parse(queryEl, defaultMax);
		return createResponse(iq, rsm, aliveIds);
	}

	@SuppressWarnings("unchecked")
	private IQ createResponse(IQ iq, RSM rsm, List<RendezvousItem> aliveIds) {
		IQ resultIQ = IQ.createResultIQ(iq);

		Element queryElement = resultIQ.getElement().addElement("query",
				NAMESPACE);
		List<RendezvousItem> filteredAliveIds = (List<RendezvousItem>) rsm
				.filter(aliveIds);

		if (filteredAliveIds == null) {
			String from = iq.getFrom().toFullJID();
			iq.setFrom(iq.getTo());
			iq.setTo(from);
			iq.setError(new PacketError(
					PacketError.Condition.item_not_found,
					PacketError.Type.cancel));
			return iq;
		}

		for (RendezvousItem rendezvousItem : filteredAliveIds) {
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
		queryElement = rsm.appendSetElements(queryElement, filteredAliveIds);
		return resultIQ;
	}
}
