package org.fogbowcloud.rendezvous.xmpp.model;

import java.util.List;

import org.fogbowcloud.rendezvous.core.RendezvousItem;

public class RendezvousResponseItem {

	private List<String> neighbors;
	private List<RendezvousItem> knownManagersAlive;

	public RendezvousResponseItem(List<String> neighbors,
			List<RendezvousItem> knownManagersAlive) {
		this.neighbors = neighbors;
		this.knownManagersAlive = knownManagersAlive;
	}

	public List<String> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(List<String> neighbors) {
		this.neighbors = neighbors;
	}

	public List<RendezvousItem> getKnownManagersAlive() {
		return knownManagersAlive;
	}

	public void setKnownManagersAlive(
			List<RendezvousItem> knownManagersAlive) {
		this.knownManagersAlive = knownManagersAlive;
	}
}
