package xmpp;

import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.jamppa.client.XMPPClient;
import org.jivesoftware.smack.XMPPException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;

public class TestXMPPRendezvous {
    
    //server properties
    private static final int SERVER_CLIENT_PORT = 5222;
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
        xmppClient = new XMPPClient(CLIENT, CLIENT_PASS, SERVER_HOST, SERVER_CLIENT_PORT);

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
        iq.getElement().addElement("query", "IamAlive");
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
            
            Assert.assertTrue(aliveIDs.contains(response.getFrom()));
            Assert.assertEquals(1, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    
    private ArrayList<String> getAliveIdsFromIQ(IQ responseFromWhoIsAliveIQ) {
        return (ArrayList<String>) responseFromWhoIsAliveIQ
                .getElement().element("query").element("content").getData();
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
        
            iq = createIAmAliveIQ();
        
            response = (IQ) xmppClient.syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());
        
            iq = createWhoIsAliveIQ();
        
            response = (IQ) xmppClient.syncSend(iq);

            ArrayList<String> aliveIDs = getAliveIdsFromIQ(response);
            
            Assert.assertTrue(aliveIDs.contains(response.getFrom()));
            Assert.assertEquals(1, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIAmAliveNullContent() {

        //TODO I think this is invalid case

        IQ iq = createIAmAliveIQ();

        try {
            response = (IQ) xmppClient.syncSend(iq);

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
            Assert.assertTrue(aliveIDs.contains(CLIENT));
            Assert.assertTrue(aliveIDs.contains("user2@test.com"));
            Assert.assertEquals(2, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            xmppClient2.disconnect();    
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
            
            Assert.assertTrue(aliveIDs.contains(CLIENT));
            Assert.assertEquals(1, aliveIDs.size());
        
            //sleeping
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
    public void tearDown(){
        xmppClient.disconnect();
    }
}
