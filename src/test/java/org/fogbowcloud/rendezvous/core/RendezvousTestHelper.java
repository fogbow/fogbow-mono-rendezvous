package org.fogbowcloud.rendezvous.core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.fogbowcloud.rendezvous.xmpp.RendezvousXMPPComponent;
import org.fogbowcloud.rendezvous.xmpp.model.RendezvousResponseItem;
import org.fogbowcloud.rendezvous.xmpp.util.FakeXMPPServer;
import org.jamppa.client.XMPPClient;
import org.jivesoftware.smack.XMPPException;
import org.mockito.Mockito;
import org.xmpp.component.ComponentException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.Packet;

public class RendezvousTestHelper {

	public static final int DEFAULT_WAIT_FREQUENCY_TIMES = 3;
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
	private static int maxWhoIsAliveManagerCount = 100;
	private static int maxWhoIsAliveSyncManagerCount = 100;
	private static int maxWhoIsAliveSyncNeighborCount = 100;

	public static int getMaxWhoIsAliveManagerCount() {
		return maxWhoIsAliveManagerCount;
	}

	public static void setMaxWhoIsAliveManagerCount(
			int maxWhoIsAliveManagerCount) {
		RendezvousTestHelper.maxWhoIsAliveManagerCount = maxWhoIsAliveManagerCount;
	}

	public static int getMaxWhoIsAliveSyncManagerCount() {
		return maxWhoIsAliveSyncManagerCount;
	}

	public static void setMaxWhoIsAliveSyncManagerCount(
			int maxWhoIsAliveSyncManagerCount) {
		RendezvousTestHelper.maxWhoIsAliveSyncManagerCount = maxWhoIsAliveSyncManagerCount;
	}

	public static int getMaxWhoIsAliveSyncNeighborCount() {
		return maxWhoIsAliveSyncNeighborCount;
	}

	public static void setMaxWhoIsAliveSyncNeighborCount(
			int maxWhoIsAliveSyncNeighborCount) {
		RendezvousTestHelper.maxWhoIsAliveSyncNeighborCount = maxWhoIsAliveSyncNeighborCount;
	}

	final String NEIGHBOR_CLIENT_PASSWORD = "neighborClient";
	private RendezvousXMPPComponent rendezvousXmppComponent;
	private RendezvousXMPPComponent rendezvousXmppComponentNumberTwo;
	private RendezvousXMPPComponent rendezvousXmppComponentNumberThree;
		
	private FakeXMPPServer fakeServer = new FakeXMPPServer();

	public RendezvousXMPPComponent getRendezvousXmppComponentNumberThree() {
		return rendezvousXmppComponentNumberThree;
	}

	public void setRendezvousXmppComponentNumberThree(
			RendezvousXMPPComponent rendezvousXmppComponentNumberThree) {
		this.rendezvousXmppComponentNumberThree = rendezvousXmppComponentNumberThree;
	}

	public RendezvousXMPPComponent getRendezvousXmppComponent() {
		return rendezvousXmppComponent;
	}
	
	public RendezvousXMPPComponent getRendezvousXmppComponentNumberTwo() {
		return rendezvousXmppComponentNumberTwo;
	}	

	private ArrayList<XMPPClient> xmppClients = new ArrayList<XMPPClient>();

	public XMPPClient createXMPPClient() throws XMPPException {
		return createXMPPClient(null);
	}
	
	public XMPPClient createXMPPClient(String clientJID) throws XMPPException {
		int clientIndex = this.xmppClients.size();

		final String client;
		if (clientJID == null) {
			client = getClientJid(clientIndex);			
		} else {
			client = clientJID;			
		}
		final String client_pass = getClientPassword(clientIndex);

		XMPPClient xmppClient = Mockito.spy(new XMPPClient(client, client_pass,
				SERVER_HOST, SERVER_CLIENT_PORT));
		fakeServer.connect(xmppClient);
		xmppClient.process(false);
		xmppClients.add(xmppClient);

		return xmppClient;
	}

	public static String getClientJid(int clientIndex) {
		clientIndex += 100;
		return "user" + clientIndex + "@test.com";
	}

