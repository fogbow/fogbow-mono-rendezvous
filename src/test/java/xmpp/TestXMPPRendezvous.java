package xmpp;

import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.jamppa.client.XMPPClient;
import org.jivesoftware.smack.XMPPException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;

public class TestXMPPRendezvous {
    
    //server properties
    private static final int CLIENT_PORT = 5222;
    private static final String SERVER_HOST = "localhost";
    
    //client properties
    private static final String CLIENT = "user1@test.com";
    private static final String CLIENT_PASS = "user1";
   
    //rendezvous component properties
    private static final String RENDEZVOUS_COMPONENT_URL = "rendezvous.test.com";
    private static final int TIMEOUT =  10000;
    private static final int TIMEOUT_GRACE = 500;

    XMPPClient xmppClient;
    IQ response;

    @Before
    public void setUp() {

        // initializing client
        xmppClient = new XMPPClient(CLIENT, CLIENT_PASS, SERVER_HOST, CLIENT_PORT);

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
    // criar Erro
    public void testInvalidIQ() throws XMPPException {
      
        String invalidNamespace = "invalidnamespace";
        IQ iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", invalidNamespace);

        response = (IQ) xmppClient.syncSend(iq);

    }

    //TODO Is it necessary content? 
    private IQ createIAmAliveIQ() {
        IQ iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", "IamAlive");
        return iq;
    }

    @Test
    public void testImAliveSingleElement() {
        IQ iq = createIAmAliveIQ();

        try {
            response = (IQ) xmppClient.syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }

        iq = createWhoIsAliveIQ();

        try {
            response = (IQ) xmppClient.syncSend(iq);
            ArrayList<String> aliveIDs = (ArrayList<String>) response
                    .getElement().element("query").element("content").getData();
            Assert.assertTrue(aliveIDs.contains(iq.getFrom()));
            Assert.assertEquals(1, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }
    }

    private IQ createWhoIsAliveIQ() {
        IQ iq;
        iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", "WhoIsAlive");
        return iq;
    }

    @Test
    public void testIamAlive2EqualElements() {

        IQ iq = createIAmAliveIQ();

        try {
            response = (IQ) xmppClient.syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }

        iq = createIAmAliveIQ();

        try {
            response = (IQ) xmppClient.syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }

        iq = createWhoIsAliveIQ();

        try {
            response = (IQ) xmppClient.syncSend(iq);
            ArrayList<String> aliveIDs = (ArrayList<String>) response
                    .getElement().element("query").element("content").getData();
            Assert.assertTrue(aliveIDs.contains(iq.getFrom()));
            Assert.assertEquals(1, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    // criar Erro
    public void testIAmAliveNullContent() {

        //TODO I think this is invalid case

        IQ iq = createIAmAliveIQ();

        try {
            response = (IQ) xmppClient.syncSend(iq);

        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }
    }

    @Test
    public void testIamAlive2IQs() {
        // set up client 2
        XMPPClient xmppClient2 = new XMPPClient("user2@test.com", "user2",
                SERVER_HOST, 5222);
        try {
            xmppClient2.connect();
            xmppClient2.login();
            // /client.process(false);
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Client set up problem!");
        }
        
        IQ iq = createIAmAliveIQ();

        // send am alive! from client 1
        try {
            response = (IQ) xmppClient.syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }
        // send am alive! from client 2
        try {
            response = (IQ) xmppClient2.syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }

        iq = createWhoIsAliveIQ();
        // send Who is Alive?
        try {
            response = (IQ) xmppClient.syncSend(iq);
            ArrayList<String> aliveIDs = (ArrayList<String>) response
                    .getElement().element("query").element("content").getData();
            // asserts
            Assert.assertTrue(aliveIDs.contains(CLIENT));
            Assert.assertTrue(aliveIDs.contains("user2@test.com"));
            Assert.assertEquals(2, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }
        xmppClient2.disconnect();
    }content

    @Test
    public void testWhoisAliveEmpty() {

        IQ iq = createWhoIsAliveIQ();

        try {
            response = (IQ) xmppClient.syncSend(iq);
            ArrayList<String> aliveIDs = (ArrayList<String>) response
                    .getElement().element("query").element("content").getData();
            Assert.assertEquals(0, aliveIDs.size());

        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }
    }
    
    @Test
    public void testWhoIsAliveAfterTime() throws InterruptedException {

        IQ iq = createIAmAliveIQ();

        try {
            response = (IQ) xmppClient.syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }

        iq = createWhoIsAliveIQ();

        try {
            response = (IQ) xmppClient.syncSend(iq);
            ArrayList<String> aliveIDs = (ArrayList<String>) response
                    .getElement().element("query").element("content").getData();
            Assert.assertTrue(aliveIDs.contains(iq.getFrom()));
            Assert.assertEquals(1, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }
        
        //sleep
        Thread.sleep(TIMEOUT + TIMEOUT_GRACE);
        
        iq = createWhoIsAliveIQ();

        try {
            response = (IQ) xmppClient.syncSend(iq);
            ArrayList<String> aliveIDs = (ArrayList<String>) response
                    .getElement().element("query").element("content").getData();
            Assert.assertEquals(0, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }
        
    }
}
