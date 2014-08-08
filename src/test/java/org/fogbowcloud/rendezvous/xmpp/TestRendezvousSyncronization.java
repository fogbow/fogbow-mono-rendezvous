package org.fogbowcloud.rendezvous.xmpp;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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

public class TestRendezvousSyncronization {

	private RendezvousTestHelper rendezvousTestHelper;
	private static final long SEMAPHORE_TIMEOUT = 20L; // In minutes
	private static final int XMPP_CLIENT_COUNT = 200;

	@Before
	public void setUp() throws ComponentException {
		rendezvousTestHelper = new RendezvousTestHelper();
	}

	@Test
	public void testWhoIsAliveiqResponse() throws Exception {
		ScheduledExecutorService executor = Mockito
				.mock(ScheduledExecutorService.class);
		
		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, new String[] {},executor);
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
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, new String[] {},executor);
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
				Mockito.anyLong(), Mockito.anyLong(), Mockito.any(TimeUnit.class));
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
		//sending i am alives
		sendIAmAlives(XMPP_CLIENT_COUNT);
		//sending whois alive sync
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
		for (int i = 1; i <= 199; i++) {
			neighborsList.add("neighbor" + i);
		}
		String[] neighbors = new String[neighborsList.size()];
		neighbors = neighborsList.toArray(neighbors);
		ScheduledExecutorService executor = Mockito
				.mock(ScheduledExecutorService.class);
		
		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, neighbors, executor);
		//sending i am alives
		sendIAmAlives(XMPP_CLIENT_COUNT-1);
		//sending whois alive sync
		IQ iq = RendezvousTestHelper.createWhoIsAliveSyncIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
		RendezvousResponseItem itemsAlive = RendezvousTestHelper
				.getItemsFromSyncIQ(response);
		Assert.assertEquals(100, itemsAlive.getManagers().size());
		Assert.assertEquals(100, itemsAlive.getNeighbors().size());
		
		String lastManager = rendezvousTestHelper.getManagersSetElementsFromSyncIQ("last",response);
		String lastNeighbor = rendezvousTestHelper.getNeighborsSetElementsFromSyncIQ("last", response);
		response = (IQ) xmppClient.syncSend(RendezvousTestHelper.createWhoIsAliveSyncIQ(lastManager, lastNeighbor));
		RendezvousResponseItem itemsAlive2 = RendezvousTestHelper.getItemsFromSyncIQ(response);
		Assert.assertEquals(99, itemsAlive2.getManagers().size());
		// Number of neighbors + client added to neighbors List
		Assert.assertEquals(100, itemsAlive2.getNeighbors().size());
		Assert.assertNotEquals(itemsAlive.getManagers(),itemsAlive2.getManagers());
		Assert.assertNotEquals(itemsAlive.getNeighbors(),itemsAlive2.getNeighbors());
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
}
