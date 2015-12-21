package org.fogbowcloud.rendezvous.core;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class RendezvousItemTest {


	@Test
	public void testLastTime() throws InterruptedException {
		Date before = new Date(System.currentTimeMillis());
		Thread.sleep(1);
//		List<Flavor> flavors = new LinkedList<Flavor>();
//		flavors.add(new Flavor("small", "cpu", "mem", 2));
		RendezvousItem item = new RendezvousItem("id", "cert");
		Thread.sleep(1);
		Date after = new Date(System.currentTimeMillis());

		Date lastTime = new Date(item.getLastTime());

		Assert.assertTrue(lastTime.after(before));
		Assert.assertTrue(lastTime.before(after));
	}

	@Test
	public void testFormattedDateNotNull() {
//		List<Flavor> flavors = new LinkedList<Flavor>();
//		flavors.add(new Flavor("small", "cpu", "mem", 2));
		RendezvousItem item = new RendezvousItem("id", "cert");

		Assert.assertNotNull(item.getFormattedTime());
	}

}
