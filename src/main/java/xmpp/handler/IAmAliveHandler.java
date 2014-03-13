package xmpp.handler;

import org.dom4j.Element;
import org.jamppa.component.handler.AbstractQueryHandler;
import org.ourgrid.cloud.Rendezvous;
import org.ourgrid.cloud.ResourcesInfo;
import org.xmpp.packet.IQ;

public class IAmAliveHandler extends AbstractQueryHandler {

    final static String NAMESPACE = "http://fogbowcloud.org/rendezvous/iamalive";
    Rendezvous rendezvous;

    public IAmAliveHandler(Rendezvous rendezvous) {
        super(NAMESPACE);
        this.rendezvous = rendezvous;
    }

    public IQ handle(IQ iq) {
        String id = iq.getFrom().toBareJID();

        Element statusElement = iq.getElement().element("query")
                .element("status");

        String cpuIdle = statusElement.element("cpu-idle").getText();
        String cpuInUse = statusElement.element("cpu-inuse").getText();
        String memIdle = statusElement.element("mem-idle").getText();
        String memInUse = statusElement.element("mem-inuse").getText();

        ResourcesInfo resources = new ResourcesInfo(id ,cpuIdle, cpuInUse, memIdle,
                memInUse);

        rendezvous.iAmAlive(resources);

        IQ response = IQ.createResultIQ(iq);
        return response;
    }
}
