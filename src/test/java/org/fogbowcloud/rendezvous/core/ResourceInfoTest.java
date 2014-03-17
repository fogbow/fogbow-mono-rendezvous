package org.fogbowcloud.rendezvous.core;

import org.fogbowcloud.rendezvous.core.ResourcesInfo;
import org.junit.Assert;
import org.junit.Test;

public class ResourceInfoTest {

    @Test(expected=IllegalArgumentException.class)
    public void testNullId() {
        new ResourcesInfo(null, "value1", "value2", "value3", "value4");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testEmptyId() {
        new ResourcesInfo("", "value1", "value2", "value3", "value4");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullCpuIdle() {
        new ResourcesInfo("id", null, "value2", "value3", "value4");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullCpuInUse() {
        new ResourcesInfo("id", "value1", null, "value3", "value4");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullMemIdle() {
        new ResourcesInfo("id", "value1", "value2", null, "value4");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullMemInUse() {
        new ResourcesInfo("id", "value1", "value2", "value3", null);
    }
    
    @Test
    public void testValidValues(){
        ResourcesInfo resources = new ResourcesInfo("id", "value1", "value2", "value3", "value4");
        Assert.assertEquals("id", resources.getId());
        Assert.assertEquals("value1", resources.getCpuIdle());
        Assert.assertEquals("value2", resources.getCpuInUse());
        Assert.assertEquals("value3", resources.getMemIdle());
        Assert.assertEquals("value4", resources.getMemInUse());
        
    }
}
