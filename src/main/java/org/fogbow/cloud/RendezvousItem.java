package org.fogbow.cloud;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RendezvousItem {
    private long lastTime;
    private ResourcesInfo resourcesInfo;

    public RendezvousItem(ResourcesInfo resourcesInfo) {
        if (resourcesInfo == null) {
            throw new IllegalArgumentException();
        }
        lastTime = System.currentTimeMillis();
        this.resourcesInfo = resourcesInfo;
    }

    public ResourcesInfo getResourcesInfo() {
        return resourcesInfo;
    }

    public long getLastTime() {
        return lastTime;
    }

    public String getFormattedTime() {
        SimpleDateFormat dateFormatISO8601 = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ROOT);
        Date date = new Date(lastTime);

        return dateFormatISO8601.format(date);
    }
}
