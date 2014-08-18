package org.fogbowcloud.rendezvous.xmpp.model;

import java.util.List;

import org.fogbowcloud.rendezvous.core.RendezvousItem;

public class RendezvousResponseItem {

	private List<String> neighbors;
	private List<RendezvousItem> managers;

	public RendezvousResponseItem(List<String> neighbors,
			List<RendezvousItem> managers) {
		this.neighbors = neighbors;
		this.managers = managers;
	}

	public List<String> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(List<String> neighbors) {
		this.neighbors = neighbors;
	}

	public List<RendezvousItem> getManagers() {
		return managers;
	}

	public void setManagers(List<RendezvousItem> managers) {
		this.managers = managers;
	}
}
