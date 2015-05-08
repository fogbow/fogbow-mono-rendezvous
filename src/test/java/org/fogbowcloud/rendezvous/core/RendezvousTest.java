package org.fogbowcloud.rendezvous.core;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

import org.fogbowcloud.rendezvous.core.model.DateUtils;
import org.fogbowcloud.rendezvous.core.model.Flavor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class RendezvousTest {

	private final static int TIMEOUT = 1000;
	private final static int TIMEOUT_GRACE = 500;
	private ResourcesInfo resources;
	private List<Flavor> flavors;
	private String[] neighbors;
	ScheduledExecutorService executor;
	Properties properties;
	private static final String PROP_EXPIRATION = "site_expiration";
	RendezvousImpl rendezvous;

	@Before
	public void set() {
		flavors = new LinkedList<Flavor>();
		flavors.add(new Flavor("small", "cpu", "mem", 2));
		resources = new ResourcesInfo("abc", "value1", "value2", "value3",
				"value4", "value5", "value6", flavors, "cert");
		neighbors = new String[] {};
		executor = Mockito.mock(ScheduledExecutorService.class);
		properties = Mockito.mock(Properties.class);
		Mockito.doReturn("").when(properties)
				.getProperty(RendezvousTestHelper.PROP_EXPIRATION);
		Mockito.doReturn("").when(properties)
				.getProperty(RendezvousTestHelper.PROP_NEIGHBORS);
		Mockito.doReturn("")
				.when(properties)
				.getProperty(
						RendezvousTestHelper.PROP_MAX_WHOISALIVE_MANAGER_COUNT);
		Mockito.doReturn("")
				.when(properties)
				.getProperty(
						RendezvousTestHelper.PROP_MAX_WHOISALIVESYNC_MANAGER_COUNT);
		Mockito.doReturn("")
				.when(properties)
				.getProperty(
						RendezvousTestHelper.PROP_MAX_WHOISALIVESYNC_NEIGHBOR_COUNT);
		rendezvous = new RendezvousImpl(null, properties, executor);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testRendezvousConstructor() {
		// timeout -100
		Mockito.when(properties.getProperty(PROP_EXPIRATION)).thenReturn("-100");
		new RendezvousImpl(null, properties, executor);
	}

	@Test
	public void testImAliveSingleElement() {

		List<Flavor> flavors = new LinkedList<Flavor>();
		flavors.add(new Flavor("small", "cpu", "mem", 2));
		rendezvous.iAmAlive(resources);
		List<RendezvousItem> element = rendezvous.whoIsAlive();
		Assert.assertEquals(1, element.size());
		Assert.assertEquals("abc", element.get(0).getResourcesInfo().getId());
	}

	@Test
	public void testImAliveManyElements() {
		for (int i = 0; i < 10; i++) {
			String s = "Element " + i;
			resources = new ResourcesInfo(s, "value1", "value2", "value3",
					"value4", "value5", "value6", flavors, "cert");
			rendezvous.iAmAlive(resources);
			List<RendezvousItem> elementList = rendezvous.whoIsAlive();
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
		rendezvous.iAmAlive(resources);
		rendezvous.iAmAlive(resources);
		List<RendezvousItem> elementList = rendezvous.whoIsAlive();
		Assert.assertEquals(1, elementList.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testImAliveNullParameter() {
		ResourcesInfo resources = new ResourcesInfo(null, "value1", "value2",
				"value3", "value4", "value5", "value6", flavors, "cert");
		rendezvous.iAmAlive(resources);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testImAliveEmptyParameter() {
		ResourcesInfo resources = new ResourcesInfo("", "value1", "value2",
				"value3", "value4", "value5", "value6", flavors, "cert");
		rendezvous.iAmAlive(resources);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIAmAliveNullResourceInfo() {
		rendezvous.iAmAlive(null);
	}

	@Test
	public void testWhoIsAliveEmpty() {
		List<RendezvousItem> elementList = rendezvous.whoIsAlive();
		Assert.assertEquals(0, elementList.size());

	}

	@Test
	public void testWhoIsAliveSingleElement() {
		ResourcesInfo resources = new ResourcesInfo("OnlyElement", "value1",
				"value2", "value3", "value4", "value5", "value6", flavors, "cert");
		rendezvous.iAmAlive(resources);
		Assert.assertEquals(1, rendezvous.whoIsAlive().size());
		Assert.assertTrue(containsId(rendezvous.whoIsAlive(), "OnlyElement"));
	}

	@Test
	public void testWhoIsAliveElementValues() throws InterruptedException {
		ResourcesInfo resources = new ResourcesInfo("id", "value1", "value2",
				"value3", "value4", "value5", "value6", flavors, "cert");

		Date beforeMessage = new Date(System.currentTimeMillis());
		Thread.sleep(1);
		rendezvous.iAmAlive(resources);
		Thread.sleep(1);
		Date afterMessage = new Date(System.currentTimeMillis());

		Assert.assertEquals(1, rendezvous.whoIsAlive().size());
		Assert.assertTrue(containsId(rendezvous.whoIsAlive(), "id"));
		RendezvousItem item = rendezvous.whoIsAlive().get(0);
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
		rendezvous.iAmAlive(resources);

		Assert.assertEquals(1, rendezvous.whoIsAlive().size());
		Assert.assertTrue(containsId(rendezvous.whoIsAlive(), "abc"));
		RendezvousItem item = rendezvous.whoIsAlive().get(0);

		Assert.assertNotNull(item.getFormattedTime());
	}

	@Test
	public void testWhoIsAliveManyElements() {
		for (int i = 0; i < 10; i++) {
			rendezvous.iAmAlive(new ResourcesInfo("Element" + (i + 1),
					"value1", "value2", "value3", "value4", "value5", "value6", flavors, "cert"));
		}

		for (int i = 0; i < 10; i++) {
			Assert.assertTrue(containsId(rendezvous.whoIsAlive(), "Element"
					+ (i + 1)));
		}
	}

	@Test
	public void testWhoIsAliveAfterTime() throws InterruptedException {
		Mockito.when(properties.getProperty(PROP_EXPIRATION)).thenReturn(TIMEOUT + "");
		rendezvous = new RendezvousImpl(null, properties, executor);

		rendezvous.iAmAlive(resources);

		Assert.assertEquals(1, rendezvous.whoIsAlive().size());

		// mocking data
		DateUtils dateMock = Mockito.mock(DateUtils.class);
		long timeAfterTimeout = new DateUtils().currentTimeMillis() + TIMEOUT
				+ TIMEOUT_GRACE;
		Mockito.when(dateMock.currentTimeMillis()).thenReturn(timeAfterTimeout);
		rendezvous.setDateUnit(dateMock);

		// checking expired alive ids
		rendezvous.checkExpiredAliveIDs();

		Assert.assertEquals(0, rendezvous.whoIsAlive().size());
	}

	@Test
	public void testWhoIsAliveAfterTimeManyElements()
			throws InterruptedException {
		Mockito.when(properties.getProperty(PROP_EXPIRATION)).thenReturn(TIMEOUT + "");
		rendezvous = new RendezvousImpl(null, properties, executor);
		
		rendezvous.iAmAlive(resources);
		Assert.assertEquals(1, rendezvous.whoIsAlive().size());

		// mocking data
		long currentTime = new DateUtils().currentTimeMillis();
		long halfTimeoutInterval = TIMEOUT / 2 + TIMEOUT_GRACE - 10;
		long firstPassageOfTime = currentTime + halfTimeoutInterval;
		long secondPassageOfTime = firstPassageOfTime + halfTimeoutInterval;
		long thirdPassageOfTime = secondPassageOfTime + halfTimeoutInterval;

		DateUtils dateMock = Mockito.mock(DateUtils.class);
		Mockito.when(dateMock.currentTimeMillis()).thenReturn(firstPassageOfTime,
				firstPassageOfTime, secondPassageOfTime, secondPassageOfTime, secondPassageOfTime,
				thirdPassageOfTime);
		rendezvous.setDateUnit(dateMock);

		// first passage of time

		Assert.assertEquals(1, rendezvous.whoIsAlive().size());

		// IAmAlive of new id
		ResourcesInfo resources2 = new ResourcesInfo("id2", "value1", "value2",
				"value3", "value4", "value5", "value6", flavors, "cert");
		rendezvous.iAmAlive(resources2);
		rendezvous.setLastTime("id2", firstPassageOfTime + 3);

		Assert.assertEquals(2, rendezvous.whoIsAlive().size());

		// second passage of time

		Assert.assertEquals(1, rendezvous.whoIsAlive().size());

		// third passage of time

		Assert.assertEquals(0, rendezvous.whoIsAlive().size());
	}

	@Test
	public void testConcurrentIAmAlive() throws InterruptedException {
		Mockito.when(properties.getProperty(PROP_EXPIRATION)).thenReturn(TIMEOUT + "");
		rendezvous = new RendezvousImpl(null, properties, executor);
		
		long shortInterval = TIMEOUT / 20;

		long currentTime = new DateUtils().currentTimeMillis();

		DateUtils dateMock;

		int numberOfClients = 10;
		for (int i = 0; i < numberOfClients; i++) {
			// simulating short passage of time
			currentTime += shortInterval + 3;

			// IAmAlive of new id
			String id = "Element" + i;
			rendezvous.iAmAlive(new ResourcesInfo(id, "value1", "value2",
					"value3", "value4", "value5", "value6", flavors, "cert"));
			rendezvous.setLastTime(id, currentTime);

			// mocking date to check expired
			dateMock = Mockito.mock(DateUtils.class);
			Mockito.when(dateMock.currentTimeMillis())
					.thenReturn(++currentTime);
			rendezvous.setDateUnit(dateMock);

			// checking expired alive ids
			rendezvous.checkExpiredAliveIDs();

			// assert new id was added
			Assert.assertEquals(i + 1, rendezvous.whoIsAlive().size());
		}

		for (int i = 0; i < numberOfClients; i++) {
			// simulating short passage of time
			currentTime += shortInterval + 3;

			// IAmAlive client from already existing client
			String id = "Element" + i;
			rendezvous.iAmAlive(new ResourcesInfo(id, "value1", "value2",
					"value3", "value4", "value5", "value6", flavors, "cert"));
			((RendezvousImpl) rendezvous).setLastTime(id, currentTime);

			// mocking date to check expired
			dateMock = Mockito.mock(DateUtils.class);
			Mockito.when(dateMock.currentTimeMillis())
					.thenReturn(++currentTime);
			rendezvous.setDateUnit(dateMock);

			// checking expired alive ids
			rendezvous.checkExpiredAliveIDs();

			// assert all ids were alive
			Assert.assertEquals(numberOfClients, rendezvous.whoIsAlive().size());
		}

		Assert.assertEquals(numberOfClients, rendezvous.whoIsAlive().size());

		// simulating passage of time bigger than timeout
		long bigInterval = TIMEOUT + TIMEOUT_GRACE;
		currentTime += bigInterval;

		Long firstReturn = ++currentTime;

		// mocking date to check expired
		dateMock = Mockito.mock(DateUtils.class);
		Mockito.when(dateMock.currentTimeMillis()).thenReturn(firstReturn);
		rendezvous.setDateUnit(dateMock);

		// checking expired alive ids
		rendezvous.checkExpiredAliveIDs();

		// assert there was not ids alive
		Assert.assertEquals(0, rendezvous.whoIsAlive().size());
	}

	@Test
	public void testConcourrency() throws InterruptedException {
		Mockito.when(properties.getProperty(PROP_EXPIRATION)).thenReturn(TIMEOUT + "");
		rendezvous = new RendezvousImpl(null, properties, executor);
		
		for (int i = 0; i < 1000000; i++) {
			rendezvous.iAmAlive(new ResourcesInfo("Element" + i, "value1",
					"value2", "value3", "value4", "value5", "value6", flavors, "cert"));
		}

		Assert.assertFalse(rendezvous.getInError());
	}
}
