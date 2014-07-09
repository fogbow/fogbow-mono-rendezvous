package org.fogbowcloud.rendezvous.core;

import java.util.List;

import org.fogbowcloud.rendezvous.xmpp.model.WhoIsAliveResponseItem;

public interface Rendezvous {

	void iAmAlive(ResourcesInfo resourcesInfo);
	
	List<RendezvousItem> whoIsAlive();
}
