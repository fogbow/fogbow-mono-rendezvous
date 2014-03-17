package org.fogbowcloud.rendezvous.xmpp;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.fogbowcloud.rendezvous.cloud.RendezvousItem;
import org.fogbowcloud.rendezvous.cloud.ResourcesInfo;
import org.fogbowcloud.rendezvous.xmpp.model.TestRendezvousXMPPComponent;
import org.fogbowcloud.rendezvous.xmpp.model.WhoIsAliveResponseItem;
import org.jamppa.client.XMPPClient;
import org.jamppa.client.plugin.xep0077.XEP0077;
import org.jivesoftware.smack.XMPPException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmpp.component.ComponentException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;

public class TestRendezvousXMPPComponentWhoIsAlive extends TestRendezvousXMPPComponent{
	
    @Before
    public void setUp() {

        initializeXMPPRendezvousComponent(TEST_DEFAULT_TIMEOUT);

        initializeXMPPClient();
    }
    
    @Test
    public void testWhoisAliveEmpty() {

        IQ iq = createWhoIsAliveIQ();
        try {
            response = (IQ) xmppClient.syncSend(iq);
            ArrayList<String> aliveIDs = getAliveIdsFromIQ(response);
            Assert.assertEquals(0, aliveIDs.size());

        } catch (XMPPException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testWhoIsAliveAfterTimeout() throws InterruptedException {

        IQ iq = createIAmAliveIQ();

        try {
            response = (IQ) xmppClient.syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());

            iq = createWhoIsAliveIQ();

            response = (IQ) xmppClient.syncSend(iq);
            ArrayList<String> aliveIDs = getAliveIdsFromIQ(response);

            Assert.assertEquals(1, aliveIDs.size());
            Assert.assertTrue(aliveIDs.contains(CLIENT));

            // sleeping
            Thread.sleep(TEST_DEFAULT_TIMEOUT + TIMEOUT_GRACE);

            iq = createWhoIsAliveIQ();

            response = (IQ) xmppClient.syncSend(iq);
            aliveIDs = getAliveIdsFromIQ(response);

            Assert.assertEquals(0, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }    
    
    @Test
    public void testWhoIsAliveReturnedItemValue() throws InterruptedException,
            ParseException {
        
        //creating IAmAlive IQ
        IQ iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        Element statusEl = iq.getElement()
                .addElement("query", IAMALIVE_NAMESPACE).addElement("status");
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

            iq = createWhoIsAliveIQ();

            response = (IQ) xmppClient.syncSend(iq);
            ArrayList<WhoIsAliveResponseItem> responseItems = getItemsFromIQ(response);

            //checking values from whoIsAlive
            WhoIsAliveResponseItem item = responseItems.get(0);
            Assert.assertEquals(cpuIdleValue, item.getResources().getCpuIdle());
            Assert.assertEquals(cpuInUseValue, item.getResources()
                    .getCpuInUse());
            Assert.assertEquals(memIdleValue, item.getResources().getMemIdle());
            Assert.assertEquals(memInUseValue, item.getResources()
                    .getMemInUse());

            ArrayList<String> aliveIDs = getAliveIdsFromIQ(response);

            SimpleDateFormat format = new SimpleDateFormat(
                   RendezvousItem.ISO_8601_DATE_FORMAT, Locale.ROOT);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));

            Date updated = new Date(format.parse(item.getUpdated()).getTime());

            Assert.assertTrue(updated.after(beforeMessage));
            Assert.assertTrue(updated.before(afterMessage));
            Assert.assertTrue(aliveIDs.contains(CLIENT));
            Assert.assertEquals(1, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }    
    
    @After
    public void tearDown() {
        try {           
            register.deleteAccount();            
        } catch (XMPPException e1) {
            fail(e1.getMessage());
        }
        xmppClient.disconnect();
        try{
            rendezvousXmppComponent.disconnect();
        } catch (ComponentException e) {
            fail(e.getMessage());
        }
    }        
}
