package org.fogbow.xmpp;

import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.fogbow.cloud.ResourcesInfo;
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

public class TestRendezvousXMPPComponent {

    // server properties
    private static final int SERVER_CLIENT_PORT = 5222;
    private static final int SERVER_COMPONENT_PORT = 5347;
    private static final String SERVER_HOST = "localhost";

    // client properties
    private static final String CLIENT = "user1@test.com";
    private static final String CLIENT_PASS = "user1";

    // rendezvous component properties
    private static final String RENDEZVOUS_COMPONENT_URL = "rendezvous.test.com";
    private static final String RENDEZVOUS_COMPONENT_PASS = "password";

    private static final String WHOISALIVE_NAMESPACE = "http://fogbowcloud.org/rendezvous/whoisalive";
    private static final String IAMALIVE_NAMESPACE = "http://fogbowcloud.org/rendezvous/iamalive";

    private static final int TIMEOUT = 10000;
    private static final int TIMEOUT_GRACE = 500;

    XMPPClient xmppClient;
    RendezvousXMPPComponent rendezvousXmppComponent;

    IQ response;

    @Before
    public void setUp() {

        // initializing xmpp component
        rendezvousXmppComponent = new RendezvousXMPPComponent(
                RENDEZVOUS_COMPONENT_URL, RENDEZVOUS_COMPONENT_PASS,
                SERVER_HOST, SERVER_COMPONENT_PORT, TIMEOUT);

        rendezvousXmppComponent.setDescription("Rendezvous Component");
        rendezvousXmppComponent.setName("rendezvous");
        try {
            rendezvousXmppComponent.connect();
        } catch (ComponentException e1) {
            e1.printStackTrace();
            fail(e1.getMessage());
        }

        rendezvousXmppComponent.process();

        // initializing client
        xmppClient = new XMPPClient(CLIENT, CLIENT_PASS, SERVER_HOST,
                SERVER_CLIENT_PORT);

        try {
            xmppClient.connect();
            xmppClient.login();
            xmppClient.process(false);
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Client set up problem!");
        }
    }

    @Test(expected = XMPPException.class)
    public void testInvalidIQ() throws XMPPException {

        String invalidNamespace = "invalidnamespace";
        IQ iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", invalidNamespace);

        response = (IQ) xmppClient.syncSend(iq);
    }

    private IQ createIAmAliveIQ() {
        IQ iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        Element statusEl = iq.getElement()
                .addElement("query", IAMALIVE_NAMESPACE).addElement("status");
        statusEl.addElement("cpu-idle").setText("valor1");
        statusEl.addElement("cpu-inuse").setText("valor2");
        statusEl.addElement("mem-idle").setText("valor3");
        statusEl.addElement("mem-inuse").setText("valor4");
        return iq;
    }

