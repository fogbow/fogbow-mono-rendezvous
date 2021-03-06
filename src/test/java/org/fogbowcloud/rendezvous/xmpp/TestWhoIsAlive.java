package org.fogbowcloud.rendezvous.xmpp;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.RendezvousImpl;
import org.fogbowcloud.rendezvous.core.RendezvousItem;
import org.fogbowcloud.rendezvous.core.RendezvousTestHelper;
import org.fogbowcloud.rendezvous.core.model.DateUtils;
import org.jamppa.client.XMPPClient;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
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

public class TestWhoIsAlive {

	private static final int XMPP_CLIENT_COUNT = 200;
	private RendezvousTestHelper rendezvousTestHelper;
	private long SEMAPHORE_TIMEOUT = 100;

	@Before
	public void setUp() throws Exception {
		ScheduledExecutorService executor = Mockito
				.mock(ScheduledExecutorService.class);
		rendezvousTestHelper = new RendezvousTestHelper();
		rendezvousTestHelper.initializeXMPPRendezvousComponent(
				RendezvousTestHelper.TEST_DEFAULT_TIMEOUT, new String[] {},
				executor);
	}

	@Test
	public void testWhoisAliveEmpty() throws XMPPException, ParseException {
		IQ iq = RendezvousTestHelper.createWhoIsAliveIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response = (IQ) xmppClient.syncSend(iq);
		ArrayList<String> aliveIDs = RendezvousTestHelper.getAliveIds(response);
		Assert.assertEquals(0, aliveIDs.size());
	}

	@Test
	public void testWhoisAliveNoRsmSet() throws XMPPException, ParseException,
			InterruptedException {

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

		IQ iq = RendezvousTestHelper.createWhoIsAliveIQNoRsm();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response = (IQ) xmppClient.syncSend(iq);
		ArrayList<String> aliveIDs = RendezvousTestHelper.getAliveIds(response);
		Assert.assertEquals(
				RendezvousTestHelper.getMaxWhoIsAliveManagerCount(),
				aliveIDs.size());

	}

	@Test
	public void testWhoIsAliveAfterTimeout() throws InterruptedException,
			XMPPException, ParseException {
		IQ iq = RendezvousTestHelper.createIAmAliveIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());

		iq = RendezvousTestHelper.createWhoIsAliveIQ();
		response = (IQ) xmppClient.syncSend(iq);
		ArrayList<String> aliveIDs = RendezvousTestHelper.getAliveIds(response);
		Assert.assertEquals(1, aliveIDs.size());
		Assert.assertTrue(aliveIDs.contains(RendezvousTestHelper
				.getClientJid(0)));

		RendezvousImpl rendezvous = ((RendezvousImpl) rendezvousTestHelper
				.getRendezvousXmppComponent().getRendezvous());
		
		DateUtils dateMock = Mockito.mock(DateUtils.class);
		Mockito.when(dateMock.currentTimeMillis()).thenReturn(
				System.currentTimeMillis() + RendezvousTestHelper.TEST_DEFAULT_TIMEOUT
						* RendezvousTestHelper.DEFAULT_WAIT_FREQUENCY_TIMES);
		rendezvous.setDateUnit(dateMock);		
		
