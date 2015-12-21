package org.fogbowcloud.rendezvous.xmpp.util;

public abstract class FederationMember implements Comparable<FederationMember>{
	
	public abstract String getMemberId();
	
	@Override
	public int compareTo(FederationMember o) {
		return getMemberId().compareTo(o.getMemberId());
	}
}
