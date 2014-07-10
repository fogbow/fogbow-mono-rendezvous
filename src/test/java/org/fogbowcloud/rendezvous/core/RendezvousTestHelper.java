package org.fogbowcloud.rendezvous.core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.model.Flavor;
import org.fogbowcloud.rendezvous.xmpp.RendezvousXMPPComponent;
import org.fogbowcloud.rendezvous.xmpp.model.RendezvousResponseItem;
import org.fogbowcloud.rendezvous.xmpp.model.WhoIsAliveResponseItem;
import org.jamppa.client.XMPPClient;
import org.jamppa.client.plugin.xep0077.XEP0077;
import org.jivesoftware.smack.XMPPException;
import org.xmpp.component.ComponentException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;

public class RendezvousTestHelper {

	private static final int SERVER_CLIENT_PORT = 5222;
	private static final int SERVER_COMPONENT_PORT = 5347;
	private static final String SERVER_HOST = "localhost";

	public static final String RENDEZVOUS_COMPONENT_URL = "rendezvous.test.com";
	public static final String RENDEZVOUS_COMPONENT_PASS = "password";

	public static final String WHOISALIVE_NAMESPACE = "http://fogbowcloud.org/rendezvous/whoisalive";
	public static final String IAMALIVE_NAMESPACE = "http://fogbowcloud.org/rendezvous/iamalive";
	public static final String WHOISALIVESYNC_NAMESPACE = "http://fogbowcloud.org/rendezvous/synch/whoisalive";

	public static final int TEST_DEFAULT_TIMEOUT = 10000;
	public static final int TIMEOUT_GRACE = 500;

	public static final String NEIGHBOR_CLIENT_JID = "neighborClient@test.com/Smack";
	final String NEIGHBOR_CLIENT_PASSWORD = "neighborClient";
	private RendezvousXMPPComponent rendezvousXmppComponent;

	public RendezvousXMPPComponent getRendezvousXmppComponent() {
		return rendezvousXmppComponent;
	}

	private ArrayList<XMPPClient> xmppClients = new ArrayList<XMPPClient>();

	public XMPPClient createXMPPClient() throws XMPPException {
		int clientIndex = this.xmppClients.size();

		final String client = getClientJid(clientIndex);
		final String client_pass = getClientPassword(clientIndex);

		XMPPClient xmppClient = new XMPPClient(client, client_pass,
				SERVER_HOST, SERVER_CLIENT_PORT);
		XEP0077 register = new XEP0077();
		xmppClient.registerPlugin(register);
		xmppClient.connect();
		try {
			register.createAccount(client, client_pass);
		} catch (XMPPException e) {
		}

		xmppClient.login();
		xmppClient.process(false);
		xmppClients.add(xmppClient);

		return xmppClient;
	}

	public static String getClientJid(int clientIndex) {
		return "user" + clientIndex + "@test.com";
	}

	private static String getClientPassword(int clientIndex) {
		return "user" + clientIndex;
	}

	public void disconnectXMPPClients() {
		for (XMPPClient xmppClient : this.xmppClients) {
			xmppClient.disconnect();
		}
	}

	public void initializeXMPPRendezvousComponent(int timeout)
			throws ComponentException {
		rendezvousXmppComponent = new RendezvousXMPPComponent(
				RENDEZVOUS_COMPONENT_URL, RENDEZVOUS_COMPONENT_PASS,
				SERVER_HOST, SERVER_COMPONENT_PORT, timeout,
				new String[] {});
		rendezvousXmppComponent.setDescription("Rendezvous Component");
		rendezvousXmppComponent.setName("rendezvous");
		rendezvousXmppComponent.connect();
		rendezvousXmppComponent.process();
	}
	
	public void initializeXMPPRendezvousComponent(int timeout, String[] neighbors)
			throws ComponentException {
		rendezvousXmppComponent = new RendezvousXMPPComponent(
				RENDEZVOUS_COMPONENT_URL, RENDEZVOUS_COMPONENT_PASS,
				SERVER_HOST, SERVER_COMPONENT_PORT, timeout,
				neighbors);
		rendezvousXmppComponent.setDescription("Rendezvous Component");
		rendezvousXmppComponent.setName("rendezvous");
		rendezvousXmppComponent.connect();
		rendezvousXmppComponent.process();
	}

	public void disconnectRendezvousXMPPComponent() throws ComponentException {
		rendezvousXmppComponent.disconnect();
	}

