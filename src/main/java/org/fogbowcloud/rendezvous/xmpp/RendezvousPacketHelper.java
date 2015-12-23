package org.fogbowcloud.rendezvous.xmpp;

import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.RendezvousItem;
import org.fogbowcloud.rendezvous.xmpp.handler.WhoIsAliveSyncHandler;
import org.fogbowcloud.rendezvous.xmpp.model.RendezvousResponseItem;
import org.jamppa.component.PacketSender;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;

public class RendezvousPacketHelper {

	public static RendezvousResponseItem sendWhoIsAliveSyncIq(String neighborAddress,
			PacketSender packetSender) throws ParseException {
		IQ iq = new IQ(Type.get);
		iq.setTo(neighborAddress);
		iq.getElement().addElement("query", WhoIsAliveSyncHandler.WHOISALIVESYNC_NAMESPACE);
		IQ response = (IQ) packetSender.syncSendPacket(iq);
		RendezvousResponseItem responseItem = convertWhoIsAliveSyncResponse(response);

		return responseItem;
	}

	@SuppressWarnings("unchecked")
	private static RendezvousResponseItem convertWhoIsAliveSyncResponse(IQ iq)
			throws ParseException {
		Element queryElement = iq.getElement().element("query");
		Element managersEl = queryElement.element("managers");
		Iterator<Element> managersIterator = managersEl.elementIterator("manager");
		List<RendezvousItem> managersAlive = new LinkedList<RendezvousItem>();
		while (managersIterator.hasNext()) {
			Element itemEl = (Element) managersIterator.next();
			managersAlive.add(getWhoIsAliveResponseItem(itemEl));
		}
		RendezvousResponseItem rendezvousItem = new RendezvousResponseItem(managersAlive);

		return rendezvousItem;
	}

	public static RendezvousItem getWhoIsAliveResponseItem(Element itemEl) throws ParseException {
		Attribute id = itemEl.attribute("id");
		Element statusEl = itemEl.element("status");
		Element certEl = itemEl.element("cert");
		String cert = certEl == null ? null : certEl.getText();
		long timeout = Long.parseLong(statusEl.element("timeout").getText());
		long quietFor = Long.parseLong(statusEl.element("quiet-for").getText());

		RendezvousItem item = new RendezvousItem(id.getValue(), cert, timeout);
		item.setLastTime(System.currentTimeMillis() - quietFor);
		return item;
	}
}
