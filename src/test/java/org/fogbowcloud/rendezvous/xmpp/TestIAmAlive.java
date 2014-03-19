package org.fogbowcloud.rendezvous.xmpp;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.fogbowcloud.rendezvous.xmpp.model.RendezvousTestHelper;
import org.jamppa.client.XMPPClient;
import org.jamppa.client.plugin.xep0077.XEP0077;
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
	public void setUp() {
		rendezvousTestHelper = new RendezvousTestHelper();

		rendezvousTestHelper
				.initializeXMPPRendezvousComponent(RendezvousTestHelper.TEST_DEFAULT_TIMEOUT);
	}

	@Test(expected = XMPPException.class)
	public void testInvalidIQ() throws XMPPException {
		IQ response;

		String invalidNamespace = "invalidnamespace";
		IQ iq = new IQ(Type.get);
		iq.setTo(RendezvousTestHelper.RENDEZVOUS_COMPONENT_URL);
		iq.getElement().addElement("query", invalidNamespace);

		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();

		response = (IQ) xmppClient.syncSend(iq);
	}

	@Test
	public void testSyncImAliveSingleElement() {
		IQ response;
		IQ iq = rendezvousTestHelper.createIAmAliveIQ();

		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();

		try {
			response = (IQ) xmppClient.syncSend(iq);
			Assert.assertEquals(Type.result, response.getType());

			iq = rendezvousTestHelper.createWhoIsAliveIQ();

			response = (IQ) xmppClient.syncSend(iq);
			ArrayList<String> aliveIDs = rendezvousTestHelper
					.getAliveIdsFromIQ(response);

			Assert.assertTrue(aliveIDs.contains(rendezvousTestHelper
					.returnNameXMPPClientOnList(RendezvousTestHelper.SEARCH_FIRST_XMPPCLIENT_CREATED)));
			Assert.assertEquals(1, aliveIDs.size());
		} catch (XMPPException e) {
		}
	}

	@Test
	public void testImAsyncAliveSingleElement() {
		IQ iq = rendezvousTestHelper.createIAmAliveIQ();

		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();

		PacketFilter filter = new AndFilter(
				new PacketTypeFilter(IQ.class),
				new ToContainsFilter(
						rendezvousTestHelper
								.returnNameXMPPClientOnList(RendezvousTestHelper.SEARCH_FIRST_XMPPCLIENT_CREATED)));

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
	public void testIamAlive2EqualElements() {
		IQ response;
		IQ iq = rendezvousTestHelper.createIAmAliveIQ();

		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();

		try {
			response = (IQ) xmppClient.syncSend(iq);
			Assert.assertEquals(Type.result, response.getType());

			iq = rendezvousTestHelper.createIAmAliveIQ();

			response = (IQ) xmppClient.syncSend(iq);
			Assert.assertEquals(Type.result, response.getType());

			iq = rendezvousTestHelper.createWhoIsAliveIQ();

			response = (IQ) xmppClient.syncSend(iq);

			ArrayList<String> aliveIDs = rendezvousTestHelper
					.getAliveIdsFromIQ(response);

			Assert.assertEquals(1, aliveIDs.size());
			Assert.assertTrue(aliveIDs.contains(rendezvousTestHelper
					.returnNameXMPPClientOnList(RendezvousTestHelper.SEARCH_FIRST_XMPPCLIENT_CREATED)));
		} catch (XMPPException e) {
		}
	}

	@Test
	public void testIamAlive2Clients() {
		// set up client 2
		IQ response;

		XMPPClient xmppClient1 = rendezvousTestHelper.createXMPPClient();
		XMPPClient xmppClient2 = rendezvousTestHelper.createXMPPClient();

		try {

			IQ iq = rendezvousTestHelper.createIAmAliveIQ();

			// send am alive! from client 1
			response = (IQ) xmppClient1.syncSend(iq);
			Assert.assertEquals(Type.result, response.getType());

			// send am alive! from client 2
			response = (IQ) xmppClient2.syncSend(iq);
			Assert.assertEquals(Type.result, response.getType());

			// send Who is Alive?
			iq = rendezvousTestHelper.createWhoIsAliveIQ();
			response = (IQ) xmppClient1.syncSend(iq);
			ArrayList<String> aliveIDs = rendezvousTestHelper
					.getAliveIdsFromIQ(response);

			// asserts
			Assert.assertEquals(2, aliveIDs.size());
			Assert.assertTrue(aliveIDs.contains(rendezvousTestHelper
					.returnNameXMPPClientOnList(RendezvousTestHelper.SEARCH_FIRST_XMPPCLIENT_CREATED)));
			Assert.assertTrue(aliveIDs.contains(rendezvousTestHelper
					.returnNameXMPPClientOnList(RendezvousTestHelper.SEARCH_SECOND_XMPPCLIENT_CREATED)));

		} catch (XMPPException e) {
		} finally {
			xmppClient2.disconnect();
		}
	}

	@Test
	public void testIamAliveManyClients() {
		IQ response;

		// stopping components and client
		tearDown();

		// Initializing XMPP component with big Timeout
		rendezvousTestHelper
				.initializeXMPPRendezvousComponent(10000 * RendezvousTestHelper.TEST_DEFAULT_TIMEOUT);

		// creating clients
		int begin = 0;
		int numberOfXmppClients = 1000;

		XMPPClient xmppClient;
		for (int i = begin; i < begin + numberOfXmppClients; i++) {

			xmppClient = rendezvousTestHelper.createXMPPClient();

			IQ iq = rendezvousTestHelper.createIAmAliveIQ();

			String nameClient = rendezvousTestHelper
					.returnNameXMPPClientOnList(i);

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

		try {
			IQ iq = rendezvousTestHelper.createWhoIsAliveIQ();
			response = (IQ) xmppClient2.syncSend(iq);
			ArrayList<String> aliveIDs = rendezvousTestHelper
					.getAliveIdsFromIQ(response);

			for (int i = begin; i < begin + numberOfXmppClients; i++) {
				String user = rendezvousTestHelper
						.returnNameXMPPClientOnList(i);
				Assert.assertTrue(aliveIDs.contains(user));
			}

			Assert.assertEquals(numberOfXmppClients, aliveIDs.size());
		} catch (XMPPException e) {
		}
	}

	@Test
	public void testIamAliveManyClientsWithSemaphore() throws Exception {
		IQ response;

		// stopping components and client
		tearDown();

		// Initializing XMPP component with big Timeout
		rendezvousTestHelper
				.initializeXMPPRendezvousComponent(10000 * RendezvousTestHelper.TEST_DEFAULT_TIMEOUT);

		// creating clients
		int numberOfXmppClients = 1000;

		final Semaphore semaphore = new Semaphore(0);
		final long TIMEOUT_ALL_RESPONSE = 6000;

		XMPPClient xmppClient;
		for (int i = 0; i < numberOfXmppClients; i++) {			

			xmppClient = rendezvousTestHelper.createXMPPClient();

			IQ iq = rendezvousTestHelper.createIAmAliveIQ();

			String nameClient = rendezvousTestHelper
					.returnNameXMPPClientOnList(i);

			PacketFilter filter = new AndFilter(new PacketTypeFilter(IQ.class),
					new ToContainsFilter(nameClient));

			PacketListener callback = new PacketListener() {
				public void processPacket(Packet packet) {
					IQ response = (IQ) packet;
					if (response
							.getFrom()
							.toString()
							.equals(rendezvousTestHelper.RENDEZVOUS_COMPONENT_URL)) {
						semaphore.release();
					}
					Assert.assertEquals(Type.result, response.getType());
				}
			};

			xmppClient.on(filter, callback);
			xmppClient.send(iq);

		}

		boolean receivedAllResults = false;

		try {
			receivedAllResults = semaphore.tryAcquire(numberOfXmppClients,
					TIMEOUT_ALL_RESPONSE, TimeUnit.MILLISECONDS);
			Assert.assertTrue(receivedAllResults);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		XMPPClient xmppClient2 = rendezvousTestHelper.createXMPPClient();

		try {
			IQ iq = rendezvousTestHelper.createWhoIsAliveIQ();
			response = (IQ) xmppClient2.syncSend(iq);
			ArrayList<String> aliveIDs = rendezvousTestHelper
					.getAliveIdsFromIQ(response);

			for (int i = 0; i < numberOfXmppClients; i++) {
				String user = rendezvousTestHelper
						.returnNameXMPPClientOnList(i);
				Assert.assertTrue(aliveIDs.contains(user));
			}

			Assert.assertEquals(numberOfXmppClients, aliveIDs.size());
		} catch (XMPPException e) {
		}

	}

	@Test
	public void testIAmLiveXmppResponse() {
		IQ response;
		IQ iq = rendezvousTestHelper.createIAmAliveIQ();

		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();

		try {
			response = (IQ) xmppClient.syncSend(iq);
			Assert.assertEquals(Type.result, response.getType());
		} catch (XMPPException e) {
		}
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