	private static String getClientPassword(int clientIndex) {
		clientIndex += 100;
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
		Mockito.when(properties.getProperty(PROP_EXPIRATION)).thenReturn(
				String.valueOf(testDefaultTimeout));
		Mockito.doReturn("").when(properties).getProperty(PROP_NEIGHBORS);
		Mockito.doReturn("").when(properties)
				.getProperty(PROP_MAX_WHOISALIVE_MANAGER_COUNT);
		Mockito.doReturn("").when(properties)
				.getProperty(PROP_MAX_WHOISALIVESYNC_MANAGER_COUNT);
		Mockito.doReturn("").when(properties)
				.getProperty(PROP_MAX_WHOISALIVESYNC_NEIGHBOR_COUNT);
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
		Mockito.when(properties.getProperty(PROP_EXPIRATION)).thenReturn(
				testDefaultTimeout + "");
		Mockito.when(properties.getProperty(PROP_NEIGHBORS)).thenReturn(
				neighborsString);
		Mockito.doReturn("").when(properties)
				.getProperty(PROP_MAX_WHOISALIVE_MANAGER_COUNT);
		Mockito.doReturn("").when(properties)
				.getProperty(PROP_MAX_WHOISALIVESYNC_MANAGER_COUNT);
		Mockito.doReturn("").when(properties)
				.getProperty(PROP_MAX_WHOISALIVESYNC_NEIGHBOR_COUNT);
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
		Mockito.when(properties.getProperty(RendezvousImpl.PROP_I_AM_ALIVE_PERIOD)).thenReturn(
				testDefaultTimeout + "");		
		Mockito.when(properties.getProperty(PROP_NEIGHBORS)).thenReturn(
				neighborsString);
		Mockito.doReturn("").when(properties)
				.getProperty(PROP_MAX_WHOISALIVE_MANAGER_COUNT);
		Mockito.doReturn("").when(properties)
				.getProperty(PROP_MAX_WHOISALIVESYNC_MANAGER_COUNT);
		Mockito.doReturn("").when(properties)
				.getProperty(PROP_MAX_WHOISALIVESYNC_NEIGHBOR_COUNT);
		RendezvousXMPPComponent comp = new RendezvousXMPPComponent(
				RENDEZVOUS_COMPONENT_URL, RENDEZVOUS_COMPONENT_PASS,
				SERVER_HOST, SERVER_COMPONENT_PORT, properties, executor);
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
		@SuppressWarnings("unused")
		Element statusEl = iq.getElement()
				.addElement("query", IAMALIVE_NAMESPACE).addElement("status");
		iq.getElement().element("query").addElement("cert").setText("cert");
		return iq;
	}

	public static ArrayList<String> getAliveIds(IQ whoIsAliveResponse)
			throws ParseException {
		ArrayList<String> aliveIds = new ArrayList<String>();
		for (RendezvousItem item : getItemsFromIQ(whoIsAliveResponse)) {
			aliveIds.add(item.getMemberId());
		}
		return aliveIds;
	}

	public static String getSetElementFromWhoIsAlive(IQ whoIsAliveResponse,
			String elementName) {
		Element queryElement = whoIsAliveResponse.getElement().element("query");
		Element setElement = queryElement.element("set");
		String element = setElement.element(elementName).getText();
		return element;
	}

	@SuppressWarnings("unchecked")
	public static LinkedList<RendezvousItem> getItemsFromIQ(
			IQ whoIsAliveResponse) throws ParseException {
		Element queryElement = whoIsAliveResponse.getElement().element("query");
		Iterator<Element> itemIterator = queryElement.elementIterator("item");
		LinkedList<RendezvousItem> aliveItems = new LinkedList<RendezvousItem>();

		while (itemIterator.hasNext()) {
			Element itemEl = (Element) itemIterator.next();
			aliveItems.add(getWhoIsAliveResponseItem(itemEl));
		}
		return aliveItems;
	}

	public static IQ createWhoIsAliveIQ() {
		return createWhoIsAliveIQ(null);
	}

	public static IQ createWhoIsAliveIQ(String after) {
		return createWhoIsAliveIQ(after, maxWhoIsAliveManagerCount);
	}
	
	public static IQ createWhoIsAliveIQNoRsm() {
		IQ iq = new IQ(Type.get);
		iq.setTo(RENDEZVOUS_COMPONENT_URL);
		@SuppressWarnings("unused")
		Element queryEl = iq.getElement().addElement("query", WHOISALIVE_NAMESPACE);
		return iq;
	}
	
