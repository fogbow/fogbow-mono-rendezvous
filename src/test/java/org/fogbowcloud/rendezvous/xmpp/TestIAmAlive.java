package org.fogbowcloud.rendezvous.xmpp;

import java.util.ArrayList;

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
		xmppClient.syncSend(iq);
	}

	@Test
	public void testSyncImAliveSingleElement() throws XMPPException {
		IQ response;
		IQ iq = RendezvousTestHelper.createIAmAliveIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();

		response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());

		iq = RendezvousTestHelper.createWhoIsAliveIQ();
		response = (IQ) xmppClient.syncSend(iq);
		ArrayList<String> aliveIDs = RendezvousTestHelper.getAliveIds(response);

		Assert.assertTrue(aliveIDs.contains(RendezvousTestHelper
				.getClientJid(0)));
		Assert.assertEquals(1, aliveIDs.size());
	}

	@Test
	public void testImAsyncAliveSingleElement() throws XMPPException {
		IQ iq = RendezvousTestHelper.createIAmAliveIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();

		PacketFilter filter = new AndFilter(
				new PacketTypeFilter(IQ.class),
				new ToContainsFilter(
						RendezvousTestHelper
								.getClientJid(0)));
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
		IQ iq = RendezvousTestHelper.createIAmAliveIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();

		response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
		iq = RendezvousTestHelper.createIAmAliveIQ();
		response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());

		iq = RendezvousTestHelper.createWhoIsAliveIQ();
		response = (IQ) xmppClient.syncSend(iq);
		ArrayList<String> aliveIDs = RendezvousTestHelper.getAliveIds(response);
		Assert.assertEquals(1, aliveIDs.size());
		Assert.assertTrue(aliveIDs.contains(RendezvousTestHelper
				.getClientJid(0)));
	}

	@Test
	public void testIamAlive2Clients() throws XMPPException {
		XMPPClient xmppClient1 = rendezvousTestHelper.createXMPPClient();
		XMPPClient xmppClient2 = rendezvousTestHelper.createXMPPClient();
		IQ iq = RendezvousTestHelper.createIAmAliveIQ();

		IQ response = (IQ) xmppClient1.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
		response = (IQ) xmppClient2.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());

		iq = RendezvousTestHelper.createWhoIsAliveIQ();
		response = (IQ) xmppClient1.syncSend(iq);
		ArrayList<String> aliveIDs = RendezvousTestHelper.getAliveIds(response);
		Assert.assertEquals(2, aliveIDs.size());
		Assert.assertTrue(aliveIDs.contains(RendezvousTestHelper
				.getClientJid(0)));
		Assert.assertTrue(aliveIDs.contains(RendezvousTestHelper
				.getClientJid(1)));
	}

	@Test
	public void testIAmAliveXmppResponse() throws XMPPException {
		IQ iq = RendezvousTestHelper.createIAmAliveIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());
	}

	@After
	public void tearDown() {
		rendezvousTestHelper.disconnectXMPPClients();
		try {
			rendezvousTestHelper.disconnectRendezvousXMPPComponent();
		} catch (ComponentException e) {}
	}
}
