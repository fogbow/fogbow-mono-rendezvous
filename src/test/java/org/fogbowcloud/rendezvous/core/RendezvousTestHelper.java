package org.fogbowcloud.rendezvous.core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.model.Flavor;
import org.fogbowcloud.rendezvous.xmpp.RendezvousXMPPComponent;
import org.fogbowcloud.rendezvous.xmpp.model.RendezvousResponseItem;
import org.fogbowcloud.rendezvous.xmpp.util.FakeXMPPServer;
import org.jamppa.client.XMPPClient;
import org.jivesoftware.smack.XMPPException;
import org.mockito.Mockito;
import org.xmpp.component.ComponentException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;

public class RendezvousTestHelper {
	
	public static final String PROP_EXPIRATION = "site_expiration";
	public static final String PROP_NEIGHBORS = "neighbors";
	public static final String PROP_MAX_WHOISALIVE_MANAGER_COUNT = "max_whoisalive_manager_count";
	public static final String PROP_MAX_WHOISALIVESYNC_MANAGER_COUNT = "max_whoisalivesync_manager_count";
	public static final String PROP_MAX_WHOISALIVESYNC_NEIGHBOR_COUNT = "max_whoisalivesync_neighbor_count";
	
	private static final String HTTP_JABBER_ORG_PROTOCOL_RSM = "http://jabber.org/protocol/rsm";
	private static final int SERVER_CLIENT_PORT = 5222;
	private static final int SERVER_COMPONENT_PORT = 5347;
	private static final String SERVER_HOST = "localhost";

	public static final String RENDEZVOUS_COMPONENT_URL = "rendezvous.test.com";
	public static final String RENDEZVOUS_COMPONENT_PASS = "password";

	public static final String WHOISALIVE_NAMESPACE = "http://fogbowcloud.org/rendezvous/whoisalive";
	public static final String IAMALIVE_NAMESPACE = "http://fogbowcloud.org/rendezvous/iamalive";
	public static final String WHOISALIVESYNC_NAMESPACE = "http://fogbowcloud.org/rendezvous/synch/whoisalive";

	public static final long TEST_DEFAULT_TIMEOUT = 600;
	public static final long TIMEOUT_GRACE = 60;

	public static final String NEIGHBOR_CLIENT_JID = "neighborClient@test.com/Smack";
	public static final int MAX_WHOISALIVE_MANAGER_COUNT = 100;
	private static final int MAX_WHOISALIVESYNC_MANAGER_COUNT = 100;
	private static final int MAX_WHOISALIVESYNC_NEIGHBOR_COUNT = 100;
	final String NEIGHBOR_CLIENT_PASSWORD = "neighborClient";
	private RendezvousXMPPComponent rendezvousXmppComponent;
	private FakeXMPPServer fakeServer = new FakeXMPPServer();

	public RendezvousXMPPComponent getRendezvousXmppComponent() {
		return rendezvousXmppComponent;
	}

	private ArrayList<XMPPClient> xmppClients = new ArrayList<XMPPClient>();

