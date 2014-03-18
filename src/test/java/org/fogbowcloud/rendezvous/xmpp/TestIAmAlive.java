package org.fogbowcloud.rendezvous.xmpp;

import java.util.ArrayList;

import org.fogbowcloud.rendezvous.xmpp.model.TestRendezvousXMPPComponent;
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

public class TestIAmAlive extends TestRendezvousXMPPComponent{
		
    @Before
    public void setUp() {

        initializeXMPPRendezvousComponent(TEST_DEFAULT_TIMEOUT);

        initializeXMPPClient();
    }
    
    @Test(expected = XMPPException.class)
    public void testInvalidIQ() throws XMPPException {

        String invalidNamespace = "invalidnamespace";
        IQ iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", invalidNamespace);

        response = (IQ) xmppClient.syncSend(iq);
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
        }
    }

    @Test
    public void testIamAlive2Clients() {
        // set up client 2
        
        XEP0077 reg = new XEP0077();
        
        XMPPClient xmppClient2 = new XMPPClient("testuser2@test.com", "testuser2",
                SERVER_HOST, SERVER_CLIENT_PORT);
        try {
            xmppClient2.registerPlugin(reg);
            xmppClient2.connect();

            reg.createAccount("testuser2@test.com", "testuser2");

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
            Assert.assertTrue(aliveIDs.contains("testuser2@test.com"));
            
        } catch (XMPPException e) {
            e.printStackTrace();
        } finally {            
            try {
                reg.deleteAccount();
            } catch (XMPPException e) {
            	e.printStackTrace();
            }
            xmppClient2.disconnect();
        }
    }
    
    @Test
    public void testIamAliveManyClients() {

        //stopping components and client
        tearDown();
        
        //Initializing XMPP component with big Timeout     
        initializeXMPPRendezvousComponent(100 * TEST_DEFAULT_TIMEOUT);
        
        //creating clients
        int begin = 100;
        int numberOfXmppClients = 1000;

        XMPPClient otherXmppClient;
        for (int i = begin; i < begin + numberOfXmppClients; i++) {
            String user = "user" + i;
            otherXmppClient = new XMPPClient(user + "@test.com", user,
                    SERVER_HOST, SERVER_CLIENT_PORT);

            XEP0077 reg = new XEP0077();
            try {
                otherXmppClient.registerPlugin(reg);
                otherXmppClient.connect();

                reg.createAccount(user + "@test.com", user);

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

                reg.deleteAccount();
            } catch (XMPPException e) {
                e.printStackTrace();
            } finally {
                otherXmppClient.disconnect();
            }
        }

        // initializing client
        initializeXMPPClient();

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
        }
    }

    @After
    public void tearDown() {
        try {           
            register.deleteAccount();            
        } catch (XMPPException e1) {
            e1.printStackTrace();
        }
        xmppClient.disconnect();
        try{
            rendezvousXmppComponent.disconnect();
        } catch (ComponentException e) {
        	e.printStackTrace();
        }
    }    
}
