package org.fogbowcloud.rendezvous.xmpp.handler;

import java.util.Properties;

import org.dom4j.Element;
import org.fogbowcloud.rendezvous.core.Rendezvous;
import org.fogbowcloud.rendezvous.core.RendezvousImpl;
import org.fogbowcloud.rendezvous.core.RendezvousItem;
import org.jamppa.component.handler.AbstractQueryHandler;
import org.xmpp.packet.IQ;

public class IAmAliveHandler extends AbstractQueryHandler {
	
    private static final String I_AM_ALIVE_PERIOD = "iamalive-period";
	private final static String NAMESPACE = "http://fogbowcloud.org/rendezvous/iamalive";
    private Rendezvous rendezvous;
    private Properties properties;

    public IAmAliveHandler(Rendezvous rendezvous, Properties properties) {
        super(NAMESPACE);
        this.properties = properties;
        this.rendezvous = rendezvous;
    }

	public IQ handle(IQ iq) {
        String id = iq.getFrom().toBareJID();
        
        Element queryEl = iq.getElement().element("query");
        
        Element certEl = queryEl.element("cert");
        String cert = null;
        if (certEl != null) {
        	cert = certEl.getText();
        }
             
		long iAmAlivePeriod = parseLongFromConfiguration(RendezvousImpl.PROP_I_AM_ALIVE_PERIOD,
				RendezvousImpl.DEFAULT_I_AM_ALIVE_PERIOD);
		long iAmAliveMaxMessageLost = parseLongFromConfiguration(
				RendezvousImpl.PROP_I_AM_ALIVE_MAX_MESSAGE_LOST,
				RendezvousImpl.DEFAULT_I_AM_ALIVE_MAX_MESSAGE_LOST);
       
		//TODO handle certificate?
		rendezvous.iAmAlive(new RendezvousItem(id, cert, iAmAlivePeriod * iAmAliveMaxMessageLost));
        
        IQ resultIQ = IQ.createResultIQ(iq);

		Element queryElement = resultIQ.getElement().addElement("query", NAMESPACE);
        
        String iAmAliveMessageCount = this.properties.getProperty(RendezvousImpl.PROP_I_AM_ALIVE_PERIOD);
        if (iAmAliveMessageCount != null && !iAmAliveMessageCount.isEmpty()) {
        	Element iAmAliveMessageEl = queryElement.addElement(I_AM_ALIVE_PERIOD);
        	iAmAliveMessageEl.setText(iAmAliveMessageCount);
        }
        
        return resultIQ;        
    }
	
	private long parseLongFromConfiguration(String propName, long defaultVaue) {
		String propValue = properties.getProperty(propName);
		if (propValue == null || propValue.isEmpty()) {
			return defaultVaue;
		}
		long timeOut = Long.parseLong(propValue);
		if (timeOut < 0) {
			throw new IllegalArgumentException();
		}
		return timeOut;
	}	
}
