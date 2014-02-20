package xmpp;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Element;
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
        iq.getElement().addElement("query", "iamalive");
        return iq;
    }

    @Test
    public void testImAliveSingleElement() {
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

    private ArrayList<String> getAliveIdsFromIQ(IQ responseFromWhoIsAliveIQ) {

        Element queryElement = responseFromWhoIsAliveIQ.getElement().element(
                "query");
        Iterator<Element> itemIterator = queryElement.elementIterator("item");

        ArrayList<String> aliveIds = new ArrayList<String>();

        while (itemIterator.hasNext()) {
            Element item = (Element) itemIterator.next();
            Attribute id = item.attribute("id");
            aliveIds.add(id.getValue());
        }

        return aliveIds;
    }

    private IQ createWhoIsAliveIQ() {
        IQ iq;
        iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", "whoisalive");
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
        // set up client 2

        int begin = 100;
        int numberOfXmppClients = 100;

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

                response = (IQ) otherXmppClient.syncSend(iq);
                Assert.assertEquals(Type.result, response.getType());

                // send Who is Alive
                iq = createWhoIsAliveIQ();
                response = (IQ) otherXmppClient.syncSend(iq);
                ArrayList<String> aliveIDs = getAliveIdsFromIQ(response);

                // asserts
                Assert.assertTrue(aliveIDs.contains(user + "@test.com"));
                Assert.assertEquals(i - begin, aliveIDs.size());
            } catch (XMPPException e) {
                e.printStackTrace();
                fail(e.getMessage());
            } finally {
                otherXmppClient.disconnect();
            }
        }

        try {
            IQ iq = createWhoIsAliveIQ();
            response = (IQ) xmppClient.syncSend(iq);
            ArrayList<String> aliveIDs = getAliveIdsFromIQ(response);

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

    @After
    public void tearDown() {
        xmppClient.disconnect();
        rendezvousXmppComponent.shutdown();
    }
}
