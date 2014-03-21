package org.fogbowcloud.rendezvous.xmpp;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.fogbowcloud.rendezvous.xmpp.model.RendezvousTestHelper;
import org.jamppa.client.XMPPClient;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmpp.component.ComponentException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

public class TestIAmAliveManyClients {

	private static final long SEMAPHORE_TIMEOUT = 5; //In minutes
	private static final int XMPP_CLIENT_COUNT = 100;
	
	private RendezvousTestHelper rendezvousTestHelper;
	
	@Before
	public void setUp() throws ComponentException {
		rendezvousTestHelper = new RendezvousTestHelper();
		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				10000 * RendezvousTestHelper.TEST_DEFAULT_TIMEOUT);
	}

	@Test
	public void testIamAliveManyClientsWithSemaphore()
			throws InterruptedException, XMPPException, ComponentException {
		final Semaphore semaphore = new Semaphore(0);
		
		final PacketListener callback = new PacketListener() {
			public void processPacket(Packet packet) {
				semaphore.release();
			}
		};

		for (int i = 0; i < XMPP_CLIENT_COUNT; i++) {
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
		
		boolean receivedAll = semaphore.tryAcquire(XMPP_CLIENT_COUNT, 
				SEMAPHORE_TIMEOUT, TimeUnit.MINUTES);
		Assert.assertTrue(receivedAll);
		
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response = (IQ) xmppClient.syncSend(RendezvousTestHelper.createWhoIsAliveIQ());
		
		ArrayList<String> aliveIDs = RendezvousTestHelper.getAliveIds(response);
		for (int i = 0; i < XMPP_CLIENT_COUNT; i++) {
			Assert.assertTrue(aliveIDs.contains(
					RendezvousTestHelper.getClientJid(i)));
		}
		Assert.assertEquals(XMPP_CLIENT_COUNT, aliveIDs.size());
	}
	
	@After
	public void tearDown() {
		rendezvousTestHelper.disconnectXMPPClients();
		try {
			rendezvousTestHelper.disconnectRendezvousXMPPComponent();
		} catch (ComponentException e) {}
	}
}
