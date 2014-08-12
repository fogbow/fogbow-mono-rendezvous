package org.fogbowcloud.rendezvous.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.fogbowcloud.rendezvous.core.model.DateUtils;
import org.fogbowcloud.rendezvous.xmpp.util.RSMElement;

public class RendezvousItem extends RSMElement {
	
    private static final String ISO_8601_DATE_FORMAT_STR = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final SimpleDateFormat ISO_8601_DATE_FORMAT = new SimpleDateFormat(
    		ISO_8601_DATE_FORMAT_STR, Locale.ROOT);
    static {
    	ISO_8601_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
    private long lastTime;
    private ResourcesInfo resourcesInfo;

    public RendezvousItem(ResourcesInfo resourcesInfo) {
        if (resourcesInfo == null) {
            throw new IllegalArgumentException();
        }
        lastTime = new DateUtils().currentTimeMillis();
        this.resourcesInfo = resourcesInfo;
    }

    public ResourcesInfo getResourcesInfo() {
        return resourcesInfo;
    }

    public long getLastTime() {
        return lastTime;
    }

    public String getFormattedTime() {
        return ISO_8601_DATE_FORMAT.format(new Date(lastTime));
    }
    
    
    /**
     * This method was implemented just for unit test.
     *  
     * @param lastTime
     */
    public void setLastTime(long lastTime){
    	this.lastTime = lastTime;
    }

	@Override
	public String getId() {
		return this.resourcesInfo.getId();
	}

}