	public XMPPClient createXMPPClient() throws XMPPException {
		int clientIndex = this.xmppClients.size();

		final String client = getClientJid(clientIndex);
		final String client_pass = getClientPassword(clientIndex);

		XMPPClient xmppClient = Mockito.spy(new XMPPClient(client, client_pass,
				SERVER_HOST, SERVER_CLIENT_PORT));
		fakeServer.connect(xmppClient);
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
			fakeServer.disconnect(xmppClient.getJid().toBareJID());
		}
	}

	public void initializeXMPPRendezvousComponent(long testDefaultTimeout)
			throws Exception {
		Properties properties = Mockito.mock(Properties.class);
		Mockito.when(properties.getProperty(PROP_EXPIRATION)).thenReturn(testDefaultTimeout + "");
		Mockito.doReturn("").when(properties).getProperty(PROP_NEIGHBORS);
		Mockito.doReturn("").when(properties).getProperty(PROP_MAX_WHOISALIVE_MANAGER_COUNT);
		Mockito.doReturn("").when(properties).getProperty(PROP_MAX_WHOISALIVESYNC_MANAGER_COUNT);
		Mockito.doReturn("").when(properties).getProperty(PROP_MAX_WHOISALIVESYNC_NEIGHBOR_COUNT);
		RendezvousXMPPComponent comp = new RendezvousXMPPComponent(
				RENDEZVOUS_COMPONENT_URL, RENDEZVOUS_COMPONENT_PASS,
				SERVER_HOST, SERVER_COMPONENT_PORT, properties);
		rendezvousXmppComponent = Mockito.spy(comp);
		((RendezvousImpl) comp.getRendezvous())
				.setPacketSender(rendezvousXmppComponent);
		rendezvousXmppComponent.setDescription("Rendezvous Component");
		rendezvousXmppComponent.setName("rendezvous");
		fakeServer.connect(rendezvousXmppComponent);
		rendezvousXmppComponent.process();
	}

	public void initializeXMPPRendezvousComponent(long testDefaultTimeout,
			String[] neighbors) throws Exception {
		Properties properties = Mockito.mock(Properties.class);
		String neighborsString = "";
		for (int i = 0; i < neighbors.length; i++) {
			if (i != 0) {
				neighborsString += ",";
			}
			neighborsString += neighbors[i];
		}
		Mockito.when(properties.getProperty(PROP_EXPIRATION)).thenReturn(testDefaultTimeout + "");
		Mockito.when(properties.getProperty(PROP_NEIGHBORS)).thenReturn(neighborsString);
		Mockito.doReturn("").when(properties).getProperty(PROP_MAX_WHOISALIVE_MANAGER_COUNT);
		Mockito.doReturn("").when(properties).getProperty(PROP_MAX_WHOISALIVESYNC_MANAGER_COUNT);
		Mockito.doReturn("").when(properties).getProperty(PROP_MAX_WHOISALIVESYNC_NEIGHBOR_COUNT);
		RendezvousXMPPComponent comp = new RendezvousXMPPComponent(
				RENDEZVOUS_COMPONENT_URL, RENDEZVOUS_COMPONENT_PASS,
				SERVER_HOST, SERVER_COMPONENT_PORT, properties);
		rendezvousXmppComponent = Mockito.spy(comp);
		((RendezvousImpl) comp.getRendezvous())
				.setPacketSender(rendezvousXmppComponent);
		rendezvousXmppComponent.setDescription("Rendezvous Component");
		rendezvousXmppComponent.setName("rendezvous");
		fakeServer.connect(rendezvousXmppComponent);
		rendezvousXmppComponent.process();
	}

	public void initializeXMPPRendezvousComponent(long testDefaultTimeout,
			String[] neighbors, ScheduledExecutorService executor)
			throws Exception {
		Properties properties = Mockito.mock(Properties.class);
		String neighborsString = "";
		for (int i = 0; i < neighbors.length; i++) {
			if (i != 0) {
				neighborsString += ",";
			}
			neighborsString += neighbors[i];
		}
		Mockito.when(properties.getProperty(PROP_EXPIRATION)).thenReturn(testDefaultTimeout + "");
		Mockito.when(properties.getProperty(PROP_NEIGHBORS)).thenReturn(neighborsString);
		Mockito.doReturn("").when(properties).getProperty(PROP_MAX_WHOISALIVE_MANAGER_COUNT);
		Mockito.doReturn("").when(properties).getProperty(PROP_MAX_WHOISALIVESYNC_MANAGER_COUNT);
		Mockito.doReturn("").when(properties).getProperty(PROP_MAX_WHOISALIVESYNC_NEIGHBOR_COUNT);
		RendezvousXMPPComponent comp = new RendezvousXMPPComponent(
				RENDEZVOUS_COMPONENT_URL, RENDEZVOUS_COMPONENT_PASS,
				SERVER_HOST, SERVER_COMPONENT_PORT, properties,  executor);
		rendezvousXmppComponent = Mockito.spy(comp);
		((RendezvousImpl) comp.getRendezvous())
				.setPacketSender(rendezvousXmppComponent);
		rendezvousXmppComponent.setDescription("Rendezvous Component");
		rendezvousXmppComponent.setName("rendezvous");
		fakeServer.connect(rendezvousXmppComponent);
		rendezvousXmppComponent.process();
	}

	public void disconnectRendezvousXMPPComponent() throws ComponentException {
		fakeServer.disconnect(rendezvousXmppComponent.getJID().toBareJID());
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
	
	public static String getSetElementFromWhoIsAlive(IQ whoIsAliveResponse, String elementName) {
		Element queryElement = whoIsAliveResponse.getElement().element(
				"query");
		Element setElement = queryElement.element("set");
		String element= setElement.element(elementName).getText();
		return element;
	}
	
	@SuppressWarnings("unchecked")
	public static LinkedList<RendezvousItem> getItemsFromIQ(
			IQ whoIsAliveResponse) throws ParseException {
		Element queryElement = whoIsAliveResponse.getElement().element(
				"query");
		Iterator<Element> itemIterator = queryElement.elementIterator("item");
		LinkedList<RendezvousItem> aliveItems = new LinkedList<RendezvousItem>();

		while (itemIterator.hasNext()) {
			Element itemEl = (Element) itemIterator.next();
			aliveItems.add(getWhoIsAliveResponseItem(itemEl));
		}

/*		Element setElement = queryElement.element("set");
		String fist = setElement.element("first").getText();
		String last = setElement.element("last").getText();
		int count = Integer.parseInt(setElement.element("count").getText());*/
		return aliveItems;
	}
	
	public static IQ createWhoIsAliveIQ() {
		return createWhoIsAliveIQ("");
	}
	
	public static IQ createWhoIsAliveIQ(String after) {
		IQ iq = new IQ(Type.get);
		iq.setTo(RENDEZVOUS_COMPONENT_URL);
		Element queryEl = iq.getElement().addElement("query",
				WHOISALIVE_NAMESPACE);
		Element setEl = queryEl.addElement("set",
				HTTP_JABBER_ORG_PROTOCOL_RSM);
		setEl.addElement("max").setText("" + MAX_WHOISALIVE_MANAGER_COUNT);
		if (!after.isEmpty()) {
			setEl.addElement("after").setText(after);
		}
		return iq;
	}

	public static IQ createWhoIsAliveSyncIQ() {
		IQ iq = new IQ(Type.get);
		iq.setTo(RENDEZVOUS_COMPONENT_URL);
		Element queryEl = iq.getElement().addElement("query", WHOISALIVESYNC_NAMESPACE);
		Element neighborsEl = queryEl.addElement("neighbors");
		Element setEl = neighborsEl.addElement("set",
				HTTP_JABBER_ORG_PROTOCOL_RSM);
		setEl.addElement("max").setText("" + MAX_WHOISALIVESYNC_NEIGHBOR_COUNT);
		
		Element managersEl = queryEl.addElement("managers");
		setEl = managersEl.addElement("set",
				HTTP_JABBER_ORG_PROTOCOL_RSM);
		setEl.addElement("max").setText("" + MAX_WHOISALIVESYNC_MANAGER_COUNT);
		
		return iq;
	}
	
	public String getNeighborsSetElementsFromSyncIQ(String elementName,IQ syncResponse) {
		Element queryElement = syncResponse.getElement().element(
				"query");
		Element neighborsEl = queryElement.element("neighbors");
		Element setElement = neighborsEl.element("set");
		String element= setElement.element(elementName).getText();
		return element;
	}
	
	public String getManagersSetElementsFromSyncIQ(String elementName,IQ syncResponse) {
		Element queryElement = syncResponse.getElement().element(
				"query");
		Element managersEl = queryElement.element("neighbors");
		Element setElement = managersEl.element("set");
		String element= setElement.element(elementName).getText();
		return element;
	}
	
	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
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

		XMPPClient xmppClient = Mockito.spy(new XMPPClient(client, client_pass,
				SERVER_HOST, SERVER_CLIENT_PORT));
		fakeServer.connect(xmppClient);
		xmppClient.process(false);
		xmppClients.add(xmppClient);

		return xmppClient;
	}
}