		rendezvous.checkExpiredAliveIDs();
		iq = RendezvousTestHelper.createWhoIsAliveIQ();
		response = (IQ) xmppClient.syncSend(iq);
		aliveIDs = RendezvousTestHelper.getAliveIds(response);
		Assert.assertEquals(0, aliveIDs.size());
	}

	@Test
	public void testWhoIsAliveReturnedItemValue() throws InterruptedException,
			ParseException, XMPPException {
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response;
		IQ iq = new IQ(Type.get);
		iq.setTo(RendezvousTestHelper.RENDEZVOUS_COMPONENT_URL);
		@SuppressWarnings("unused")
		Element statusEl = iq.getElement()
				.addElement("query", RendezvousTestHelper.IAMALIVE_NAMESPACE)
				.addElement("status");
		iq.getElement().element("query").addElement("cert").setText("cert");	

		response = (IQ) xmppClient.syncSend(iq);
		Assert.assertEquals(Type.result, response.getType());

		iq = RendezvousTestHelper.createWhoIsAliveIQ();
		response = (IQ) xmppClient.syncSend(iq);
		LinkedList<RendezvousItem> responseItems = RendezvousTestHelper
				.getItemsFromIQ(response);
		RendezvousItem item = responseItems.get(0);

		ArrayList<String> aliveIDs = RendezvousTestHelper.getAliveIds(response);
		Assert.assertTrue(aliveIDs.contains(RendezvousTestHelper
				.getClientJid(0)));
		Assert.assertEquals(1, aliveIDs.size());
	}

	@Test
	public void testWhoisAlivePagination() throws InterruptedException,
			XMPPException, ParseException {

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
		IQ response = (IQ) xmppClient.syncSend(RendezvousTestHelper
				.createWhoIsAliveIQ());

		ArrayList<String> aliveIDs = RendezvousTestHelper.getAliveIds(response);
		Assert.assertEquals(
				RendezvousTestHelper.getMaxWhoIsAliveManagerCount(),
				aliveIDs.size());
		Assert.assertEquals(RendezvousTestHelper.getSetElementFromWhoIsAlive(
				response, "first"), RendezvousTestHelper.getClientJid(0));
		Assert.assertEquals(RendezvousTestHelper.getSetElementFromWhoIsAlive(
				response, "last"), RendezvousTestHelper
				.getClientJid(RendezvousTestHelper
						.getMaxWhoIsAliveManagerCount() - 1));
		Assert.assertEquals(RendezvousTestHelper.getMaxWhoIsAliveManagerCount()
				+ "", RendezvousTestHelper.getSetElementFromWhoIsAlive(
				response, "count"));
	}

	@Test
	public void testWhoisAliveRequest2ndPage() throws InterruptedException,
			XMPPException, ParseException {
		// do 200 i am live
		// request first page
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
		IQ response = (IQ) xmppClient.syncSend(RendezvousTestHelper
				.createWhoIsAliveIQ());
		ArrayList<String> aliveIDs0 = RendezvousTestHelper
				.getAliveIds(response);
		String last = RendezvousTestHelper.getSetElementFromWhoIsAlive(
				response, "last");
		response = (IQ) xmppClient.syncSend(RendezvousTestHelper
				.createWhoIsAliveIQ(last));
		ArrayList<String> aliveIDs = RendezvousTestHelper.getAliveIds(response);
		Assert.assertEquals(
				XMPP_CLIENT_COUNT
						- RendezvousTestHelper.getMaxWhoIsAliveManagerCount(),
				aliveIDs.size());
		Assert.assertNotEquals(aliveIDs, aliveIDs0);
		Assert.assertEquals(
				RendezvousTestHelper.getMaxWhoIsAliveManagerCount(),
				aliveIDs.size());
		Assert.assertEquals(RendezvousTestHelper.getSetElementFromWhoIsAlive(
				response, "first"), RendezvousTestHelper
				.getClientJid(RendezvousTestHelper
						.getMaxWhoIsAliveManagerCount()));
		Assert.assertEquals(RendezvousTestHelper.getSetElementFromWhoIsAlive(
				response, "last"), RendezvousTestHelper
				.getClientJid((2 * RendezvousTestHelper
						.getMaxWhoIsAliveManagerCount()) - 1));
		Assert.assertEquals(RendezvousTestHelper.getMaxWhoIsAliveManagerCount()
				+ "", RendezvousTestHelper.getSetElementFromWhoIsAlive(
				response, "count"));
	}

	@Test
	public void testWhoisAliveRSMReturnEmptyPage() throws InterruptedException,
			XMPPException, ParseException {
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
		IQ response = (IQ) xmppClient.syncSend(RendezvousTestHelper
				.createWhoIsAliveIQ());
		ArrayList<String> aliveIDs = RendezvousTestHelper.getAliveIds(response);
		String last = RendezvousTestHelper.getSetElementFromWhoIsAlive(
				response, "last");
		response = (IQ) xmppClient.syncSend(RendezvousTestHelper
				.createWhoIsAliveIQ(last));
		aliveIDs = RendezvousTestHelper.getAliveIds(response);

		last = RendezvousTestHelper.getSetElementFromWhoIsAlive(response,
				"last");
		response = (IQ) xmppClient.syncSend(RendezvousTestHelper
				.createWhoIsAliveIQ(last));
		aliveIDs = RendezvousTestHelper.getAliveIds(response);
		Assert.assertEquals(0, aliveIDs.size());
	}

	@Test
	public void testWhoisAliveRSMReturnItemCount0()
			throws InterruptedException, XMPPException, ParseException {
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
		IQ response = (IQ) xmppClient.syncSend(RendezvousTestHelper
				.createWhoIsAliveIQ(null, 0));
		RendezvousTestHelper.getAliveIds(response);
		String count = RendezvousTestHelper.getSetElementFromWhoIsAlive(
				response, "count");
		Assert.assertEquals(0, Integer.parseInt(count));
	}

	@Test
	public void testWhoisAliveRSMAfterNotFound() throws InterruptedException,
			XMPPException, ParseException {
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
		try {
			@SuppressWarnings("unused")
			IQ response = (IQ) xmppClient.syncSend(RendezvousTestHelper
					.createWhoIsAliveIQ("NotFoundString"));
			Assert.fail();
		} catch (XMPPException e) {
			Assert.assertTrue(PacketError.Type.cancel.equals(e.getXMPPError()
					.getType()));
		}
	}

	@After
	public void tearDown() throws ComponentException {
		rendezvousTestHelper.disconnectXMPPClients();
		rendezvousTestHelper.disconnectRendezvousXMPPComponent();
	}
}
