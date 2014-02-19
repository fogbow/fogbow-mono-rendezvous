package org.ourgrid.cloud;

import java.util.List;

public interface Rendezvous {

	void iAmAlive(String id);
	
	List<String> whoIsAlive();

}
