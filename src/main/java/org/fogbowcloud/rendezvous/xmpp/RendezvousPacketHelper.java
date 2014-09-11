package org.fogbowcloud.rendezvous.xmpp;

import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.RendezvousItem;
import org.fogbowcloud.rendezvous.core.ResourcesInfo;
import org.fogbowcloud.rendezvous.core.model.Flavor;
import org.fogbowcloud.rendezvous.xmpp.model.RendezvousResponseItem;
import org.jamppa.component.PacketSender;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;

public class RendezvousPacketHelper {

	public static final String WHOISALIVESYNCH_NAMESPACE = "http://fogbowcloud.org/rendezvous/synch/whoisalive";

	public static RendezvousResponseItem sendWhoIsAliveSyncIq(String neighborAddress,
			PacketSender packetSender) throws ParseException {
		IQ iq = new IQ(Type.get);
		iq.setTo(neighborAddress);
		iq.getElement().addElement("query", WHOISALIVESYNCH_NAMESPACE);
		IQ response = (IQ) packetSender.syncSendPacket(iq);
		RendezvousResponseItem responseItem = convertWhoIsAliveSyncResponse(response);
		
		return responseItem;
	}

	@SuppressWarnings("unchecked")
	private static RendezvousResponseItem convertWhoIsAliveSyncResponse(IQ iq) throws ParseException {
		Element queryElement = iq.getElement().element("query");
		Element neighborsEl = queryElement.element("neighbors");
		Iterator<Element> neighborsIterator = neighborsEl
				.elementIterator("neighbor");
		List<String> neighborIds = new LinkedList<String>();
		while (neighborsIterator.hasNext()) {
			Element itemEl = (Element) neighborsIterator.next();
			String neighbor = itemEl.element("id").getText();
			neighborIds.add(neighbor);
		}
		Element managersEl = queryElement.element("managers");
		Iterator<Element> managersIterator = managersEl
				.elementIterator("manager");
		List<RendezvousItem> managersAlive = new LinkedList<RendezvousItem>();
		while (managersIterator.hasNext()) {
			Element itemEl = (Element) managersIterator.next();
			managersAlive.add(getWhoIsAliveResponseItem(itemEl));
		}
		RendezvousResponseItem rendezvousItem = new RendezvousResponseItem(
				neighborIds, managersAlive);

		return rendezvousItem;
	}
	
	@SuppressWarnings("unchecked")
	public static RendezvousItem getWhoIsAliveResponseItem(
			Element itemEl) throws ParseException {
		Attribute id = itemEl.attribute("id");
		Element statusEl = itemEl.element("status");
		Element certEl = itemEl.element("cert");
		String cert = certEl == null ? null : certEl.getText();
		String cpuIdle = statusEl.element("cpu-idle").getText();
		String cpuInUse = statusEl.element("cpu-inuse").getText();
		String memIdle = statusEl.element("mem-idle").getText();
		String memInUse = statusEl.element("mem-inuse").getText();
		String updated = statusEl.element("updated").getText();

		List<Flavor> flavoursList = new LinkedList<Flavor>();
		Iterator<Element> flavourIterator = statusEl.elementIterator("flavor");
		while (flavourIterator.hasNext()) {
			Element flavour = (Element) flavourIterator.next();
			String name = flavour.element("name").getText();
			String cpu = flavour.element("cpu").getText();
			String mem = flavour.element("mem").getText();
			int capacity = Integer.parseInt(flavour.element("capacity")
					.getText());
			Flavor flavor = new Flavor(name, cpu, mem, capacity);
			flavoursList.add(flavor);
		}

		ResourcesInfo resources = new ResourcesInfo(id.getValue(), cpuIdle,
				cpuInUse, memIdle, memInUse, flavoursList, cert);
		RendezvousItem item = new RendezvousItem(resources);
		item.setLastTime(RendezvousItem.ISO_8601_DATE_FORMAT.parse(
				updated).getTime());
		return item;
	}
}
