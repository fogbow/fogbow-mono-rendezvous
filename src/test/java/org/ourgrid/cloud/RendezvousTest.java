package org.ourgrid.cloud;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class RendezvousTest {

    private final static int TIMEOUT = 10000;
    private final static int TIMEOUT_GRACE = 500;
//    private ResourcesInfo DEFAULT_RESOURCES = new ResourcesInfo("id", "value1", "value2", "value3", "value4");

    @Test(expected = IllegalArgumentException.class)
    public void testRendezvousConstructor() {
        new RendezvousImpl(-100);
    }

    
    
    @Test
    public void testImAliveSingleElement() {
        Rendezvous r = new RendezvousImpl();
        
        ResourcesInfo resources = new ResourcesInfo("abc", "value1", "value2", "value3", "value4");
        
        r.iAmAlive(resources);
        List<RendezvousItem> element = r.whoIsAlive();
        Assert.assertEquals(1, element.size());
        Assert.assertEquals("abc", element.get(0).getResourcesInfo().getId());
    }

    @Test
    public void testImAliveManyElements() {
        Rendezvous r = new RendezvousImpl();
        for (int i = 0; i < 10; i++) {
            String s = "Element " + i;
            ResourcesInfo resources = new ResourcesInfo(s, "value1", "value2", "value3", "value4");

            r.iAmAlive(resources);
            List<RendezvousItem> elementList = r.whoIsAlive();
            Assert.assertTrue(containsId(elementList, s));
        }
    }

    private boolean containsId(List<RendezvousItem> elementList, String id) {
        
        for (RendezvousItem item : elementList){        
            if (item.getResourcesInfo().getId().equals(id)){
              return true;  
            }
        }
        return false;
    }



    @Test
    public void testContainsSameIDs() {
        Rendezvous r = new RendezvousImpl();
        ResourcesInfo resources = new ResourcesInfo("id", "value1", "value2", "value3", "value4");
        r.iAmAlive(resources);
        r.iAmAlive(resources);
        List<RendezvousItem> elementList = r.whoIsAlive();
        Assert.assertEquals(1, elementList.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testImAliveNullParameter() {
        Rendezvous r = new RendezvousImpl();
        ResourcesInfo resources = new ResourcesInfo(null, "value1", "value2", "value3", "value4");
        r.iAmAlive(resources);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testImAliveEmptyParameter() {
        Rendezvous r = new RendezvousImpl();
        ResourcesInfo resources = new ResourcesInfo("", "value1", "value2", "value3", "value4");
        r.iAmAlive(resources);
    }

    @Test
    public void testWhoIsAliveEmpty() {
        Rendezvous r = new RendezvousImpl();
        List<RendezvousItem> elementList = r.whoIsAlive();
        Assert.assertEquals(0, elementList.size());

    }

    @Test
    public void testWhoIsAliveSingleElement() {
        Rendezvous r = new RendezvousImpl();
        ResourcesInfo resources = new ResourcesInfo("OnlyElement", "value1", "value2", "value3", "value4");
        r.iAmAlive(resources);
        Assert.assertEquals(1, r.whoIsAlive().size());
        Assert.assertTrue(containsId(r.whoIsAlive(), "OnlyElement"));
    }

    @Test
    public void testWhoIsAliveManyElements() {
        Rendezvous r = new RendezvousImpl();
        for (int i = 0; i < 10; i++) {
            r.iAmAlive(new ResourcesInfo("Element" + (i + 1), "value1", "value2", "value3", "value4"));
        }

        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(containsId(r.whoIsAlive(), "Element" + (i + 1)));
        }
    }

    @Test
    public void testWhoIsAliveAfterTime() throws InterruptedException {
        Rendezvous r = new RendezvousImpl(TIMEOUT);
        ResourcesInfo resources = new ResourcesInfo("id", "value1", "value2", "value3", "value4");
        r.iAmAlive( resources);
        Assert.assertEquals(1, r.whoIsAlive().size());
        Thread.sleep(TIMEOUT + TIMEOUT_GRACE);
        Assert.assertEquals(0, r.whoIsAlive().size());
    }

    @Test
    public void testWhoIsAliveAfterTimeManyElements()
            throws InterruptedException {
        Rendezvous r = new RendezvousImpl(TIMEOUT);
        ResourcesInfo resources = new ResourcesInfo("id", "value1", "value2", "value3", "value4");
        r.iAmAlive(resources);
        Assert.assertEquals(1, r.whoIsAlive().size());
        Thread.sleep(TIMEOUT / 2 + TIMEOUT_GRACE);
        Assert.assertEquals(1, r.whoIsAlive().size());
        ResourcesInfo resources2 = new ResourcesInfo("id2", "value1", "value2", "value3", "value4");
        r.iAmAlive(resources2);
        Assert.assertEquals(2, r.whoIsAlive().size());
        Thread.sleep(TIMEOUT / 2 + TIMEOUT_GRACE);
        Assert.assertEquals(1, r.whoIsAlive().size());
        Thread.sleep(TIMEOUT / 2 + TIMEOUT_GRACE);
        Assert.assertEquals(0, r.whoIsAlive().size());
    }

    @Test
    public void testConcurrentIAmAlive() throws InterruptedException {
        Rendezvous r = new RendezvousImpl(TIMEOUT);
        for (int i = 0; i < 10; i++) {
            r.iAmAlive( new ResourcesInfo("Element" + i, "value1", "value2", "value3", "value4"));
            Thread.sleep(100);
            Assert.assertEquals(i + 1, r.whoIsAlive().size());
        }
        for (int i = 0; i < 10; i++) {
            r.iAmAlive(new ResourcesInfo("Element" + i, "value1", "value2", "value3", "value4"));
            Thread.sleep(100);
            Assert.assertEquals(10, r.whoIsAlive().size());
        }
        Assert.assertEquals(10, r.whoIsAlive().size());
        Thread.sleep(TIMEOUT + TIMEOUT_GRACE);
        Assert.assertEquals(0, r.whoIsAlive().size());
    }

    //TODO I don't understand this test! Where is the assert?
    @Test    
    public void testDuplicateIsItAlive() throws InterruptedException {
        Rendezvous r = new RendezvousImpl(TIMEOUT);
        ResourcesInfo resources = new ResourcesInfo("Element", "value1", "value2", "value3", "value4");
        r.iAmAlive(resources);
        r.iAmAlive(resources);
        Thread.sleep(TIMEOUT + TIMEOUT_GRACE);
        ResourcesInfo resources2 = new ResourcesInfo("Element1", "value1", "value2", "value3", "value4");
        r.iAmAlive(resources2);
        Thread.sleep(TIMEOUT_GRACE);
    }

    //TODO I don't understand this test!
    @Test
    public void testConcourrency3() throws InterruptedException {
        RendezvousImpl r = new RendezvousImpl(TIMEOUT);
        
        for (int i = 0; i < 1000000; i++) {
            r.iAmAlive(new ResourcesInfo("Element" + i, "value1", "value2", "value3", "value4"));
        }
        
        Assert.assertFalse(r.getInError());
    }
}
