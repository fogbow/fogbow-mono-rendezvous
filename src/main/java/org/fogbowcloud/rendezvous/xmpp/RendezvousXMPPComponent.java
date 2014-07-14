package org.fogbowcloud.rendezvous.xmpp;

import java.util.concurrent.ScheduledExecutorService;

import org.fogbowcloud.rendezvous.core.Rendezvous;
import org.fogbowcloud.rendezvous.core.RendezvousImpl;
import org.fogbowcloud.rendezvous.xmpp.handler.IAmAliveHandler;
import org.fogbowcloud.rendezvous.xmpp.handler.WhoIsAliveHandler;
import org.fogbowcloud.rendezvous.xmpp.handler.WhoIsAliveSyncHandler;
import org.jamppa.component.XMPPComponent;

public class RendezvousXMPPComponent extends XMPPComponent {

	private Rendezvous rendezvous;

	public RendezvousXMPPComponent(String jid, String password, String server,
			int port, String[] neighbors) {
		this(jid, password, server, port, RendezvousImpl.DEFAULT_TIMEOUT, neighbors);
	}

	public RendezvousXMPPComponent(String jid, String password, String server,
			int port, long timeout, String[] neighbors) {
		super(jid, password, server, port);
		rendezvous = new RendezvousImpl(timeout, this, neighbors);
		addGetHandlers();
	}
	
	public RendezvousXMPPComponent(String jid, String password, String server,
			int port, long timeout, String[] neighbors, ScheduledExecutorService executor) {
		super(jid, password, server, port);
		rendezvous = new RendezvousImpl(timeout, this, neighbors, executor);
		addGetHandlers();
	}

	private void addGetHandlers() {
		addGetHandler(new IAmAliveHandler(rendezvous));
		addGetHandler(new WhoIsAliveHandler(rendezvous));
		addGetHandler(new WhoIsAliveSyncHandler(rendezvous));
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