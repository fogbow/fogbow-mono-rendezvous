package org.fogbowcloud.rendezvous.xmpp;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.fogbowcloud.rendezvous.core.RendezvousImpl;
import org.fogbowcloud.rendezvous.core.RendezvousTestHelper;
import org.fogbowcloud.rendezvous.xmpp.model.RendezvousResponseItem;
import org.jamppa.client.XMPPClient;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmpp.component.ComponentException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.Packet;

public class TestRendezvousSyncronization {

	private RendezvousTestHelper rendezvousTestHelper;
	private static final long SEMAPHORE_TIMEOUT = (long) 1; // In minutes

	@Before
	public void setUp() throws ComponentException {
		rendezvousTestHelper = new RendezvousTestHelper();
	}

	@Test
	public void testWhoIsAliveiqResponse() throws XMPPException,
			ComponentException {
		rendezvousTestHelper
				.initializeXMPPRendezvousComponent(RendezvousTestHelper.TEST_DEFAULT_TIMEOUT);
		IQ response;
		IQ iq = RendezvousTestHelper.createWhoIsAliveSyncIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
	}

	@Test
	public void testWhoIsAliveResponseNoNeighbors() throws XMPPException,
			ParseException, ComponentException {
		rendezvousTestHelper
				.initializeXMPPRendezvousComponent(RendezvousTestHelper.TEST_DEFAULT_TIMEOUT);
		IQ response;
		IQ iq = RendezvousTestHelper.createWhoIsAliveSyncIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();

		response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
		RendezvousResponseItem itemsAlive = RendezvousTestHelper
				.getItemsFromSyncIQ(response);
		Assert.assertEquals(0, itemsAlive.getKnownManagersAlive().size());
		Assert.assertEquals(0, itemsAlive.getNeighbors().size());
	}

	@Test
	public void testWhoIsAliveResponseNoManagers1Neighbor()
			throws XMPPException, ParseException, ComponentException {
		String[] neighbors = new String[] { RendezvousTestHelper.NEIGHBOR_CLIENT_JID };
		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, neighbors);
		IQ response;
		IQ iq = RendezvousTestHelper.createWhoIsAliveSyncIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();

		response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
		RendezvousResponseItem itemsAlive = RendezvousTestHelper
				.getItemsFromSyncIQ(response);
		Assert.assertEquals(0, itemsAlive.getKnownManagersAlive().size());
		Assert.assertEquals(1, itemsAlive.getNeighbors().size());
	}

	@Test
	public void testWhoIsAlive1Neighbors1manager2Neighbors()
			throws XMPPException, ParseException, ComponentException {
		String[] neighbors = new String[] {
				RendezvousTestHelper.NEIGHBOR_CLIENT_JID, "aabc" };
		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, neighbors);
		IQ iq1 = RendezvousTestHelper.createIAmAliveIQ();
		XMPPClient xmppClient1 = rendezvousTestHelper.createXMPPClient();
		xmppClient1.syncSend(iq1);

		IQ iq = RendezvousTestHelper.createWhoIsAliveSyncIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
		RendezvousResponseItem itemsAlive = RendezvousTestHelper
				.getItemsFromSyncIQ(response);
		Assert.assertEquals(1, itemsAlive.getKnownManagersAlive().size());
		Assert.assertEquals(2, itemsAlive.getNeighbors().size());
	}

	@Test
	public void testSendWhoIsAlive() throws XMPPException,
			InterruptedException, ComponentException {
		String[] neighbors = new String[] { RendezvousTestHelper.NEIGHBOR_CLIENT_JID };
		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, neighbors);

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
		rendezvous.setNeighborIds(new HashSet<String>(Arrays.asList(xmppClient
				.getJid().toFullJID())));
		((RendezvousImpl) rendezvousTestHelper.getRendezvousXmppComponent()
				.getRendezvous()).syncWhoIsAlive();

		boolean receivedAll = semaphore.tryAcquire(SEMAPHORE_TIMEOUT,
				TimeUnit.MINUTES);
		Assert.assertTrue(receivedAll);
		Assert.assertEquals(2, ((RendezvousImpl) rendezvousTestHelper
				.getRendezvousXmppComponent().getRendezvous()).getNeighborIds()
				.size());
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
