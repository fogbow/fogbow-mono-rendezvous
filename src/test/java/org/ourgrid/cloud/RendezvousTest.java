package org.ourgrid.cloud;

import java.util.List;
import java.util.concurrent.Semaphore;

import org.junit.Assert;
import org.junit.Test;
import org.ourgid.cloud.Rendezvous;
import org.ourgid.cloud.RendezvousImpl;
import org.ourgid.cloud.RendezvousItem;

public class RendezvousTest {

	private final static int TIMEOUT = 10000;
	private final static int TIMEOUT_GRACE = 500;

	@Test(expected = IllegalArgumentException.class)
	public void testRendezvousConstructor() {
		new RendezvousImpl(-100);
	}

	@Test
	public void testImAliveSingleElement() {
		Rendezvous r = new RendezvousImpl();
		r.iAmAlive("abc");
		List<String> element = r.whoIsAlive();
		Assert.assertEquals(1, element.size());
		Assert.assertEquals("abc", element.get(0));
	}

	@Test
	public void testImAliveManyElements() {
		Rendezvous r = new RendezvousImpl();
		for (int i = 0; i < 10; i++) {
			String s = "Element " + i;
			r.iAmAlive(s);
			List<String> elementList = r.whoIsAlive();
			Assert.assertTrue(elementList.contains(s));
		}
	}

	@Test
	public void testContainsSameIDs() {
		Rendezvous r = new RendezvousImpl();
		r.iAmAlive("id");
		r.iAmAlive("id");
		List<String> elementList = r.whoIsAlive();
		Assert.assertEquals(1, elementList.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testImAliveNullParameter() {
		Rendezvous r = new RendezvousImpl();
		r.iAmAlive(null);
	}

	@Test
	public void testWhoIsAliveEmpty() {
		Rendezvous r = new RendezvousImpl();
		List<String> elementList = r.whoIsAlive();
		Assert.assertEquals(0, elementList.size());

	}

	@Test
	public void testWhoIsAliveSingleElement() {
		Rendezvous r = new RendezvousImpl();
		r.iAmAlive("OnlyElement");
		Assert.assertEquals(1, r.whoIsAlive().size());
		Assert.assertTrue(r.whoIsAlive().contains("OnlyElement"));
	}

	@Test
	public void testWhoIsAliveManyElements() {
		Rendezvous r = new RendezvousImpl();
		for (int i = 0; i < 10; i++) {
			r.iAmAlive("Element" + (i + 1));
		}

		for (int i = 0; i < 10; i++) {
			Assert.assertTrue(r.whoIsAlive().contains("Element" + (i + 1)));
		}
	}

	@Test
	public void testWhoisAliveAfterTime() throws InterruptedException {
		Rendezvous r = new RendezvousImpl(TIMEOUT);
		r.iAmAlive("id");
		Thread.sleep(TIMEOUT + TIMEOUT_GRACE);
		Assert.assertEquals(0, r.whoIsAlive().size());
	}

	@Test
	public void testWhoisAliveAfterTimeManyElements()
			throws InterruptedException {
		Rendezvous r = new RendezvousImpl(TIMEOUT);
		r.iAmAlive("id");
		Assert.assertEquals(1, r.whoIsAlive().size());
		Thread.sleep(TIMEOUT / 2 + TIMEOUT_GRACE);
		Assert.assertEquals(1, r.whoIsAlive().size());
		r.iAmAlive("id2");
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
			r.iAmAlive("Element" + i);
			Thread.sleep(100);
			Assert.assertEquals(i + 1, r.whoIsAlive().size());
		}
		for (int i = 0; i < 10; i++) {
			r.iAmAlive("Element" + i);
			Thread.sleep(100);
			Assert.assertEquals(10, r.whoIsAlive().size());
		}
		Assert.assertEquals(10, r.whoIsAlive().size());
		Thread.sleep(TIMEOUT + TIMEOUT_GRACE);
		Assert.assertEquals(0, r.whoIsAlive().size());
	}

	@Test
	public void testDuplicateIsItAlive() throws InterruptedException {
		Rendezvous r = new RendezvousImpl(TIMEOUT);
		r.iAmAlive("Element");
		r.iAmAlive("Element");
		Thread.sleep(TIMEOUT + TIMEOUT_GRACE);
		r.iAmAlive("Element2");
		Thread.sleep(TIMEOUT_GRACE);
	}

	@Test
	public void testConcourrency2() throws InterruptedException {
		Semaphore semaphoreA = new Semaphore(0);
		Semaphore semaphoreB = new Semaphore(0);
		PausableHashMap<String, RendezvousItem> aliveIDs = new PausableHashMap<String, RendezvousItem>(
				semaphoreA, semaphoreB);
		Rendezvous r = new RendezvousImpl(TIMEOUT, aliveIDs);
		r.iAmAlive("123");
		semaphoreB.acquire();
		r.iAmAlive("321");
		semaphoreA.release();
		Assert.assertFalse(r.getIserror());
	}
}
