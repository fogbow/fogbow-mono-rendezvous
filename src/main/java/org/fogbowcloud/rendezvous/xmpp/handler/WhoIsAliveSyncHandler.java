package org.fogbowcloud.rendezvous.xmpp.handler;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.Rendezvous;
import org.fogbowcloud.rendezvous.core.RendezvousImpl;
import org.fogbowcloud.rendezvous.core.RendezvousItem;
import org.fogbowcloud.rendezvous.xmpp.util.FederationMember;
import org.fogbowcloud.rendezvous.xmpp.util.RSM;
import org.jamppa.component.handler.AbstractQueryHandler;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

public class WhoIsAliveSyncHandler extends AbstractQueryHandler {

	public final static String WHOISALIVESYNC_NAMESPACE = "http://fogbowcloud.org/rendezvous/synch/whoisalive";
	private Rendezvous rendezvous;

	public WhoIsAliveSyncHandler(Rendezvous rendezvous) {
		super(WHOISALIVESYNC_NAMESPACE);
		this.rendezvous = rendezvous;
	}

	@Override
	public IQ handle(IQ iq) {
		RendezvousImpl rendezvousImpl = (RendezvousImpl) rendezvous;

		Element queryEl = iq.getElement().element("query");

		Element managersEl = queryEl.element("managers");
		int maxManagers = rendezvousImpl.getMaxWhoisaliveSyncNeighborCount();
		RSM managersRSM = RSM.parse(managersEl, maxManagers);

		Map<String, RendezvousItem> managersAlive = rendezvousImpl
				.getManagersAlive();
		List<RendezvousItem> managersList = new LinkedList<RendezvousItem>(
				managersAlive.values());
		return createResponse(iq, rendezvousImpl, managersList, managersRSM);
	}

	@SuppressWarnings("unchecked")
	private IQ createResponse(IQ iq, RendezvousImpl rendezvousImpl, 
			List<RendezvousItem> managers, RSM managersRsm) {
		IQ response = IQ.createResultIQ(iq);
		Element queryElement = response.getElement().addElement("query",
				WHOISALIVESYNC_NAMESPACE);

		List<FederationMember> filteredManagers = (List<FederationMember>) managersRsm
				.filter(managers);
		
		if (filteredManagers == null) {
			String from = iq.getFrom().toFullJID();
			iq.setFrom(iq.getTo());
			iq.setTo(from);
			iq.setError(new PacketError(
					PacketError.Condition.item_not_found,
					PacketError.Type.cancel));
			return iq;
		}
		

		Element managersEl = queryElement.addElement("managers");	
		for (FederationMember rsmItem : filteredManagers) {
			RendezvousItem item = (RendezvousItem) rsmItem;
			Element managerEl = managersEl.addElement("manager");
			managerEl.addAttribute("id", item.getMemberId());
			String cert = item.getCert();
			if (cert != null) {
				managerEl.addElement("cert").setText(cert);
			}
			Element statusEl = managerEl.addElement("status");
			
			statusEl.addElement("timeout").setText(String.valueOf(item.getTimeout()));			
			statusEl.addElement("quiet-for").setText(
					String.valueOf(System.currentTimeMillis() - item.getLastTime()));		
		}
		managersRsm.appendSetElements(queryElement.element("managers"),
				filteredManagers);
		return response;
	}
}