	public static IQ createWhoIsAliveIQ(String after, int max) {
		IQ iq = new IQ(Type.get);
		iq.setTo(RENDEZVOUS_COMPONENT_URL);
		Element queryEl = iq.getElement().addElement("query",
				WHOISALIVE_NAMESPACE);
		Element setEl = queryEl.addElement("set", HTTP_JABBER_ORG_PROTOCOL_RSM);
		setEl.addElement("max").setText(Integer.toString(max));
		if (after != null) {
			setEl.addElement("after").setText(after);
		}
		return iq;
	}

	public static Packet createWhoIsAliveSyncIQ(String lastManager) {
		IQ iq = new IQ(Type.get);
		iq.setTo(RENDEZVOUS_COMPONENT_URL);
		Element queryEl = iq.getElement().addElement("query",
				WHOISALIVESYNC_NAMESPACE);
//		Element neighborsEl = queryEl.addElement("neighbors");
//		Element setEl = neighborsEl.addElement("set",
//				HTTP_JABBER_ORG_PROTOCOL_RSM);
//		setEl.addElement("max").setText("" + maxWhoIsAliveSyncNeighborCount);
//		if (!lastNeighbor.isEmpty()) {
//			setEl.addElement("after").setText(lastNeighbor);
//		}

		Element managersEl = queryEl.addElement("managers");
		Element setEl = managersEl.addElement("set", HTTP_JABBER_ORG_PROTOCOL_RSM);
		setEl.addElement("max").setText("" + maxWhoIsAliveSyncManagerCount);
		if (!lastManager.isEmpty()) {
			setEl.addElement("after").setText(lastManager);
		}

		return iq;
	}
	
	public static Packet createWhoIsAliveSyncIQNoRsm() {
		IQ iq = new IQ(Type.get);
		iq.setTo(RENDEZVOUS_COMPONENT_URL);
		@SuppressWarnings("unused")
		Element queryEl = iq.getElement().addElement("query", WHOISALIVESYNC_NAMESPACE);
		return iq;
	}
	
	public static IQ createWhoIsAliveSyncIQ() {
		return (IQ) createWhoIsAliveSyncIQ("");
	}

//	public String getNeighborsSetElementsFromSyncIQ(String elementName,
//			IQ syncResponse) {
//		Element queryElement = syncResponse.getElement().element("query");
//		Element neighborsEl = queryElement.element("neighbors");
//		Element setElement = neighborsEl.element("set");
//		Element elementEl = setElement.element(elementName);
//		if (elementEl == null) {
//			return null;
//		}
//		String element = elementEl.getText();
//		return element;
//	}

	public String getManagersSetElementsFromSyncIQ(String elementName,
			IQ syncResponse) {
		Element queryElement = syncResponse.getElement().element("query");
		Element managersEl = queryElement.element("managers");
		Element setElement = managersEl.element("set");
		Element elementEl = setElement.element(elementName);
		if (elementEl == null) {
			return null;
		}
		String element = elementEl.getText();
		return element;
	}

	@SuppressWarnings("unchecked")
	public static RendezvousResponseItem getItemsFromSyncIQ(IQ iq)
			throws ParseException {
		Element queryElement = iq.getElement().element("query");
		Element managersEl = queryElement.element("managers");
		Iterator<Element> managersIterator = managersEl
				.elementIterator("manager");
		List<RendezvousItem> managersAlive = new LinkedList<RendezvousItem>();
		while (managersIterator.hasNext()) {
			Element itemEl = (Element) managersIterator.next();
			managersAlive.add(getWhoIsAliveResponseItem(itemEl));
		}
		RendezvousResponseItem rendezvousItem = new RendezvousResponseItem(managersAlive);

		return rendezvousItem;
	}

	public static RendezvousItem getWhoIsAliveResponseItem(Element itemEl)
			throws ParseException {
		Attribute id = itemEl.attribute("id");
		String cert = itemEl.element("cert").getText();

		RendezvousItem item = new RendezvousItem(id.getValue(), cert);
		return item;
	}

