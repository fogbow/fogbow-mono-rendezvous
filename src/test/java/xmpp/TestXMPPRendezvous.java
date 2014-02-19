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
    private static final String RENDEZVOUS_COMPONENT_URL = null;
    private static final int TIMEOUT =  10000;
    private static final int TIMEOUT_GRACE = 500;
    XMPPClient client;
    IQ response;

    @Before
    public void setUp() {

        // initializing client
        client = new XMPPClient("user1@test.com", "user1", "localhost", 5222);

        try {
            client.connect();
            client.login();
            // /client.process(false);
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Client set up problem!");
        }

    }

    @Test(expected = IllegalArgumentException.class)
    // criar Erro
    public void testInvalidIQ() {

        String content = "invalid content";

        IQ iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", "IamAlive").addElement("content")
                .setText(content);

        try {
            response = (IQ) client.syncSend(iq);

        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }

    }

    @Test
    public void testImAliveSingleElement() {

        String content = "I am alive!";

        IQ iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", "IamAlive").addElement("content")
                .setText(content);

        try {
            response = (IQ) client.syncSend(iq);
            Assert.assertEquals("result", response.getType());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }

        content = "Who is alive?";

        iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", "WhoIsAlive").addElement("content")
                .setText(content);

        try {
            response = (IQ) client.syncSend(iq);
            ArrayList<String> aliveIDs = (ArrayList<String>) response
                    .getElement().element("query").element("content").getData();
            Assert.assertTrue(aliveIDs.contains(iq.getFrom()));
            Assert.assertEquals(1, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }
    }

    @Test
    public void testIamAlive2EqualElements() {

        String content = "I am alive!";

        IQ iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", "IamAlive").addElement("content")
                .setText(content);

        try {
            response = (IQ) client.syncSend(iq);
            Assert.assertEquals("result", response.getType());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }

        iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", "IamAlive").addElement("content")
                .setText(content);

        try {
            response = (IQ) client.syncSend(iq);
            Assert.assertEquals("result", response.getType());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }

        content = "Who is alive?";

        iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", "WhoIsAlive").addElement("content")
                .setText(content);

        try {
            response = (IQ) client.syncSend(iq);
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

        String content = null;

        IQ iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", "IamAlive").addElement("content")
                .setText(content);

        try {
            response = (IQ) client.syncSend(iq);

        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }
    }

    @Test
    public void testIamAlive2IQs() {
        // set up client 2
        XMPPClient client2 = new XMPPClient("user2@test.com", "user2",
                "localhost", 5222);
        try {
            client2.connect();
            client2.login();
            // /client.process(false);
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Client set up problem!");
        }

        String content = "I am alive!";

        IQ iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", "IamAlive").addElement("content")
                .setText(content);

        // send am alive! from client 1
        try {
            response = (IQ) client.syncSend(iq);
            Assert.assertEquals("result", response.getType());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }
        // send am alive! from client 2
        try {
            response = (IQ) client2.syncSend(iq);
            Assert.assertEquals("result", response.getType());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }

        content = "Who is alive?";

        iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", "WhoIsAlive").addElement("content")
                .setText(content);
        // send Who is Alive?
        try {
            response = (IQ) client.syncSend(iq);
            ArrayList<String> aliveIDs = (ArrayList<String>) response
                    .getElement().element("query").element("content").getData();
            // asserts
            Assert.assertTrue(aliveIDs.contains("user1@test.com"));
            Assert.assertTrue(aliveIDs.contains("user2@test.com"));
            Assert.assertEquals(2, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }
        client2.disconnect();
    }

    @Test
    public void testWhoisAliveEmpty() {

        String content = "Who is Alive?";

        IQ iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", "IamAlive").addElement("content")
                .setText(content);

        try {
            response = (IQ) client.syncSend(iq);
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
        String content = "I am alive!";

        IQ iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", "IamAlive").addElement("content")
                .setText(content);

        try {
            response = (IQ) client.syncSend(iq);
            Assert.assertEquals("result", response.getType());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }

        content = "Who is alive?";

        iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", "WhoIsAlive").addElement("content")
                .setText(content);

        try {
            response = (IQ) client.syncSend(iq);
            ArrayList<String> aliveIDs = (ArrayList<String>) response
                    .getElement().element("query").element("content").getData();
            Assert.assertTrue(aliveIDs.contains(iq.getFrom()));
            Assert.assertEquals(1, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }
        
        //dorme
        Thread.sleep(TIMEOUT + TIMEOUT_GRACE);
        
        iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", "WhoIsAlive").addElement("content")
                .setText(content);

        try {
            response = (IQ) client.syncSend(iq);
            ArrayList<String> aliveIDs = (ArrayList<String>) response
                    .getElement().element("query").element("content").getData();
            Assert.assertEquals(0, aliveIDs.size());
        } catch (XMPPException e) {
            e.printStackTrace();
            fail("Response problem!");
        }
        
    }
}