	public static IQ createIAmAliveIQ() {
		IQ iq = new IQ(Type.get);
		iq.setTo(RENDEZVOUS_COMPONENT_URL);
		Element statusEl = iq.getElement()
				.addElement("query", IAMALIVE_NAMESPACE).addElement("status");
		iq.getElement().element("query").addElement("cert").setText("cert");
		statusEl.addElement("cpu-idle").setText("valor1");
		statusEl.addElement("cpu-inuse").setText("valor2");
		statusEl.addElement("mem-idle").setText("valor3");
		statusEl.addElement("mem-inuse").setText("valor4");
		return iq;
	}

	public static ArrayList<String> getAliveIds(IQ whoIsAliveResponse)
			throws ParseException {
		ArrayList<String> aliveIds = new ArrayList<String>();
		for (RendezvousItem item : getItemsFromIQ(whoIsAliveResponse)) {
			aliveIds.add(item.getResourcesInfo().getId());
		}
		return aliveIds;
	}

	@SuppressWarnings("unchecked")
	public static LinkedList<RendezvousItem> getItemsFromIQ(
			IQ responseFromWhoIsAliveIQ) throws ParseException {
		Element queryElement = responseFromWhoIsAliveIQ.getElement().element(
				"query");
		Iterator<Element> itemIterator = queryElement.elementIterator("item");
		LinkedList<RendezvousItem> aliveItems = new LinkedList<RendezvousItem>();

		while (itemIterator.hasNext()) {
			Element itemEl = (Element) itemIterator.next();
			aliveItems.add(getWhoIsAliveResponseItem(itemEl));
		}
		return aliveItems;
	}

	public static IQ createWhoIsAliveIQ() {
		IQ iq = new IQ(Type.get);
		iq.setTo(RENDEZVOUS_COMPONENT_URL);
		iq.getElement().addElement("query", WHOISALIVE_NAMESPACE);
		return iq;
	}

	public static IQ createWhoIsAliveSyncIQ() {
		IQ iq = new IQ(Type.get);
		iq.setTo(RENDEZVOUS_COMPONENT_URL);
		iq.getElement().addElement("query", WHOISALIVESYNC_NAMESPACE);
		return iq;
	}

	public static RendezvousResponseItem getItemsFromSyncIQ(IQ iq)
			throws ParseException {
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

	public static RendezvousItem getWhoIsAliveResponseItem(Element itemEl)
			throws ParseException {
		Attribute id = itemEl.attribute("id");
		Element statusEl = itemEl.element("status");
		String cert = itemEl.element("cert").getText();
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
		item.setLastTime(RendezvousItem.ISO_8601_DATE_FORMAT.parse(updated)
				.getTime());
		return item;
	}

	public ResourcesInfo getResources() {
		List<Flavor> flavours = new LinkedList<Flavor>();
		ResourcesInfo resources = new ResourcesInfo("id", "cpuIdle",
				"cpuInUse", "memIdle", "memInUse", flavours, "cert");
		return resources;
	}

	public IQ createWhoIsAliveSyncResponse(IQ iq) {
		IQ response = IQ.createResultIQ(iq);
		Element queryElement = response.getElement().addElement("query",
				WHOISALIVESYNC_NAMESPACE);

		Element neighborsEl = queryElement.addElement("neighbors");
		Element neighborEl = neighborsEl.addElement("neighbor");
		neighborEl.addElement("id").setText("id");

		Element managersEl = queryElement.addElement("managers");
		Element managerEl = managersEl.addElement("manager");
		managerEl.addAttribute("id", "id");
		managerEl.addElement("cert").setText("cert");
		Element statusEl = managerEl.addElement("status");
		statusEl.addElement("cpu-idle").setText("cpu-idle");
		statusEl.addElement("cpu-inuse").setText("cpu-inuse");
		statusEl.addElement("mem-idle").setText("mem-idle");
		statusEl.addElement("mem-inuse").setText("mem-inuse");

		Element flavorElement = statusEl.addElement("flavor");
		flavorElement.addElement("name").setText("flavor");
		flavorElement.addElement("cpu").setText("cpu");
		flavorElement.addElement("mem").setText("mem");
		flavorElement.addElement("capacity").setText("5");

		statusEl.addElement("updated").setText(
				"2012-10-01T09:45:00.000UTC+00:00");
		return response;
	}

	public XMPPClient createNeighborClient() throws XMPPException {
		final String client = NEIGHBOR_CLIENT_JID;
		final String client_pass = NEIGHBOR_CLIENT_PASSWORD;

		XMPPClient xmppClient = new XMPPClient(client, client_pass,
				SERVER_HOST, SERVER_CLIENT_PORT);
		XEP0077 register = new XEP0077();
		xmppClient.registerPlugin(register);
		xmppClient.connect();
		try {
			register.createAccount(client, client_pass);
		} catch (XMPPException e) {
		}

		xmppClient.login();
		xmppClient.process(false);
		xmppClients.add(xmppClient);

		return xmppClient;
	}
}
