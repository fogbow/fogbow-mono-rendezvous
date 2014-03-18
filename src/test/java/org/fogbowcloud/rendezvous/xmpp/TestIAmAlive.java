package org.fogbowcloud.rendezvous.xmpp;

import java.util.ArrayList;

import org.fogbowcloud.rendezvous.xmpp.model.RendezvousTestHelper;
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

public class TestIAmAlive {
		
	private RendezvousTestHelper rendezvousTestHelper;
	
    @Before
    public void setUp() {
    	
    	rendezvousTestHelper = new RendezvousTestHelper();

        rendezvousTestHelper.initializeXMPPRendezvousComponent(RendezvousTestHelper.TEST_DEFAULT_TIMEOUT);

        rendezvousTestHelper.initializeXMPPClient();
    }
    
    @Test(expected = XMPPException.class)
    public void testInvalidIQ() throws XMPPException {
    	IQ response;
    	
        String invalidNamespace = "invalidnamespace";
        IQ iq = new IQ(Type.get);
        iq.setTo(RendezvousTestHelper.RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", invalidNamespace);

        response = (IQ) rendezvousTestHelper.getXmppClient().syncSend(iq);
    }  
    
    @Test
    public void testSyncImAliveSingleElement() {
    	IQ response;
        IQ iq = rendezvousTestHelper.createIAmAliveIQ();

        try {
            response = (IQ) rendezvousTestHelper.getXmppClient().syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());

            iq = rendezvousTestHelper.createWhoIsAliveIQ();

            response = (IQ) rendezvousTestHelper.getXmppClient().syncSend(iq);
            ArrayList<String> aliveIDs = rendezvousTestHelper.getAliveIdsFromIQ(response);

            Assert.assertTrue(aliveIDs.contains(RendezvousTestHelper.CLIENT));
            Assert.assertEquals(1, aliveIDs.size());
        } catch (XMPPException e) {
        }
    }    
    
    @Test
    public void testImAsyncAliveSingleElement() {
        IQ iq = rendezvousTestHelper.createIAmAliveIQ();

        PacketFilter filter = new AndFilter(new PacketTypeFilter(IQ.class), 
                new ToContainsFilter(RendezvousTestHelper.CLIENT));
        
        PacketListener callback = new PacketListener() {
            public void processPacket(Packet packet) {
                IQ response = (IQ) packet;
                Assert.assertEquals(Type.result, response.getType());
            }
        };
                   
        rendezvousTestHelper.getXmppClient().on(filter, callback);
        rendezvousTestHelper.getXmppClient().send(iq);           
    }  
    
    @Test
    public void testIamAlive2EqualElements() {
    	IQ response;
        IQ iq = rendezvousTestHelper.createIAmAliveIQ();

        try {
            response = (IQ) rendezvousTestHelper.getXmppClient().syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());

            iq = rendezvousTestHelper.createIAmAliveIQ();

            response = (IQ) rendezvousTestHelper.getXmppClient().syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());

            iq = rendezvousTestHelper.createWhoIsAliveIQ();

            response = (IQ) rendezvousTestHelper.getXmppClient().syncSend(iq);

            ArrayList<String> aliveIDs = rendezvousTestHelper.getAliveIdsFromIQ(response);

            Assert.assertEquals(1, aliveIDs.size());
            Assert.assertTrue(aliveIDs.contains(RendezvousTestHelper.CLIENT));
        } catch (XMPPException e) {
        }
    }

    @Test
    public void testIamAlive2Clients() {
        // set up client 2
    	IQ response;
        XEP0077 reg = new XEP0077();
        
        XMPPClient xmppClient2 = new XMPPClient("testuser2@test.com", "testuser2",
                RendezvousTestHelper.SERVER_HOST, RendezvousTestHelper.SERVER_CLIENT_PORT);
        try {
            xmppClient2.registerPlugin(reg);
            xmppClient2.connect();

            try {
            	reg.createAccount("testuser2@test.com", "testuser2");	
			} catch (XMPPException e) {
			}
            

            xmppClient2.login();
            xmppClient2.process(false);
        
            IQ iq = rendezvousTestHelper.createIAmAliveIQ();

            // send am alive! from client 1
            response = (IQ) rendezvousTestHelper.getXmppClient().syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());

            // send am alive! from client 2
            response = (IQ) xmppClient2.syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());

            // send Who is Alive?
            iq = rendezvousTestHelper.createWhoIsAliveIQ();
            response = (IQ) rendezvousTestHelper.getXmppClient().syncSend(iq);
            ArrayList<String> aliveIDs = rendezvousTestHelper.getAliveIdsFromIQ(response);

            // asserts
            Assert.assertEquals(2, aliveIDs.size());
            Assert.assertTrue(aliveIDs.contains(RendezvousTestHelper.CLIENT));
            Assert.assertTrue(aliveIDs.contains("testuser2@test.com"));
            
        } catch (XMPPException e) {
        } finally {            
            xmppClient2.disconnect();
        }
    }
    
    @Test
    public void testIamAliveManyClients() {
    	IQ response;
    	
        //stopping components and client
        tearDown();
        
        //Initializing XMPP component with big Timeout     
        rendezvousTestHelper.initializeXMPPRendezvousComponent(10000 * RendezvousTestHelper.TEST_DEFAULT_TIMEOUT);
        
        //creating clients
        int begin = 100;
        int numberOfXmppClients = 1000;

        XMPPClient otherXmppClient;
        for (int i = begin; i < begin + numberOfXmppClients; i++) {
            String user = "user" + i;
            otherXmppClient = new XMPPClient(user + "@test.com", user,
                    RendezvousTestHelper.SERVER_HOST, RendezvousTestHelper.SERVER_CLIENT_PORT);

            XEP0077 reg = new XEP0077();
            try {
                otherXmppClient.registerPlugin(reg);
                otherXmppClient.connect();
                         
                try {
                	reg.createAccount(user + "@test.com", user);
				} catch (XMPPException e) {
				}
			
                otherXmppClient.login();
                otherXmppClient.process(false);
                
                IQ iq = rendezvousTestHelper.createIAmAliveIQ();
                
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
                
                try {
        			Thread.sleep(2);
        		} catch (InterruptedException e1) {
        		}                
            } catch (XMPPException e) {
            } finally {
                otherXmppClient.disconnect();
            }
        }

        // initializing client
        rendezvousTestHelper.initializeXMPPClient();
        
        try {
            IQ iq = rendezvousTestHelper.createWhoIsAliveIQ();
            response = (IQ) rendezvousTestHelper.getXmppClient().syncSend(iq);
            ArrayList<String> aliveIDs = rendezvousTestHelper.getAliveIdsFromIQ(response);

            for (int i = begin; i < begin + numberOfXmppClients; i++) {
                String user = "user" + i;
                Assert.assertTrue(aliveIDs.contains(user + "@test.com"));
            }
            
            Assert.assertEquals(numberOfXmppClients, aliveIDs.size());
        } catch (XMPPException e) {
        }
    }
    
    @Test
    public void testIAmLiveXmppResponse() {
    	IQ response;
        IQ iq = rendezvousTestHelper.createIAmAliveIQ();

        try {
            response = (IQ) rendezvousTestHelper.getXmppClient().syncSend(iq);
            Assert.assertEquals(Type.result, response.getType());
        } catch (XMPPException e) {
        }
    }

    @After
    public void tearDown() {
        rendezvousTestHelper.disconnectXMPPClient();
        try{
            rendezvousTestHelper.disconnectRendezvousXMPPComponent();
        } catch (ComponentException e) {
        }
    }    
}
