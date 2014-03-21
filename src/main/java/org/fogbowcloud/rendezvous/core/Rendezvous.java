package org.fogbowcloud.rendezvous.core;

import java.util.List;

public interface Rendezvous {

	void iAmAlive(ResourcesInfo resourcesInfo);
	
	List<RendezvousItem> whoIsAlive();

}
