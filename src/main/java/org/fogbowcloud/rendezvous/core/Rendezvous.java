package org.fogbowcloud.rendezvous.core;

import java.util.List;

public interface Rendezvous {

	void iAmAlive(RendezvousItem rendezvousItem);
	
	List<RendezvousItem> whoIsAlive();

	void init();
}
