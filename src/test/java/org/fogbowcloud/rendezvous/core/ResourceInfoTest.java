package org.fogbowcloud.rendezvous.core;

import java.util.LinkedList;
import java.util.List;

import org.fogbowcloud.rendezvous.core.ResourcesInfo;
import org.fogbowcloud.rendezvous.core.model.Flavor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ResourceInfoTest {
	
    private List<Flavor> flavors;
    
    @Before 
    public void set() {
    	flavors = new LinkedList<Flavor>();
		flavors.add(new Flavor("small", "cpu", "mem", 2));
  
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullId() {
        new ResourcesInfo(null, "value1", "value2", "value3", "value4", flavors, "cert");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testEmptyId() {
        new ResourcesInfo("", "value1", "value2", "value3", "value4", flavors, "cert");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullCpuIdle() {
        new ResourcesInfo("id", null, "value2", "value3", "value4", flavors, "cert");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullCpuInUse() {
        new ResourcesInfo("id", "value1", null, "value3", "value4", flavors, "cert");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullMemIdle() {
        new ResourcesInfo("id", "value1", "value2", null, "value4", flavors, "cert");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullMemInUse() {
        new ResourcesInfo("id", "value1", "value2", "value3",null, flavors, "cert");
    }
    
    @Test
    public void testValidValues(){
        ResourcesInfo resources = new ResourcesInfo("id", "value1", "value2", "value3", "value4", flavors, "cert");
        Assert.assertEquals("id", resources.getId());
        Assert.assertEquals("value1", resources.getCpuIdle());
        Assert.assertEquals("value2", resources.getCpuInUse());
        Assert.assertEquals("value3", resources.getMemIdle());
        Assert.assertEquals("value4", resources.getMemInUse());
        
    }
}