    @Test
    public void testSyncImAliveSingleElement() {
        IQ iq = createIAmAliveIQ();

        try {
            response = (IQ) xmppClient.syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());

            iq = createWhoIsAliveIQ();

            response = (IQ) xmppClient.syncSend(iq);
            ArrayList<String> aliveIDs = getAliveIdsFromIQ(response);

            Assert.assertTrue(aliveIDs.contains(CLIENT));
            Assert.assertEquals(1, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testImAsyncAliveSingleElement() {
        IQ iq = createIAmAliveIQ();

        PacketFilter filter = new AndFilter(new PacketTypeFilter(IQ.class), 
                new ToContainsFilter(CLIENT));
        
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
                    "yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ROOT);

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

    private ArrayList<String> getAliveIdsFromIQ(IQ responseFromWhoIsAliveIQ) {
        ArrayList<String> aliveIds = new ArrayList<String>();

        for (WhoIsAliveResponseItem item : getItemsFromIQ(responseFromWhoIsAliveIQ)) {
            aliveIds.add(item.getResources().getId());
        }

        return aliveIds;
    }

    private ArrayList<WhoIsAliveResponseItem> getItemsFromIQ(
            IQ responseFromWhoIsAliveIQ) {
        Element queryElement = responseFromWhoIsAliveIQ.getElement().element(
                "query");
        Iterator<Element> itemIterator = queryElement.elementIterator("item");

        ArrayList<WhoIsAliveResponseItem> aliveItems = new ArrayList<WhoIsAliveResponseItem>();

        while (itemIterator.hasNext()) {
            Element itemEl = (Element) itemIterator.next();

            Attribute id = itemEl.attribute("id");
            Element statusEl = itemEl.element("status");

            String cpuIdle = statusEl.element("cpu-idle").getText();
            String cpuInUse = statusEl.element("cpu-inuse").getText();
            String memIdle = statusEl.element("mem-idle").getText();
            String memInUse = statusEl.element("mem-inuse").getText();
            String updated = statusEl.element("updated").getText();

            ResourcesInfo resources = new ResourcesInfo(id.getValue(), cpuIdle,
                    cpuInUse, memIdle, memInUse);
            WhoIsAliveResponseItem item = new WhoIsAliveResponseItem(resources,
                    updated);
            aliveItems.add(item);
        }

        return aliveItems;
    }

    private IQ createWhoIsAliveIQ() {
        IQ iq;
        iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", WHOISALIVE_NAMESPACE);
        return iq;
    }

    @Test
    public void testIamAlive2EqualElements() {

        IQ iq = createIAmAliveIQ();

        try {
            response = (IQ) xmppClient.syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());

            iq = createIAmAliveIQ();

            response = (IQ) xmppClient.syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());

            iq = createWhoIsAliveIQ();

            response = (IQ) xmppClient.syncSend(iq);

            ArrayList<String> aliveIDs = getAliveIdsFromIQ(response);

            Assert.assertEquals(1, aliveIDs.size());
            Assert.assertTrue(aliveIDs.contains(CLIENT));
        } catch (XMPPException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testIamAlive2Clients() {
        // set up client 2
        XMPPClient xmppClient2 = new XMPPClient("user2@test.com", "user2",
                SERVER_HOST, SERVER_CLIENT_PORT);
        try {
            xmppClient2.connect();
            xmppClient2.login();
            xmppClient2.process(false);

            IQ iq = createIAmAliveIQ();

            // send am alive! from client 1
            response = (IQ) xmppClient.syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());

            // send am alive! from client 2
            response = (IQ) xmppClient2.syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());

            // send Who is Alive?
            iq = createWhoIsAliveIQ();
            response = (IQ) xmppClient.syncSend(iq);
            ArrayList<String> aliveIDs = getAliveIdsFromIQ(response);

            // asserts
            Assert.assertEquals(2, aliveIDs.size());
            Assert.assertTrue(aliveIDs.contains(CLIENT));
            Assert.assertTrue(aliveIDs.contains("user2@test.com"));

        } catch (XMPPException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            xmppClient2.disconnect();
        }
    }
    
    

    @Test
    public void testIamAliveManyClients() {

        //stopping components and client
        xmppClient.disconnect();
        try {
            rendezvousXmppComponent.disconnect();
        } catch (ComponentException e2) {
            fail(e2.getMessage());
        }
        
        //Initializing xmpp component with big Timeout     
        rendezvousXmppComponent = new RendezvousXMPPComponent(
                RENDEZVOUS_COMPONENT_URL, RENDEZVOUS_COMPONENT_PASS,
                SERVER_HOST, SERVER_COMPONENT_PORT, 100 * TIMEOUT);

        rendezvousXmppComponent.setDescription("Rendezvous Component");
        rendezvousXmppComponent.setName("rendezvous");
        try {
            rendezvousXmppComponent.connect();
        } catch (ComponentException e1) {
            e1.printStackTrace();
            fail(e1.getMessage());
        }

        rendezvousXmppComponent.process();

        //creating clients
        int begin = 100;
        int numberOfXmppClients = 1000;

        XMPPClient otherXmppClient;
        for (int i = begin; i < begin + numberOfXmppClients; i++) {
            String user = "user" + i;
            otherXmppClient = new XMPPClient(user + "@test.com", user,
                    SERVER_HOST, SERVER_CLIENT_PORT);

            XEP0077 register = new XEP0077();
            try {
                otherXmppClient.registerPlugin(register);
                otherXmppClient.connect();

                register.createAccount(user + "@test.com", user);

                otherXmppClient.login();
                otherXmppClient.process(false);
                
                IQ iq = createIAmAliveIQ();
                
                PacketFilter filter = new AndFilter(new PacketTypeFilter(IQ.class), 
                        new ToContainsFilter(user + "@test.com"));
                
                PacketListener callback = new PacketListener() {
                    public void processPacket(Packet packet) {
                        IQ response = (IQ) packet;
                        Assert.assertEquals(Type.result, response.getType());
                    }
                };
                
                otherXmppClient.on(filter, callback);
                otherXmppClient.send(iq);

                register.deleteAccount();
            } catch (XMPPException e) {
                e.printStackTrace();
                fail(e.getMessage());
            } finally {
                otherXmppClient.disconnect();
            }
        }

        // initializing client
        xmppClient = new XMPPClient(CLIENT, CLIENT_PASS, SERVER_HOST,
                SERVER_CLIENT_PORT);

        try {
            xmppClient.connect();
            xmppClient.login();
            xmppClient.process(false);
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Client set up problem!");
        }

        try {
            IQ iq = createWhoIsAliveIQ();
            response = (IQ) xmppClient.syncSend(iq);
            ArrayList<String> aliveIDs = getAliveIdsFromIQ(response);

            for (int i = begin; i < begin + numberOfXmppClients; i++) {
                String user = "user" + i;
                Assert.assertTrue(aliveIDs.contains(user + "@test.com"));
            }
            
            Assert.assertEquals(numberOfXmppClients, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
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
            Thread.sleep(TIMEOUT + TIMEOUT_GRACE);

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
    public void testIAmLiveXmppResponse() {
        IQ iq = createIAmAliveIQ();

        try {
            response = (IQ) xmppClient.syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @After
    public void tearDown() {
        xmppClient.disconnect();
        try {
            rendezvousXmppComponent.disconnect();
        } catch (ComponentException e) {
            fail(e.getMessage());
        }
    }

    class WhoIsAliveResponseItem {

        private ResourcesInfo resources;
        private String updated;

        public WhoIsAliveResponseItem(ResourcesInfo resources, String updated) {
            this.resources = resources;
            this.updated = updated;
        }

        public ResourcesInfo getResources() {
            return resources;
        }

        public String getUpdated() {
            return updated;
        }
    }
}
