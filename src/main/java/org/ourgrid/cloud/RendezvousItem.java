package org.ourgrid.cloud;

public class RendezvousItem {
	private long lastTime ;

	public RendezvousItem () {
		lastTime = System.currentTimeMillis();
	}
	
	public long getLastTime() {
		return lastTime;
	}
	
	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}
}
