package org.fogbowcloud.rendezvous.xmpp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.RendezvousItem;
import org.fogbowcloud.rendezvous.xmpp.model.RendezvousTestHelper;
import org.fogbowcloud.rendezvous.xmpp.model.WhoIsAliveResponseItem;
import org.jamppa.client.XMPPClient;
import org.jivesoftware.smack.XMPPException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmpp.component.ComponentException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;

public class TestWhoIsAlive {

	private RendezvousTestHelper rendezvousTestHelper;

	@Before
	public void setUp() {

		rendezvousTestHelper = new RendezvousTestHelper();

		rendezvousTestHelper
				.initializeXMPPRendezvousComponent(RendezvousTestHelper.TEST_DEFAULT_TIMEOUT);
	}

	@Test
	public void testWhoisAliveEmpty() {
		IQ response;
		IQ iq = rendezvousTestHelper.createWhoIsAliveIQ();

		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();

		try {
			response = (IQ) xmppClient.syncSend(iq);
			ArrayList<String> aliveIDs = rendezvousTestHelper
					.getAliveIdsFromIQ(response);
			Assert.assertEquals(0, aliveIDs.size());
		} catch (XMPPException e) {
		}
	}

	@Test
	public void testWhoIsAliveAfterTimeout() throws InterruptedException {
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

			Assert.assertEquals(1, aliveIDs.size());
			Assert.assertTrue(aliveIDs.contains(rendezvousTestHelper
					.returnNameXMPPClientOnList(RendezvousTestHelper.SEARCH_FIRST_XMPPCLIENT_CREATED)));

			// sleeping
			Thread.sleep(RendezvousTestHelper.TEST_DEFAULT_TIMEOUT
					+ RendezvousTestHelper.TIMEOUT_GRACE);

			iq = rendezvousTestHelper.createWhoIsAliveIQ();

			response = (IQ) xmppClient.syncSend(iq);
			aliveIDs = rendezvousTestHelper.getAliveIdsFromIQ(response);

			Assert.assertEquals(0, aliveIDs.size());
		} catch (XMPPException e) {
		}
	}

	@Test
	public void testWhoIsAliveReturnedItemValue() throws InterruptedException,
			ParseException {
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();

		IQ response;
		// creating IAmAlive IQ
		IQ iq = new IQ(Type.get);
		iq.setTo(RendezvousTestHelper.RENDEZVOUS_COMPONENT_URL);
		Element statusEl = iq.getElement()
				.addElement("query", RendezvousTestHelper.IAMALIVE_NAMESPACE)
				.addElement("status");
		String cpuIdleValue = "valor1";
		String cpuInUseValue = "valor2";
		String memIdleValue = "valor3";
		String memInUseValue = "valor4";

		statusEl.addElement("cpu-idle").setText(cpuIdleValue);
		statusEl.addElement("cpu-inuse").setText(cpuInUseValue);
		statusEl.addElement("mem-idle").setText(memIdleValue);
		statusEl.addElement("mem-inuse").setText(memInUseValue);

		try {

			Date beforeMessage = new Date(System.currentTimeMillis());
			response = (IQ) xmppClient.syncSend(iq);
			Date afterMessage = new Date(System.currentTimeMillis());

			Assert.assertEquals(Type.result, response.getType());

			iq = rendezvousTestHelper.createWhoIsAliveIQ();

			response = (IQ) xmppClient.syncSend(iq);
			ArrayList<WhoIsAliveResponseItem> responseItems = rendezvousTestHelper
					.getItemsFromIQ(response);

			// checking values from whoIsAlive
			WhoIsAliveResponseItem item = responseItems.get(0);
			Assert.assertEquals(cpuIdleValue, item.getResources().getCpuIdle());
			Assert.assertEquals(cpuInUseValue, item.getResources()
					.getCpuInUse());
			Assert.assertEquals(memIdleValue, item.getResources().getMemIdle());
			Assert.assertEquals(memInUseValue, item.getResources()
					.getMemInUse());

			ArrayList<String> aliveIDs = rendezvousTestHelper
					.getAliveIdsFromIQ(response);

			SimpleDateFormat format = new SimpleDateFormat(
					RendezvousItem.ISO_8601_DATE_FORMAT, Locale.ROOT);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));

			Date updated = new Date(format.parse(item.getUpdated()).getTime());

			Assert.assertTrue(updated.after(beforeMessage));
			Assert.assertTrue(updated.before(afterMessage));
			Assert.assertTrue(aliveIDs.contains(rendezvousTestHelper
					.returnNameXMPPClientOnList(RendezvousTestHelper.SEARCH_FIRST_XMPPCLIENT_CREATED)));
			Assert.assertEquals(1, aliveIDs.size());
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
