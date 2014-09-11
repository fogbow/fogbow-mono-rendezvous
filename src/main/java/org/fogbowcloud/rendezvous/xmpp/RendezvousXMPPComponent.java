package org.fogbowcloud.rendezvous.xmpp;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.fogbowcloud.rendezvous.core.Rendezvous;
import org.fogbowcloud.rendezvous.core.RendezvousImpl;
import org.fogbowcloud.rendezvous.xmpp.handler.IAmAliveHandler;
import org.fogbowcloud.rendezvous.xmpp.handler.WhoIsAliveHandler;
import org.fogbowcloud.rendezvous.xmpp.handler.WhoIsAliveSyncHandler;
import org.jamppa.component.XMPPComponent;
import org.xmpp.packet.IQ;

public class RendezvousXMPPComponent extends XMPPComponent {

	private Rendezvous rendezvous;

	public RendezvousXMPPComponent(String jid, String password, String server,
			int port, Properties properties) {
		this(jid, password, server, port, properties, Executors
				.newScheduledThreadPool(10));
	}

	public RendezvousXMPPComponent(String jid, String password, String server,
			int port, Properties properties, ScheduledExecutorService executor) {
		super(jid, password, server, port);
		rendezvous = new RendezvousImpl(this, properties, executor);
		addGetHandlers();
	}

	private void addGetHandlers() {
		addGetHandler(new IAmAliveHandler(rendezvous));
		addGetHandler(new WhoIsAliveHandler(rendezvous));
		addGetHandler(new WhoIsAliveSyncHandler(rendezvous));
	}
	
	@Override
	protected void handleIQError(IQ iq) {
		super.handleIQResult(iq);
	}

	@Override
	public void process() {
		rendezvous.init();
		super.process();
	}

	public Rendezvous getRendezvous() {
		return rendezvous;
	}
}