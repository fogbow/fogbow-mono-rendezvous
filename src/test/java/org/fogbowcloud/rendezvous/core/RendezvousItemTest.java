package org.fogbowcloud.rendezvous.core;

import java.util.Date;

import org.fogbowcloud.rendezvous.core.RendezvousItem;
import org.fogbowcloud.rendezvous.core.ResourcesInfo;
import org.junit.Assert;
import org.junit.Test;

public class RendezvousItemTest {

    @Test(expected=IllegalArgumentException.class)
    public void testNotNullResource() {
        new RendezvousItem(null);
    }
    
    @Test
    public void testLastTime() throws InterruptedException {
        Date before = new Date(System.currentTimeMillis());
        Thread.sleep(1);
        RendezvousItem item = new RendezvousItem(new ResourcesInfo("id","value1", "value2", "value3", "value4"));
        Thread.sleep(1);
        Date after = new Date(System.currentTimeMillis());

        Date lastTime = new Date(item.getLastTime());
        
        Assert.assertTrue(lastTime.after(before));
        Assert.assertTrue(lastTime.before(after));
    }
    
    @Test
    public void testFormattedDateNotNull(){
        RendezvousItem item = new RendezvousItem(new ResourcesInfo("id","value1", "value2", "value3", "value4"));
        Assert.assertNotNull(item.getFormattedTime());
    }
    
}
