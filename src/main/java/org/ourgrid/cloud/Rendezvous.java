package org.ourgrid.cloud;

import java.util.List;

public interface Rendezvous {

	void iAmAlive(ResourcesInfo resourcesInfo);
	
	List<RendezvousItem> whoIsAlive();

}
