package org.fogbowcloud.rendezvous.core;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.fogbowcloud.rendezvous.core.RendezvousItem;
import org.fogbowcloud.rendezvous.core.ResourcesInfo;
import org.fogbowcloud.rendezvous.core.model.Flavor;
import org.junit.Assert;
import org.junit.Test;

public class RendezvousItemTest {

	@Test(expected = IllegalArgumentException.class)
	public void testNotNullResource() {
		new RendezvousItem(null);
	}

	@Test
	public void testLastTime() throws InterruptedException {
		Date before = new Date(System.currentTimeMillis());
		Thread.sleep(1);
		List<Flavor> flavors = new LinkedList<Flavor>();
		flavors.add(new Flavor("small", "cpu", "mem", 2));
		RendezvousItem item = new RendezvousItem(new ResourcesInfo("id",
				"value1", "value2", "value3", "value4", "value5", "value6", flavors, "cert"));
		Thread.sleep(1);
		Date after = new Date(System.currentTimeMillis());

		Date lastTime = new Date(item.getLastTime());

		Assert.assertTrue(lastTime.after(before));
		Assert.assertTrue(lastTime.before(after));
	}

	@Test
	public void testFormattedDateNotNull() {
		List<Flavor> flavors = new LinkedList<Flavor>();
		flavors.add(new Flavor("small", "cpu", "mem", 2));
		RendezvousItem item = new RendezvousItem(new ResourcesInfo("id",
				"value1", "value2", "value3", "value4", "value5", "value6", flavors, "cert"));

		Assert.assertNotNull(item.getFormattedTime());
	}

}
