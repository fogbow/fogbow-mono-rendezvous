package org.fogbowcloud.rendezvous.xmpp.model;

import org.fogbowcloud.rendezvous.core.ResourcesInfo;

public class WhoIsAliveResponseItem {
    private ResourcesInfo resources;
    private String updated;

    public WhoIsAliveResponseItem(ResourcesInfo resources, String updated) {
        this.resources = resources;
        this.updated = updated;
    }

    public ResourcesInfo getResources() {
        return resources;
    }

    public String getUpdated() {
        return updated;
    }
}
