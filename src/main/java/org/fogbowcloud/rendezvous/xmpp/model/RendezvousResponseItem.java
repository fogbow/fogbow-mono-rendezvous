package org.fogbowcloud.rendezvous.xmpp.model;

import java.util.List;

import org.fogbowcloud.rendezvous.core.RendezvousItem;

public class RendezvousResponseItem {

	private List<RendezvousItem> managers;

	public RendezvousResponseItem(List<RendezvousItem> managers) {
		this.managers = managers;
	}

	public List<RendezvousItem> getManagers() {
		return managers;
	}

	public void setManagers(List<RendezvousItem> managers) {
		this.managers = managers;
	}
}
