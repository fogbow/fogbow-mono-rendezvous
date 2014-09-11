package org.fogbowcloud.rendezvous.xmpp.util;

public abstract class FederationMember implements Comparable<FederationMember>{
	
	public abstract String getId();
	
	@Override
	public int compareTo(FederationMember o) {
		return getId().compareTo(o.getId());
	}
}
