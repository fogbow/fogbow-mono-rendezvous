package org.fogbowcloud.rendezvous.xmpp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.RendezvousItem;
import org.fogbowcloud.rendezvous.xmpp.model.RendezvousUseful;
import org.fogbowcloud.rendezvous.xmpp.model.WhoIsAliveResponseItem;
import org.jivesoftware.smack.XMPPException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmpp.component.ComponentException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;

public class TestWhoIsAlive{
	
	private RendezvousUseful rendezvousUseful;
	
    @Before
    public void setUp() {
    	
    	rendezvousUseful = new RendezvousUseful();

    	rendezvousUseful.initializeXMPPRendezvousComponent(RendezvousUseful.TEST_DEFAULT_TIMEOUT);

    	rendezvousUseful.initializeXMPPClient();
    }
    
    @Test
    public void testWhoisAliveEmpty() {
    	IQ response;
        IQ iq = rendezvousUseful.createWhoIsAliveIQ();
        try {
            response = (IQ) rendezvousUseful.getXmppClient().syncSend(iq);
            ArrayList<String> aliveIDs = rendezvousUseful.getAliveIdsFromIQ(response);
            Assert.assertEquals(0, aliveIDs.size());

        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWhoIsAliveAfterTimeout() throws InterruptedException {
    	IQ response;
        IQ iq = rendezvousUseful.createIAmAliveIQ();

        try {
            response = (IQ) rendezvousUseful.getXmppClient().syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());

            iq = rendezvousUseful.createWhoIsAliveIQ();

            response = (IQ) rendezvousUseful.getXmppClient().syncSend(iq);
            ArrayList<String> aliveIDs = rendezvousUseful.getAliveIdsFromIQ(response);

            Assert.assertEquals(1, aliveIDs.size());
            Assert.assertTrue(aliveIDs.contains(RendezvousUseful.CLIENT));

            // sleeping
            Thread.sleep(RendezvousUseful.TEST_DEFAULT_TIMEOUT + RendezvousUseful.TIMEOUT_GRACE);

            iq = rendezvousUseful.createWhoIsAliveIQ();

            response = (IQ) rendezvousUseful.getXmppClient().syncSend(iq);
            aliveIDs = rendezvousUseful.getAliveIdsFromIQ(response);

            Assert.assertEquals(0, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }    
    
    @Test
    public void testWhoIsAliveReturnedItemValue() throws InterruptedException,
            ParseException {
    	IQ response;
        //creating IAmAlive IQ
        IQ iq = new IQ(Type.get);
        iq.setTo(RendezvousUseful.RENDEZVOUS_COMPONENT_URL);
        Element statusEl = iq.getElement()
                .addElement("query", RendezvousUseful.IAMALIVE_NAMESPACE).addElement("status");
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
            response = (IQ) rendezvousUseful.getXmppClient().syncSend(iq);
            Date afterMessage = new Date(System.currentTimeMillis());

            Assert.assertEquals(Type.result, response.getType());

            iq = rendezvousUseful.createWhoIsAliveIQ();

            response = (IQ) rendezvousUseful.getXmppClient().syncSend(iq);
            ArrayList<WhoIsAliveResponseItem> responseItems = rendezvousUseful.getItemsFromIQ(response);

            //checking values from whoIsAlive
            WhoIsAliveResponseItem item = responseItems.get(0);
            Assert.assertEquals(cpuIdleValue, item.getResources().getCpuIdle());
            Assert.assertEquals(cpuInUseValue, item.getResources()
                    .getCpuInUse());
            Assert.assertEquals(memIdleValue, item.getResources().getMemIdle());
            Assert.assertEquals(memInUseValue, item.getResources()
                    .getMemInUse());

            ArrayList<String> aliveIDs = rendezvousUseful.getAliveIdsFromIQ(response);

            SimpleDateFormat format = new SimpleDateFormat(
                   RendezvousItem.ISO_8601_DATE_FORMAT, Locale.ROOT);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));

            Date updated = new Date(format.parse(item.getUpdated()).getTime());

            Assert.assertTrue(updated.after(beforeMessage));
            Assert.assertTrue(updated.before(afterMessage));
            Assert.assertTrue(aliveIDs.contains(RendezvousUseful.CLIENT));
            Assert.assertEquals(1, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }    
    
    @After
    public void tearDown() {
        try {           
            rendezvousUseful.deleteAccount();         
        } catch (XMPPException e1) {
            e1.printStackTrace();
        }
        rendezvousUseful.disconnectXMPPClient();
        try{
            rendezvousUseful.disconnectRendezvousXMPPComponent();
        } catch (ComponentException e) {
        	e.printStackTrace();
        }
    }        
}
