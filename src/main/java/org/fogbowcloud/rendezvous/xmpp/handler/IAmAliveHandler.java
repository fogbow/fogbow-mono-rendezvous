package org.fogbowcloud.rendezvous.xmpp.handler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.Rendezvous;
import org.fogbowcloud.rendezvous.core.ResourcesInfo;
import org.fogbowcloud.rendezvous.core.model.Flavor;
import org.jamppa.component.handler.AbstractQueryHandler;
import org.xmpp.packet.IQ;

public class IAmAliveHandler extends AbstractQueryHandler {
	
    private final static String NAMESPACE = "http://fogbowcloud.org/rendezvous/iamalive";
    private Rendezvous rendezvous;

    public IAmAliveHandler(Rendezvous rendezvous) {
        super(NAMESPACE);
        this.rendezvous = rendezvous;
    }

    @SuppressWarnings("unchecked")
	public IQ handle(IQ iq) {
        String id = iq.getFrom().toBareJID();

        Element queryEl = iq.getElement().element("query");
		Element statusElement = queryEl.element("status");
        
        Element certEl = queryEl.element("cert");
        String cert = null;
        if (certEl != null) {
        	cert = certEl.getText();
        }
        String cpuIdle = statusElement.element("cpu-idle").getText();
        String cpuInUse = statusElement.element("cpu-inuse").getText();
        String memIdle = statusElement.element("mem-idle").getText();
        String memInUse = statusElement.element("mem-inuse").getText();
        
        List<Flavor> flavoursList = new LinkedList<Flavor>();
		Iterator<Element> flavourIterator = statusElement
				.elementIterator("flavor");
		while(flavourIterator.hasNext()) {
			Element flavour = (Element) flavourIterator.next();
			String name = flavour.element("name").getText();
			String cpu = flavour.element("cpu").getText();
			String mem = flavour.element("mem").getText();
			int capacity = Integer.parseInt(flavour.element("capacity")
					.getText());
			Flavor flavor = new Flavor(name, cpu, mem, capacity);
			flavoursList.add(flavor);
		}
        ResourcesInfo resources = new ResourcesInfo(id ,cpuIdle, cpuInUse, memIdle,
                memInUse, flavoursList, cert);
        //TODO handle certificate?
        rendezvous.iAmAlive(resources);

        IQ response = IQ.createResultIQ(iq);
        return response;
    }
}
