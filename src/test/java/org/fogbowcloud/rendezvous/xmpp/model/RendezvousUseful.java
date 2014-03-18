package org.fogbowcloud.rendezvous.xmpp.model;

import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.ResourcesInfo;
import org.fogbowcloud.rendezvous.xmpp.RendezvousXMPPComponent;
import org.jamppa.client.XMPPClient;
import org.jamppa.client.plugin.xep0077.XEP0077;
import org.jivesoftware.smack.XMPPException;
import org.xmpp.component.ComponentException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;

public class RendezvousUseful {

    // server properties
	public static final int SERVER_CLIENT_PORT = 5222;
	public static final int SERVER_COMPONENT_PORT = 5347;
	public static final String SERVER_HOST = "localhost";

    // client properties
	public static final String CLIENT = "testuser@test.com";
	public static final String CLIENT_PASS = "testuser";

    // rendezvous component properties
	public static final String RENDEZVOUS_COMPONENT_URL = "rendezvous.test.com";
	public static final String RENDEZVOUS_COMPONENT_PASS = "password";

	public static final String WHOISALIVE_NAMESPACE = "http://fogbowcloud.org/rendezvous/whoisalive";
	public static final String IAMALIVE_NAMESPACE = "http://fogbowcloud.org/rendezvous/iamalive";

	public static final int TEST_DEFAULT_TIMEOUT = 10000;
	public static final int TIMEOUT_GRACE = 500;

    private XMPPClient xmppClient;
    private RendezvousXMPPComponent rendezvousXmppComponent;
    private XEP0077 register; 

    public RendezvousUseful() {
	}
    
    public void initializeXMPPClient() {
        register = new XEP0077();
        xmppClient = new XMPPClient(CLIENT, CLIENT_PASS, SERVER_HOST,
                SERVER_CLIENT_PORT);
      
        try{
            xmppClient.registerPlugin(register);
            xmppClient.connect();
            
            register.createAccount(CLIENT, CLIENT_PASS);
            
            xmppClient.login();
            xmppClient.process(false);
            
        } catch (XMPPException e) {
        	e.printStackTrace();
        }
    }

    public void initializeXMPPRendezvousComponent(int timeout) {
        rendezvousXmppComponent = new RendezvousXMPPComponent(
                RENDEZVOUS_COMPONENT_URL, RENDEZVOUS_COMPONENT_PASS,
                SERVER_HOST, SERVER_COMPONENT_PORT, timeout);

        rendezvousXmppComponent.setDescription("Rendezvous Component");
        rendezvousXmppComponent.setName("rendezvous");
        
        try {
            rendezvousXmppComponent.connect();
        } catch (ComponentException e1) {
            e1.printStackTrace();
        }

        rendezvousXmppComponent.process();
        
    }
    
    public void deleteAccount() throws XMPPException{
    	register.deleteAccount();
    }
    
    public void disconnectXMPPClient(){
    	xmppClient.disconnect();
    }
    
    public void disconnectRendezvousXMPPComponent() throws ComponentException{
    	rendezvousXmppComponent.disconnect();
    }

    public IQ createIAmAliveIQ() {
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

    public ArrayList<String> getAliveIdsFromIQ(IQ responseFromWhoIsAliveIQ) {
        ArrayList<String> aliveIds = new ArrayList<String>();

        for (WhoIsAliveResponseItem item : getItemsFromIQ(responseFromWhoIsAliveIQ)) {
            aliveIds.add(item.getResources().getId());
        }

        return aliveIds;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<WhoIsAliveResponseItem> getItemsFromIQ(
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

    public IQ createWhoIsAliveIQ() {
        IQ iq;
        iq = new IQ(Type.get);
        iq.setTo(RENDEZVOUS_COMPONENT_URL);
        iq.getElement().addElement("query", WHOISALIVE_NAMESPACE);
        return iq;
    }

	public XMPPClient getXmppClient() {
		return xmppClient;
	}

	public RendezvousXMPPComponent getRendezvousXmppComponent() {
		return rendezvousXmppComponent;
	}

	public XEP0077 getRegister() {
		return register;
	}
}
