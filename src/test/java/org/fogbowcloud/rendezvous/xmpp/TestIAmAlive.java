package org.fogbowcloud.rendezvous.xmpp;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.fogbowcloud.rendezvous.xmpp.model.RendezvousTestHelper;
import org.jamppa.client.XMPPClient;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.filter.ToContainsFilter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmpp.component.ComponentException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.Packet;

public class TestIAmAlive {

	private RendezvousTestHelper rendezvousTestHelper;

	@Before
	public void setUp() throws ComponentException {
		rendezvousTestHelper = new RendezvousTestHelper();
		rendezvousTestHelper
				.initializeXMPPRendezvousComponent(RendezvousTestHelper.TEST_DEFAULT_TIMEOUT);
	}

	@Test(expected = XMPPException.class)
	public void testInvalidIQ() throws XMPPException {
		String invalidNamespace = "invalidnamespace";
		IQ iq = new IQ(Type.get);
		iq.setTo(RendezvousTestHelper.RENDEZVOUS_COMPONENT_URL);
		iq.getElement().addElement("query", invalidNamespace);
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response = (IQ) xmppClient.syncSend(iq);
	}

	@Test
	public void testSyncImAliveSingleElement() throws XMPPException {
		IQ response;
		IQ iq = rendezvousTestHelper.createIAmAliveIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();

		response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());

		iq = rendezvousTestHelper.createWhoIsAliveIQ();
		response = (IQ) xmppClient.syncSend(iq);
		ArrayList<String> aliveIDs = rendezvousTestHelper.getAliveIds(response);

