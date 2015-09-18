package org.fogbowcloud.rendezvous.xmpp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.RendezvousImpl;
import org.fogbowcloud.rendezvous.core.RendezvousTestHelper;
import org.fogbowcloud.rendezvous.xmpp.model.RendezvousResponseItem;
import org.jamppa.client.XMPPClient;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.xmpp.component.ComponentException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

public class TestRendezvousSyncronization {

	private RendezvousTestHelper rendezvousTestHelper;
	private static final long SEMAPHORE_TIMEOUT = 20L; // In minutes
	private static final int XMPP_CLIENT_COUNT = 200;

	@Before
	public void setUp() throws ComponentException {
		rendezvousTestHelper = new RendezvousTestHelper();
		rendezvousTestHelper.setMaxWhoIsAliveManagerCount(100);
		rendezvousTestHelper.setMaxWhoIsAliveSyncManagerCount(100);
		rendezvousTestHelper.setMaxWhoIsAliveSyncNeighborCount(100);
	}

	@Test
	public void testWhoIsAliveiqResponse() throws Exception {
		ScheduledExecutorService executor = Mockito
				.mock(ScheduledExecutorService.class);

		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, new String[] {},
				executor);
		IQ response;
		IQ iq = RendezvousTestHelper.createWhoIsAliveSyncIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
	}
	
	
	@Test
	public void testWhoIsAliveResponseNoNeighbors() throws Exception {
		ScheduledExecutorService executor = Mockito
				.mock(ScheduledExecutorService.class);

		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, new String[] {},
				executor);
		IQ response;
		IQ iq = RendezvousTestHelper.createWhoIsAliveSyncIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();

		response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
		RendezvousResponseItem itemsAlive = RendezvousTestHelper
				.getItemsFromSyncIQ(response);
		Assert.assertEquals(0, itemsAlive.getManagers().size());
		Assert.assertEquals(0, itemsAlive.getNeighbors().size());
	}

	@Test
	public void testWhoIsAliveResponseNoManagers1Neighbor() throws Exception {
		String[] neighbors = new String[] { RendezvousTestHelper.NEIGHBOR_CLIENT_JID };
		ScheduledExecutorService executor = Mockito
				.mock(ScheduledExecutorService.class);

		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, neighbors, executor);
		IQ response;
		IQ iq = RendezvousTestHelper.createWhoIsAliveSyncIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();

		response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
		RendezvousResponseItem itemsAlive = RendezvousTestHelper
				.getItemsFromSyncIQ(response);
		Assert.assertEquals(0, itemsAlive.getManagers().size());
		Assert.assertEquals(1, itemsAlive.getNeighbors().size());
	}

	@Test
	public void testWhoIsAlive1Neighbors1manager2Neighbors() throws Exception {
		String[] neighbors = new String[] {
				RendezvousTestHelper.NEIGHBOR_CLIENT_JID, "aabc" };
		ScheduledExecutorService executor = Mockito
				.mock(ScheduledExecutorService.class);

		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, neighbors, executor);
		IQ iq1 = RendezvousTestHelper.createIAmAliveIQ();
		XMPPClient xmppClient1 = rendezvousTestHelper.createXMPPClient();
		xmppClient1.syncSend(iq1);

		IQ iq = RendezvousTestHelper.createWhoIsAliveSyncIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
		RendezvousResponseItem itemsAlive = RendezvousTestHelper
				.getItemsFromSyncIQ(response);
		Assert.assertEquals(1, itemsAlive.getManagers().size());
		Assert.assertEquals(2, itemsAlive.getNeighbors().size());
	}

	@Test
	public void testSendWhoIsAlive() throws Exception {
		String[] neighbors = new String[] { RendezvousTestHelper.NEIGHBOR_CLIENT_JID };
		ScheduledExecutorService executor = Mockito
				.mock(ScheduledExecutorService.class);

		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, neighbors, executor);

		final XMPPClient xmppClient = rendezvousTestHelper
				.createNeighborClient();
		final Semaphore semaphore = new Semaphore(0);

		final PacketListener callback = new PacketListener() {
			public void processPacket(Packet packet) {
				semaphore.release();
				xmppClient.send(rendezvousTestHelper
						.createWhoIsAliveSyncResponse((IQ) packet));
			}
		};

		xmppClient.on(new PacketFilter() {
			@Override
			public boolean accept(Packet packet) {
				boolean from = packet
						.getFrom()
						.toBareJID()
						.equals(rendezvousTestHelper
								.getRendezvousXmppComponent().getJID()
								.toBareJID());
				boolean namespace = packet
						.getElement()
						.element("query")
						.getNamespaceURI()
						.equals(RendezvousPacketHelper.WHOISALIVESYNCH_NAMESPACE);
				return from && namespace;
			}
		}, callback);
		RendezvousImpl rendezvous = (RendezvousImpl) rendezvousTestHelper
				.getRendezvousXmppComponent().getRendezvous();
		rendezvous.syncNeighbors();

		boolean receivedAll = semaphore.tryAcquire(SEMAPHORE_TIMEOUT,
				TimeUnit.SECONDS);
		Assert.assertTrue(receivedAll);
		Assert.assertEquals(2, rendezvous.getNeighborIds().size());
	}

	@Test
	public void testContinuousSync() throws Exception {
		String[] neighbors = new String[] { RendezvousTestHelper.NEIGHBOR_CLIENT_JID };

		final XMPPClient xmppClient = rendezvousTestHelper
				.createNeighborClient();
		final Semaphore semaphore = new Semaphore(0);

		final PacketListener callback = new PacketListener() {
			public void processPacket(Packet packet) {
				semaphore.release();
				xmppClient.send(rendezvousTestHelper
						.createWhoIsAliveSyncResponse((IQ) packet));
			}
		};

		xmppClient.on(new PacketFilter() {
			@Override
			public boolean accept(Packet packet) {
				boolean from = packet
						.getFrom()
						.toBareJID()
						.equals(rendezvousTestHelper
								.getRendezvousXmppComponent().getJID()
								.toBareJID());
				boolean namespace = packet
						.getElement()
						.element("query")
						.getNamespaceURI()
						.equals(RendezvousPacketHelper.WHOISALIVESYNCH_NAMESPACE);
				return from && namespace;
			}
		}, callback);

		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, neighbors);
		boolean receivedAll = semaphore.tryAcquire(SEMAPHORE_TIMEOUT,
				TimeUnit.SECONDS);
		Assert.assertTrue(receivedAll);
	}

	@Test
	public void testContinuousSyncWithMock() throws Exception {
		String[] neighbors = new String[] { RendezvousTestHelper.NEIGHBOR_CLIENT_JID };
		ScheduledExecutorService executor = Mockito
				.mock(ScheduledExecutorService.class);
		executor.scheduleAtFixedRate(Mockito.isA(Runnable.class),
				Mockito.anyLong(), Mockito.anyLong(),
				Mockito.any(TimeUnit.class));
		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, neighbors, executor);
		Mockito.verify(executor);
	}

	@Test
	public void testWhoIsAliveSyncResponsePagination() throws Exception {
		List<String> neighborsList = new LinkedList<String>();
		for (int i = 1; i <= 200; i++) {
			neighborsList.add("neighbor" + i);
		}
		String[] neighbors = new String[neighborsList.size()];
		neighbors = neighborsList.toArray(neighbors);
		ScheduledExecutorService executor = Mockito
				.mock(ScheduledExecutorService.class);

		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, neighbors, executor);
		// sending i am alives
		sendIAmAlives(XMPP_CLIENT_COUNT);
		// sending whois alive sync
		IQ iq = RendezvousTestHelper.createWhoIsAliveSyncIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
		RendezvousResponseItem itemsAlive = RendezvousTestHelper
				.getItemsFromSyncIQ(response);
		Assert.assertEquals(100, itemsAlive.getManagers().size());
		Assert.assertEquals(100, itemsAlive.getNeighbors().size());
	}

	@Test
	public void testWhoIsAliveSyncSecondPageResponse() throws Exception {
		List<String> neighborsList = new LinkedList<String>();
		for (int i = 101; i <= 299; i++) {
			neighborsList.add("neighbor" + i);
		}
		String[] neighbors = new String[neighborsList.size()];
		neighbors = neighborsList.toArray(neighbors);
		ScheduledExecutorService executor = Mockito
				.mock(ScheduledExecutorService.class);

		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, neighbors, executor);
		// sending i am alives
		sendIAmAlives(XMPP_CLIENT_COUNT - 1);
		// sending whois alive sync
		IQ iq = RendezvousTestHelper.createWhoIsAliveSyncIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
		RendezvousResponseItem itemsAlive = RendezvousTestHelper
				.getItemsFromSyncIQ(response);
		Assert.assertEquals(100, itemsAlive.getManagers().size());
		Assert.assertEquals(100, itemsAlive.getNeighbors().size());

		String lastNeighbor = rendezvousTestHelper
				.getNeighborsSetElementsFromSyncIQ("last", response);
		Assert.assertEquals("neighbor101", rendezvousTestHelper
				.getNeighborsSetElementsFromSyncIQ("first", response));
		Assert.assertEquals("neighbor200", lastNeighbor);

		String lastManager = rendezvousTestHelper
				.getManagersSetElementsFromSyncIQ("last", response);

		response = (IQ) xmppClient.syncSend(RendezvousTestHelper
				.createWhoIsAliveSyncIQ(lastManager, lastNeighbor));
		RendezvousResponseItem itemsAlive2 = RendezvousTestHelper
				.getItemsFromSyncIQ(response);
		Assert.assertEquals(99, itemsAlive2.getManagers().size());
		Assert.assertEquals(100, itemsAlive2.getNeighbors().size());

		Assert.assertNotEquals(itemsAlive.getManagers(),
				itemsAlive2.getManagers());
		Assert.assertNotEquals(itemsAlive.getNeighbors(),
				itemsAlive2.getNeighbors());
		Assert.assertEquals("neighbor201", rendezvousTestHelper
				.getNeighborsSetElementsFromSyncIQ("first", response));
		Assert.assertEquals("user299@test.com", rendezvousTestHelper
				.getNeighborsSetElementsFromSyncIQ("last", response));
	}

	@Test
	public void testWhoIsAliveSyncRSMReturnEmptyPage() throws Exception {
		List<String> neighborsList = new LinkedList<String>();
		for (int i = 101; i <= 299; i++) {
			neighborsList.add("neighbor" + i);
		}
		String[] neighbors = new String[neighborsList.size()];
		neighbors = neighborsList.toArray(neighbors);
		ScheduledExecutorService executor = Mockito
				.mock(ScheduledExecutorService.class);

		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, neighbors, executor);
		// sending i am alives
		sendIAmAlives(XMPP_CLIENT_COUNT - 1);
		// sending whois alive sync
		IQ iq = RendezvousTestHelper.createWhoIsAliveSyncIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response = (IQ) xmppClient.syncSend(iq);

		String lastNeighbor = rendezvousTestHelper
				.getNeighborsSetElementsFromSyncIQ("last", response);
		String lastManager = rendezvousTestHelper
				.getManagersSetElementsFromSyncIQ("last", response);
		response = (IQ) xmppClient.syncSend(RendezvousTestHelper
				.createWhoIsAliveSyncIQ(lastManager, lastNeighbor));
		lastNeighbor = rendezvousTestHelper.getNeighborsSetElementsFromSyncIQ(
				"last", response);
		lastManager = rendezvousTestHelper.getManagersSetElementsFromSyncIQ(
				"last", response);
		response = (IQ) xmppClient.syncSend(RendezvousTestHelper
				.createWhoIsAliveSyncIQ(lastManager, lastNeighbor));
		Assert.assertNull(rendezvousTestHelper
				.getNeighborsSetElementsFromSyncIQ("last", response));
		Assert.assertEquals("0", rendezvousTestHelper
				.getNeighborsSetElementsFromSyncIQ("count", response));
	}
	
	@Test
	public void testWhoIsAliveSyncRSMReturnItemNotFoundError() throws Exception {
		List<String> neighborsList = new LinkedList<String>();
		for (int i = 101; i <= 299; i++) {
			neighborsList.add("neighbor" + i);
		}
		String[] neighbors = new String[neighborsList.size()];
		neighbors = neighborsList.toArray(neighbors);
		ScheduledExecutorService executor = Mockito
				.mock(ScheduledExecutorService.class);

		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, neighbors, executor);
		// sending i am alives
		sendIAmAlives(XMPP_CLIENT_COUNT - 1);
		// sending whois alive sync
		IQ iq = RendezvousTestHelper.createWhoIsAliveSyncIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response = (IQ) xmppClient.syncSend(iq);
		
		try {
			response = (IQ) xmppClient.syncSend(RendezvousTestHelper
					.createWhoIsAliveSyncIQ("NotFoundManager", "NotFoundNeighbor"));
			Assert.fail();
		} catch(XMPPException e) {
			Assert.assertTrue(PacketError.Type.cancel.equals(e.getXMPPError().getType()));
		}
	}
	
	@Test
	public void testWhoIsAliveSyncRSMItemCount0() throws Exception {
		List<String> neighborsList = new LinkedList<String>();
		for (int i = 101; i <= 299; i++) {
			neighborsList.add("neighbor" + i);
		}
		String[] neighbors = new String[neighborsList.size()];
		neighbors = neighborsList.toArray(neighbors);
		ScheduledExecutorService executor = Mockito
				.mock(ScheduledExecutorService.class);
		
		RendezvousTestHelper.setMaxWhoIsAliveSyncManagerCount(0);
		RendezvousTestHelper.setMaxWhoIsAliveSyncNeighborCount(0);
		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, neighbors, executor);
		// sending i am alives
		sendIAmAlives(XMPP_CLIENT_COUNT - 1);
		// sending whois alive sync
		IQ iq = RendezvousTestHelper.createWhoIsAliveSyncIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response = (IQ) xmppClient.syncSend(iq);

		Assert.assertNull(rendezvousTestHelper
				.getNeighborsSetElementsFromSyncIQ("last", response));
		Assert.assertNull(rendezvousTestHelper
				.getNeighborsSetElementsFromSyncIQ("first", response));
		Assert.assertNull(rendezvousTestHelper
				.getManagersSetElementsFromSyncIQ("first", response));
		Assert.assertNull(rendezvousTestHelper
				.getManagersSetElementsFromSyncIQ("last", response));
		Assert.assertEquals("0", rendezvousTestHelper
				.getNeighborsSetElementsFromSyncIQ("count", response));
		Assert.assertEquals("0", rendezvousTestHelper
				.getManagersSetElementsFromSyncIQ("count", response));
	}
	
	@Test
	public void testWhoIsAliveSyncNoRsm() throws Exception {
		List<String> neighborsList = new LinkedList<String>();
		for (int i = 1; i <= 200; i++) {
			neighborsList.add("neighbor" + i);
		}
		String[] neighbors = new String[neighborsList.size()];
		neighbors = neighborsList.toArray(neighbors);
		ScheduledExecutorService executor = Mockito
				.mock(ScheduledExecutorService.class);

		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, neighbors, executor);
		// sending i am alives
		sendIAmAlives(XMPP_CLIENT_COUNT);
		// sending whois alive sync
		IQ iq = (IQ) RendezvousTestHelper.createWhoIsAliveSyncIQNoRsm();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
		RendezvousResponseItem itemsAlive = RendezvousTestHelper
				.getItemsFromSyncIQ(response);
		Assert.assertEquals(100, itemsAlive.getManagers().size());
		Assert.assertEquals(100, itemsAlive.getNeighbors().size());
	}
	
	private void sendIAmAlives(int numberOfClients) throws InterruptedException {
		final Semaphore semaphore = new Semaphore(0);

		final PacketListener callback = new PacketListener() {
			public void processPacket(Packet packet) {
				semaphore.release();
			}
		};

		for (int i = 0; i < numberOfClients; i++) {
			XMPPClient xmppClient = null;
			try {
				xmppClient = rendezvousTestHelper.createXMPPClient();
			} catch (XMPPException e) {
				Assert.fail(e.getMessage());
			}
			IQ iq = RendezvousTestHelper.createIAmAliveIQ();
			xmppClient.on(new PacketIDFilter(iq.getID()), callback);
			xmppClient.send(iq);
		}

		boolean receivedAll = semaphore.tryAcquire(numberOfClients,
				SEMAPHORE_TIMEOUT, TimeUnit.MINUTES);
		Assert.assertTrue(receivedAll);
	}

	@After
	public void tearDown() {
		rendezvousTestHelper.disconnectXMPPClients();
		try {
			rendezvousTestHelper.disconnectRendezvousXMPPComponent();
		} catch (ComponentException e) {
		}
	}

	@Test
	public void testSync2Rendezvous() throws Exception {
		int testDefaultTimeout = 1000;

		String nameRendezvousOne = RendezvousTestHelper.RENDEZVOUS_COMPONENT_URL;
		String nameRendezvousTwo = "rendezvous2.test.com";

		rendezvousTestHelper.initializeTwoXMPPRendezvousComponent(testDefaultTimeout,
				nameRendezvousOne, nameRendezvousTwo);

		XMPPClient xmppClient1 = rendezvousTestHelper.createXMPPClient();
		XMPPClient xmppClient2 = rendezvousTestHelper.createXMPPClient();
		XMPPClient xmppClient3 = rendezvousTestHelper.createXMPPClient();

		IQ iqRendezvousOne = createIAmAliveIQ(nameRendezvousOne);
		IQ iqRendezvousTwo = createIAmAliveIQ(nameRendezvousTwo);

		// IAmALive Rendezvous One
		xmppClient1.syncSend(iqRendezvousOne);
		// IAmALive Rendezvous One
		xmppClient2.syncSend(iqRendezvousOne);

		IQ iqWho = RendezvousTestHelper.createWhoIsAliveIQ();
		ArrayList<String> aliveIDs = RendezvousTestHelper.getAliveIds((IQ) xmppClient1
				.syncSend(iqWho));
		Assert.assertEquals(2, aliveIDs.size());

		// IAmALive Rendezvous Two
		xmppClient3.syncSend(iqRendezvousTwo);

		RendezvousImpl rendezvousOne = (RendezvousImpl) rendezvousTestHelper
				.getRendezvousXmppComponent().getRendezvous();
		RendezvousImpl rendezvousTwo = (RendezvousImpl) rendezvousTestHelper
				.getRendezvousXmppComponentNumberTwo().getRendezvous();
		// First cycle of checking and synchronization
		checkAndSyncBothRendezvous(rendezvousOne, rendezvousTwo);

		aliveIDs = RendezvousTestHelper.getAliveIds((IQ) xmppClient1.syncSend(iqWho));
		Assert.assertEquals(3, aliveIDs.size());

		// Expected to expire
		Thread.sleep(testDefaultTimeout);

		// IAmALive Rendezvous One
		rendezvousTestHelper.createXMPPClient().syncSend(iqRendezvousOne);
		// IAmALive Rendezvous One
		rendezvousTestHelper.createXMPPClient().syncSend(iqRendezvousOne);
		// Second cycle of checking and synchronization
		checkAndSyncBothRendezvous(rendezvousOne, rendezvousTwo);

		aliveIDs = RendezvousTestHelper.getAliveIds((IQ) xmppClient1.syncSend(iqWho));
		Assert.assertEquals(2, aliveIDs.size());
	}

	private void checkAndSyncBothRendezvous(RendezvousImpl rendezvousOne,
			RendezvousImpl rendezvousTwo) {
		rendezvousOne.checkExpiredAliveIDs();
		rendezvousOne.syncNeighbors();
		rendezvousTwo.checkExpiredAliveIDs();
		rendezvousTwo.syncNeighbors();
	}

	private IQ createIAmAliveIQ(String nameRendezvous) {
		IQ iq2 = new IQ(Type.get);
		iq2.setTo(nameRendezvous);
		Element statusEl2 = iq2.getElement()
				.addElement("query", "http://fogbowcloud.org/rendezvous/iamalive")
				.addElement("status");
		iq2.getElement().element("query").addElement("cert").setText("cert");
		statusEl2.addElement("cpu-idle").setText("1");
		statusEl2.addElement("cpu-inuse").setText("2");
		statusEl2.addElement("mem-idle").setText("3");
		statusEl2.addElement("mem-inuse").setText("4");
		statusEl2.addElement("instances-idle").setText("5");
		statusEl2.addElement("instances-inuse").setText("6");		
		return iq2;
	}
}
