package org.fogbowcloud.rendezvous.core;

import java.util.Date;
import java.util.List;

import org.fogbowcloud.rendezvous.core.Rendezvous;
import org.fogbowcloud.rendezvous.core.RendezvousImpl;
import org.fogbowcloud.rendezvous.core.RendezvousItem;
import org.fogbowcloud.rendezvous.core.ResourcesInfo;
import org.junit.Assert;
import org.junit.Test;

public class RendezvousTest {

    private final static int TIMEOUT = 1000;
    private final static int TIMEOUT_GRACE = 500;

    @Test(expected = IllegalArgumentException.class)
    public void testRendezvousConstructor() {
        new RendezvousImpl(-100);
    }

    @Test
    public void testImAliveSingleElement() {
        Rendezvous r = new RendezvousImpl();

        ResourcesInfo resources = new ResourcesInfo("abc", "value1", "value2",
                "value3", "value4");

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
            ResourcesInfo resources = new ResourcesInfo(s, "value1", "value2",
                    "value3", "value4");

            r.iAmAlive(resources);
            List<RendezvousItem> elementList = r.whoIsAlive();
            Assert.assertTrue(containsId(elementList, s));
        }
    }

    private boolean containsId(List<RendezvousItem> elementList, String id) {
        for (RendezvousItem item : elementList) {
            if (item.getResourcesInfo().getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testContainsSameIDs() {
        Rendezvous r = new RendezvousImpl();
        ResourcesInfo resources = new ResourcesInfo("id", "value1", "value2",
                "value3", "value4");
        r.iAmAlive(resources);
        r.iAmAlive(resources);
        List<RendezvousItem> elementList = r.whoIsAlive();
        Assert.assertEquals(1, elementList.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testImAliveNullParameter() {
        Rendezvous r = new RendezvousImpl();
        ResourcesInfo resources = new ResourcesInfo(null, "value1", "value2",
                "value3", "value4");
        r.iAmAlive(resources);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testImAliveEmptyParameter() {
        Rendezvous r = new RendezvousImpl();
        ResourcesInfo resources = new ResourcesInfo("", "value1", "value2",
                "value3", "value4");
        r.iAmAlive(resources);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIAmAliveNullResourceInfo() {
        Rendezvous r = new RendezvousImpl();
        r.iAmAlive(null);
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
        ResourcesInfo resources = new ResourcesInfo("OnlyElement", "value1",
                "value2", "value3", "value4");
        r.iAmAlive(resources);
        Assert.assertEquals(1, r.whoIsAlive().size());
        Assert.assertTrue(containsId(r.whoIsAlive(), "OnlyElement"));
    }

    @Test
    public void testWhoIsAliveElementValues() throws InterruptedException {
        Rendezvous r = new RendezvousImpl();
        ResourcesInfo resources = new ResourcesInfo("id", "value1", "value2",
                "value3", "value4");

        Date beforeMessage = new Date(System.currentTimeMillis());
        Thread.sleep(1);
        r.iAmAlive(resources);
        Thread.sleep(1);
        Date afterMessage = new Date(System.currentTimeMillis());

        Assert.assertEquals(1, r.whoIsAlive().size());
        Assert.assertTrue(containsId(r.whoIsAlive(), "id"));
        RendezvousItem item = r.whoIsAlive().get(0);
        ResourcesInfo returnedResource = item.getResourcesInfo();
        Assert.assertEquals("value1", returnedResource.getCpuIdle());
        Assert.assertEquals("value2", returnedResource.getCpuInUse());
        Assert.assertEquals("value3", returnedResource.getMemIdle());
        Assert.assertEquals("value4", returnedResource.getMemInUse());

        Date updated = new Date(item.getLastTime());

        Assert.assertTrue(updated.after(beforeMessage));
        Assert.assertTrue(updated.before(afterMessage));
        Assert.assertNotNull(item.getFormattedTime());
    }

    @Test
    public void testWhoIsAliveElementUpdatedValueNotNull() {
        Rendezvous r = new RendezvousImpl();
        ResourcesInfo resources = new ResourcesInfo("id", "value1", "value2",
                "value3", "value4");

        r.iAmAlive(resources);

        Assert.assertEquals(1, r.whoIsAlive().size());
        Assert.assertTrue(containsId(r.whoIsAlive(), "id"));
        RendezvousItem item = r.whoIsAlive().get(0);

        Assert.assertNotNull(item.getFormattedTime());
    }

    @Test
    public void testWhoIsAliveManyElements() {
        Rendezvous r = new RendezvousImpl();
        for (int i = 0; i < 10; i++) {
            r.iAmAlive(new ResourcesInfo("Element" + (i + 1), "value1",
                    "value2", "value3", "value4"));
        }

        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(containsId(r.whoIsAlive(), "Element" + (i + 1)));
        }
    }

    @Test
    public void testWhoIsAliveAfterTime() throws InterruptedException {
        Rendezvous r = new RendezvousImpl(TIMEOUT);
        ResourcesInfo resources = new ResourcesInfo("id", "value1", "value2",
                "value3", "value4");
        r.iAmAlive(resources);
        Assert.assertEquals(1, r.whoIsAlive().size());
        Thread.sleep(TIMEOUT + TIMEOUT_GRACE);
        Assert.assertEquals(0, r.whoIsAlive().size());
    }

    @Test
    public void testWhoIsAliveAfterTimeManyElements()
            throws InterruptedException {
        Rendezvous r = new RendezvousImpl(TIMEOUT);
        ResourcesInfo resources = new ResourcesInfo("id", "value1", "value2",
                "value3", "value4");
        r.iAmAlive(resources);
        Assert.assertEquals(1, r.whoIsAlive().size());
        Thread.sleep(TIMEOUT / 2 + TIMEOUT_GRACE);
        Assert.assertEquals(1, r.whoIsAlive().size());
        ResourcesInfo resources2 = new ResourcesInfo("id2", "value1", "value2",
                "value3", "value4");
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
            r.iAmAlive(new ResourcesInfo("Element" + i, "value1", "value2",
                    "value3", "value4"));
            Thread.sleep(TIMEOUT / 20);
            Assert.assertEquals(i + 1, r.whoIsAlive().size());
        }
        for (int i = 0; i < 10; i++) {
            r.iAmAlive(new ResourcesInfo("Element" + i, "value1", "value2",
                    "value3", "value4"));
            Thread.sleep(TIMEOUT / 20);
            Assert.assertEquals(10, r.whoIsAlive().size());
        }
        Assert.assertEquals(10, r.whoIsAlive().size());
        Thread.sleep(TIMEOUT + TIMEOUT_GRACE);
        Assert.assertEquals(0, r.whoIsAlive().size());
    }

    @Test
    public void testConcourrency() throws InterruptedException {
        RendezvousImpl r = new RendezvousImpl(TIMEOUT);

        for (int i = 0; i < 1000000; i++) {
            r.iAmAlive(new ResourcesInfo("Element" + i, "value1", "value2",
                    "value3", "value4"));
        }

        Assert.assertFalse(r.getInError());
    }
}