		Assert.assertTrue(aliveIDs.contains(rendezvousTestHelper
				.getClientName(0)));
		Assert.assertEquals(1, aliveIDs.size());
	}

	@Test
	public void testImAsyncAliveSingleElement() throws XMPPException {
		IQ iq = rendezvousTestHelper.createIAmAliveIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();

		PacketFilter filter = new AndFilter(
				new PacketTypeFilter(IQ.class),
				new ToContainsFilter(
						rendezvousTestHelper
								.getClientName(0)));
		PacketListener callback = new PacketListener() {
			public void processPacket(Packet packet) {
				IQ response = (IQ) packet;
				Assert.assertEquals(Type.result, response.getType());
			}
		};
		xmppClient.on(filter, callback);
		xmppClient.send(iq);
	}

	@Test
	public void testIamAlive2EqualElements() throws XMPPException {
		IQ response;
		IQ iq = rendezvousTestHelper.createIAmAliveIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();

		response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
		iq = rendezvousTestHelper.createIAmAliveIQ();
		response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());

		iq = rendezvousTestHelper.createWhoIsAliveIQ();
		response = (IQ) xmppClient.syncSend(iq);
		ArrayList<String> aliveIDs = rendezvousTestHelper.getAliveIds(response);
		Assert.assertEquals(1, aliveIDs.size());
		Assert.assertTrue(aliveIDs.contains(rendezvousTestHelper
				.getClientName(0)));
	}

	@Test
	public void testIamAlive2Clients() throws XMPPException {
		XMPPClient xmppClient1 = rendezvousTestHelper.createXMPPClient();
		XMPPClient xmppClient2 = rendezvousTestHelper.createXMPPClient();
		IQ iq = rendezvousTestHelper.createIAmAliveIQ();

		IQ response = (IQ) xmppClient1.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
		response = (IQ) xmppClient2.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());

		iq = rendezvousTestHelper.createWhoIsAliveIQ();
		response = (IQ) xmppClient1.syncSend(iq);
		ArrayList<String> aliveIDs = rendezvousTestHelper.getAliveIds(response);
		Assert.assertEquals(2, aliveIDs.size());
		Assert.assertTrue(aliveIDs.contains(rendezvousTestHelper
				.getClientName(0)));
		Assert.assertTrue(aliveIDs.contains(rendezvousTestHelper
				.getClientName(1)));
	}

	@Test
	public void testIamAliveManyClients() throws XMPPException, ComponentException {
		rendezvousTestHelper
				.initializeXMPPRendezvousComponent(10000 * RendezvousTestHelper.TEST_DEFAULT_TIMEOUT);
		int numberOfXmppClients = 800;

		for (int i = 0; i < numberOfXmppClients; i++) {
			XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
			IQ iq = rendezvousTestHelper.createIAmAliveIQ();
			String nameClient = rendezvousTestHelper.getClientName(i);
			PacketFilter filter = new AndFilter(new PacketTypeFilter(IQ.class),
					new ToContainsFilter(nameClient));

			PacketListener callback = new PacketListener() {
				public void processPacket(Packet packet) {
					IQ response = (IQ) packet;
					Assert.assertEquals(Type.result, response.getType());
				}
			};
			xmppClient.on(filter, callback);
			xmppClient.send(iq);
		}

		XMPPClient xmppClient2 = rendezvousTestHelper.createXMPPClient();
		IQ iq = rendezvousTestHelper.createWhoIsAliveIQ();
		IQ response = (IQ) xmppClient2.syncSend(iq);
		ArrayList<String> aliveIDs = rendezvousTestHelper.getAliveIds(response);
		for (int i = 0; i < numberOfXmppClients; i++) {
			String user = rendezvousTestHelper.getClientName(i);
			Assert.assertTrue(aliveIDs.contains(user));
		}
		Assert.assertEquals(numberOfXmppClients, aliveIDs.size());
	}

	@Test
	public void testIamAliveManyClientsWithSemaphore()
			throws InterruptedException, XMPPException, ComponentException {
		rendezvousTestHelper
				.initializeXMPPRendezvousComponent(10000 * RendezvousTestHelper.TEST_DEFAULT_TIMEOUT);
		int numberOfXmppClients = 1000;
		final Semaphore semaphore = new Semaphore(0);
		final long TIMEOUT_ALL_RESPONSE = 60000;

		for (int i = 0; i < numberOfXmppClients; i++) {
			XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
			IQ iq = rendezvousTestHelper.createIAmAliveIQ();
			String nameClient = rendezvousTestHelper.getClientName(i);
			PacketFilter filter = new AndFilter(new PacketTypeFilter(IQ.class),
					new ToContainsFilter(nameClient));

			PacketListener callback = new PacketListener() {
				public void processPacket(Packet packet) {
					IQ response = (IQ) packet;
					semaphore.release();
					Assert.assertEquals(Type.result, response.getType());
				}
			};
			xmppClient.on(filter, callback);
			xmppClient.send(iq);
		}
		
		boolean receivedAllResults = false;
		receivedAllResults = semaphore.tryAcquire(numberOfXmppClients,
				TIMEOUT_ALL_RESPONSE, TimeUnit.MILLISECONDS);

		Assert.assertTrue(receivedAllResults);
		XMPPClient xmppClient2 = rendezvousTestHelper.createXMPPClient();
		IQ iq = rendezvousTestHelper.createWhoIsAliveIQ();
		IQ response = (IQ) xmppClient2.syncSend(iq);
		ArrayList<String> aliveIDs = rendezvousTestHelper.getAliveIds(response);
		for (int i = 0; i < numberOfXmppClients; i++) {
			String user = rendezvousTestHelper.getClientName(i);
			Assert.assertTrue(aliveIDs.contains(user));
		}
		Assert.assertEquals(numberOfXmppClients, aliveIDs.size());
	}

	@Test
	public void testIAmLiveXmppResponse() throws XMPPException {
		IQ iq = rendezvousTestHelper.createIAmAliveIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
	}

	@After
	public void tearDown() throws ComponentException {
		rendezvousTestHelper.disconnectXMPPClients();
		rendezvousTestHelper.disconnectRendezvousXMPPComponent();
	}
}
