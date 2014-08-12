package org.fogbowcloud.rendezvous.xmpp.util;

public abstract class RSMElement implements Comparable<RSMElement>{
	
	public abstract String getId();
	
	@Override
	public int compareTo(RSMElement o) {
		return getId().compareTo(o.getId());
	}
}
