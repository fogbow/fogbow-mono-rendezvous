package org.ourgrid.cloud;

public class RendezvousItem {
	private long lastTime ;
	private ResourcesInfo resourcesInfo;

	public RendezvousItem (ResourcesInfo resourcesInfo) {
		lastTime = System.currentTimeMillis();
		this.resourcesInfo = resourcesInfo;
	}
	
	public ResourcesInfo getResourcesInfo() {
        return resourcesInfo;
    }

    public long getLastTime() {
		return lastTime;
	}

}