	public RendezvousItem getRendezvousItem() {
		return new RendezvousItem("id", "cert");
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

		statusEl.addElement("updated").setText(
				"2012-10-01T09:45:00.000UTC+00:00");		
		statusEl.addElement("quiet-for").setText("1");
		
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

	public void initializeTwoXMPPRendezvousComponent(long testDefaultTimeout,
			String nameRendezvousOne, String nameRendezvousTwo)
			throws Exception {	
		initializeTwoXMPPRendezvousComponent(testDefaultTimeout,
				nameRendezvousOne, nameRendezvousTwo, null);
	}
	
	public void initializeTwoXMPPRendezvousComponent(long testDefaultTimeout,
			String nameRendezvousOne, String nameRendezvousTwo, String nameRendezvousThree)
			throws Exception {				
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
		
		Properties properties = Mockito.mock(Properties.class);
		Mockito.when(properties.getProperty(PROP_EXPIRATION)).thenReturn(
				testDefaultTimeout + "");
		Mockito.when(properties.getProperty(RendezvousImpl.PROP_I_AM_ALIVE_PERIOD))
				.thenReturn(testDefaultTimeout + "");
		Mockito.when(properties.getProperty(RendezvousImpl.PROP_I_AM_ALIVE_MAX_MESSAGE_LOST))
		        .thenReturn(String.valueOf(DEFAULT_WAIT_FREQUENCY_TIMES));		
		Mockito.when(properties.getProperty(PROP_NEIGHBORS)).thenReturn(
				nameRendezvousTwo);
		Mockito.doReturn("").when(properties)
				.getProperty(PROP_MAX_WHOISALIVE_MANAGER_COUNT);
		Mockito.doReturn("").when(properties)
				.getProperty(PROP_MAX_WHOISALIVESYNC_MANAGER_COUNT);
		Mockito.doReturn("").when(properties)
				.getProperty(PROP_MAX_WHOISALIVESYNC_NEIGHBOR_COUNT);
						
		RendezvousXMPPComponent compOne = new RendezvousXMPPComponent(
				nameRendezvousOne, RENDEZVOUS_COMPONENT_PASS,
				SERVER_HOST, SERVER_COMPONENT_PORT, properties, executor);
		rendezvousXmppComponent = Mockito.spy(compOne);
		((RendezvousImpl) compOne.getRendezvous())
				.setPacketSender(rendezvousXmppComponent);
		rendezvousXmppComponent.setDescription("Rendezvous Component One");
		rendezvousXmppComponent.setName("rendezvous one");
		fakeServer.connect(rendezvousXmppComponent);
		rendezvousXmppComponent.process();
		
		rendezvousXmppComponent.getRendezvous();
		
		RendezvousXMPPComponent compTwo = new RendezvousXMPPComponent(
				nameRendezvousTwo, RENDEZVOUS_COMPONENT_PASS,
				SERVER_HOST, SERVER_COMPONENT_PORT, properties, executor);
		rendezvousXmppComponentNumberTwo = Mockito.spy(compTwo);
		((RendezvousImpl) compTwo.getRendezvous())
				.setPacketSender(rendezvousXmppComponent);
		rendezvousXmppComponentNumberTwo.setDescription("Rendezvous Component Tow");
		rendezvousXmppComponentNumberTwo.setName("rendezvous two");
		fakeServer.connect(rendezvousXmppComponentNumberTwo);
		rendezvousXmppComponentNumberTwo.process();
		
		if (nameRendezvousThree != null) {
						
			properties = Mockito.mock(Properties.class);
			Mockito.when(properties.getProperty(PROP_EXPIRATION)).thenReturn(
					testDefaultTimeout + "");
			Mockito.when(properties.getProperty(PROP_NEIGHBORS)).thenReturn(
					nameRendezvousTwo + "," + nameRendezvousOne);
			Mockito.doReturn("").when(properties)
					.getProperty(PROP_MAX_WHOISALIVE_MANAGER_COUNT);
			Mockito.doReturn("").when(properties)
					.getProperty(PROP_MAX_WHOISALIVESYNC_MANAGER_COUNT);
			Mockito.doReturn("").when(properties)
					.getProperty(PROP_MAX_WHOISALIVESYNC_NEIGHBOR_COUNT);			
			
			RendezvousXMPPComponent compThree = new RendezvousXMPPComponent(
					nameRendezvousThree, RENDEZVOUS_COMPONENT_PASS,
					SERVER_HOST, SERVER_COMPONENT_PORT, properties, executor);
			rendezvousXmppComponentNumberThree = Mockito.spy(compThree);
			((RendezvousImpl) compThree.getRendezvous())
					.setPacketSender(rendezvousXmppComponent);
			rendezvousXmppComponentNumberThree.setDescription("Rendezvous Component Three");
			rendezvousXmppComponentNumberThree.setName("rendezvous Three");
			fakeServer.connect(rendezvousXmppComponentNumberThree);
			rendezvousXmppComponentNumberThree.process();
		}
		
	}
}
