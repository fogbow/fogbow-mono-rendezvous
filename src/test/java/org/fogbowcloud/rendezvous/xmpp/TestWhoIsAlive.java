package org.fogbowcloud.rendezvous.xmpp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.RendezvousItem;
import org.fogbowcloud.rendezvous.core.model.Flavor;
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
	public void setUp() throws ComponentException {
		rendezvousTestHelper = new RendezvousTestHelper();
		rendezvousTestHelper
				.initializeXMPPRendezvousComponent(RendezvousTestHelper.TEST_DEFAULT_TIMEOUT);
	}

	@Test
	public void testWhoisAliveEmpty() throws XMPPException {
		IQ iq = RendezvousTestHelper.createWhoIsAliveIQ();
		XMPPClient xmppClient = rendezvousTestHelper.createXMPPClient();
		IQ response = (IQ) xmppClient.syncSend(iq);
		ArrayList<String> aliveIDs = RendezvousTestHelper.getAliveIds(response);
		Assert.assertEquals(0, aliveIDs.size());
	}

	@Test
	public void testWhoIsAliveAfterTimeout() throws InterruptedException,
			XMPPException {
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

		Thread.sleep(RendezvousTestHelper.TEST_DEFAULT_TIMEOUT
				+ RendezvousTestHelper.TIMEOUT_GRACE);
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
		Element statusEl = iq.getElement()
				.addElement("query", RendezvousTestHelper.IAMALIVE_NAMESPACE)
				.addElement("status");
		iq.getElement().element("query").addElement("cert").setText("cert");
		String cpuIdleValue = "value1";
		String cpuInUseValue = "value2";
		String memIdleValue = "value3";
		String memInUseValue = "value4";
		Flavor flavor = new Flavor("small", "cpu", "mem", 2);
		
		statusEl.addElement("cpu-idle").setText(cpuIdleValue);
		statusEl.addElement("cpu-inuse").setText(cpuInUseValue);
		statusEl.addElement("mem-idle").setText(memIdleValue);
		statusEl.addElement("mem-inuse").setText(memInUseValue);

		Element flavorElement = statusEl.addElement("flavor");
		flavorElement.addElement("name").setText(flavor.getName());
		flavorElement.addElement("cpu").setText(flavor.getCpu());
		flavorElement.addElement("mem").setText(flavor.getMem());
		flavorElement.addElement("capacity").setText(
				flavor.getCapacity().toString());

		Date beforeMessage = new Date(System.currentTimeMillis());
		response = (IQ) xmppClient.syncSend(iq);
		Date afterMessage = new Date(System.currentTimeMillis());
		Assert.assertEquals(Type.result, response.getType());

		iq = RendezvousTestHelper.createWhoIsAliveIQ();
		response = (IQ) xmppClient.syncSend(iq);
		ArrayList<WhoIsAliveResponseItem> responseItems = RendezvousTestHelper
				.getItemsFromIQ(response);
		WhoIsAliveResponseItem item = responseItems.get(0);
		Assert.assertEquals(cpuIdleValue, item.getResources().getCpuIdle());
		Assert.assertEquals(cpuInUseValue, item.getResources().getCpuInUse());
		Assert.assertEquals(memIdleValue, item.getResources().getMemIdle());
		Assert.assertEquals(memInUseValue, item.getResources().getMemInUse());
		Assert.assertEquals(flavor.getName(), item.getResources().getFlavours()
				.get(0).getName());
		Assert.assertEquals(flavor.getCpu(), item.getResources().getFlavours()
				.get(0).getCpu());
		Assert.assertEquals(flavor.getMem(), item.getResources().getFlavours()
				.get(0).getMem());
		Assert.assertEquals(flavor.getCapacity(), item.getResources().getFlavours()
				.get(0).getCapacity());
		
		ArrayList<String> aliveIDs = RendezvousTestHelper.getAliveIds(response);
		SimpleDateFormat format = new SimpleDateFormat(
				RendezvousItem.ISO_8601_DATE_FORMAT, Locale.ROOT);
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date updated = new Date(format.parse(item.getUpdated()).getTime());
		Assert.assertTrue(updated.after(beforeMessage));
		Assert.assertTrue(updated.before(afterMessage));
		Assert.assertTrue(aliveIDs.contains(RendezvousTestHelper
				.getClientJid(0)));
		Assert.assertEquals(1, aliveIDs.size());
	}

	@After
	public void tearDown() throws ComponentException {
		rendezvousTestHelper.disconnectXMPPClients();
		rendezvousTestHelper.disconnectRendezvousXMPPComponent();
	}
}
