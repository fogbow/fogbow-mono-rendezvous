package org.fogbowcloud.rendezvous.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

import org.fogbowcloud.rendezvous.core.model.DateUtils;
import org.fogbowcloud.rendezvous.core.plugins.WhiteListPlugin;
import org.fogbowcloud.rendezvous.xmpp.model.RendezvousResponseItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class RendezvousTest {

	private final static int TIMEOUT = 1000;
	private final static int TIMEOUT_GRACE = 500;
	private RendezvousItem rendezvousItem;
	@SuppressWarnings("unused")
	private String[] neighbors;
	ScheduledExecutorService executor;
	Properties properties;
	private static final String PROP_EXPIRATION = "site_expiration";
	RendezvousImpl rendezvous;

	@Before
	public void set() {
		rendezvousItem = new RendezvousItem("abc", "cert", TIMEOUT);
		
		this.neighbors = new String[] {};
		executor = Mockito.mock(ScheduledExecutorService.class);
		properties = Mockito.mock(Properties.class);
		Mockito.doReturn("").when(properties)
				.getProperty(RendezvousTestHelper.PROP_EXPIRATION);
		Mockito.doReturn("").when(properties)
				.getProperty(RendezvousTestHelper.PROP_NEIGHBORS);
		Mockito.doReturn("").when(properties)
				.getProperty(
						RendezvousTestHelper.PROP_MAX_WHOISALIVE_MANAGER_COUNT);
		Mockito.doReturn("").when(properties)
				.getProperty(
						RendezvousTestHelper.PROP_MAX_WHOISALIVESYNC_MANAGER_COUNT);
		Mockito.doReturn("").when(properties)
				.getProperty(
						RendezvousTestHelper.PROP_MAX_WHOISALIVESYNC_NEIGHBOR_COUNT);
		rendezvous = new RendezvousImpl(null, properties, executor);

	}

	@Test
	public void testImAliveSingleElement() {

		rendezvous.iAmAlive(rendezvousItem);
		List<RendezvousItem> element = rendezvous.whoIsAlive();
		Assert.assertEquals(1, element.size());
		Assert.assertEquals("abc", element.get(0).getMemberId());
	}

	@Test
	public void testImAliveManyElements() {
		for (int i = 0; i < 10; i++) {
			String s = "Element " + i;
			rendezvousItem = new RendezvousItem(s, "cert", TIMEOUT);
			rendezvous.iAmAlive(rendezvousItem);
			List<RendezvousItem> elementList = rendezvous.whoIsAlive();
			Assert.assertTrue(containsId(elementList, s));

		}
	}

	private boolean containsId(List<RendezvousItem> elementList, String id) {
		for (RendezvousItem item : elementList) {
			if (item.getMemberId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void testContainsSameIDs() {
		rendezvous.iAmAlive(rendezvousItem);
		rendezvous.iAmAlive(rendezvousItem);
		List<RendezvousItem> elementList = rendezvous.whoIsAlive();
		Assert.assertEquals(1, elementList.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testImAliveNullParameter() {
		rendezvousItem = new RendezvousItem(null, "cert");
		rendezvous.iAmAlive(rendezvousItem);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testImAliveEmptyParameter() {
		rendezvousItem = new RendezvousItem("", "cert");
		rendezvous.iAmAlive(rendezvousItem);
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
		rendezvousItem = new RendezvousItem("OnlyElement", "cert", TIMEOUT);
		rendezvous.iAmAlive(rendezvousItem);
		Assert.assertEquals(1, rendezvous.whoIsAlive().size());
		Assert.assertTrue(containsId(rendezvous.whoIsAlive(), "OnlyElement"));
	}

	@Test
	public void testWhoIsAliveElementValues() throws InterruptedException {
		Date beforeMessage = new Date(System.currentTimeMillis());
		Thread.sleep(1);
		rendezvous.iAmAlive(rendezvousItem = new RendezvousItem("id", "cert", TIMEOUT));
		Thread.sleep(1);
		Date afterMessage = new Date(System.currentTimeMillis());

		Assert.assertEquals(1, rendezvous.whoIsAlive().size());
		Assert.assertTrue(containsId(rendezvous.whoIsAlive(), "id"));
		RendezvousItem item = rendezvous.whoIsAlive().get(0);

		Date updated = new Date(item.getLastTime());
		Assert.assertTrue(updated.after(beforeMessage));
		Assert.assertTrue(updated.before(afterMessage));
		Assert.assertNotNull(item.getFormattedTime());
	}

	@Test
	public void testWhoIsAliveElementUpdatedValueNotNull() {
		rendezvous.iAmAlive(rendezvousItem);

		Assert.assertEquals(1, rendezvous.whoIsAlive().size());
		Assert.assertTrue(containsId(rendezvous.whoIsAlive(), "abc"));
		RendezvousItem item = rendezvous.whoIsAlive().get(0);

		Assert.assertNotNull(item.getFormattedTime());
	}

	@Test
	public void testWhoIsAliveManyElements() {
		for (int i = 0; i < 10; i++) {		
			rendezvous.iAmAlive(new RendezvousItem("Element" + (i + 1), "cert", TIMEOUT));			
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

		rendezvous.iAmAlive(rendezvousItem);

		Assert.assertEquals(1, rendezvous.whoIsAlive().size());

		// mocking data
		DateUtils dateMock = Mockito.mock(DateUtils.class);
		long timeAfterTimeout = new DateUtils().currentTimeMillis() + (TIMEOUT * RendezvousImpl.DEFAULT_I_AM_ALIVE_MAX_MESSAGE_LOST)
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
		Mockito.when(properties.getProperty(RendezvousImpl.PROP_I_AM_ALIVE_PERIOD))
				.thenReturn(String.valueOf(TIMEOUT));
		Mockito.when(properties.getProperty(RendezvousImpl.PROP_I_AM_ALIVE_MAX_MESSAGE_LOST))
				.thenReturn(String.valueOf(RendezvousTestHelper.DEFAULT_WAIT_FREQUENCY_TIMES));
		rendezvous = new RendezvousImpl(null, properties, executor);
		
		// mocking data
		long currentTime = new DateUtils().currentTimeMillis();
		long firstPassageOfTime = currentTime;
		long secondPassageOfTime = firstPassageOfTime + TIMEOUT * RendezvousTestHelper.DEFAULT_WAIT_FREQUENCY_TIMES;
		
		DateUtils dateMock = Mockito.mock(DateUtils.class);
		Mockito.when(dateMock.currentTimeMillis()).thenReturn(firstPassageOfTime);
		rendezvous.setDateUnit(dateMock);
		
		rendezvous.iAmAlive(rendezvousItem);

		// first passage of time
		Assert.assertEquals(1, rendezvous.whoIsAlive().size());

		// IAmAlive of new id
		RendezvousItem rendezvousItem2 = new RendezvousItem("id2", "cert", TIMEOUT);
		rendezvous.iAmAlive(rendezvousItem2);
		rendezvous.setLastTime("id2", currentTime);

		dateMock = Mockito.mock(DateUtils.class);
		Mockito.when(dateMock.currentTimeMillis()).thenReturn(firstPassageOfTime, firstPassageOfTime);
		rendezvous.setDateUnit(dateMock);
		
		Assert.assertEquals(2, rendezvous.whoIsAlive().size());

		dateMock = Mockito.mock(DateUtils.class);
		Mockito.when(dateMock.currentTimeMillis()).thenReturn(firstPassageOfTime, secondPassageOfTime);
		rendezvous.setDateUnit(dateMock);
		
		// second passage of time
		Assert.assertEquals(1, rendezvous.whoIsAlive().size());

		// third passage of time
		Assert.assertEquals(0, rendezvous.whoIsAlive().size());
	}
	
	@Test
	public void testWhoIsAliveAfterTimeManyElementsTwo()
			throws InterruptedException {
		Mockito.when(properties.getProperty(PROP_EXPIRATION)).thenReturn(String.valueOf(TIMEOUT));
		rendezvous = new RendezvousImpl(null, properties, executor);
		
		// mocking data
		long currentTime = new DateUtils().currentTimeMillis();
		long firstPassageOfTime = currentTime;
		long secondPassageOfTime = firstPassageOfTime + TIMEOUT
				* RendezvousTestHelper.DEFAULT_WAIT_FREQUENCY_TIMES;

		DateUtils dateMock = Mockito.mock(DateUtils.class);
		Mockito.when(dateMock.currentTimeMillis()).thenReturn(firstPassageOfTime,
				secondPassageOfTime);
		rendezvous.setDateUnit(dateMock);

		rendezvous.iAmAlive(rendezvousItem);

		// first passage of time
		Assert.assertEquals(1, rendezvous.whoIsAlive().size());
		Assert.assertEquals(0, rendezvous.whoIsAlive().size());

		// IAmAlive of new id
		RendezvousItem rendezvousItem2 = new RendezvousItem("id2", "cert", TIMEOUT);
		rendezvous.iAmAlive(rendezvousItem2);
		rendezvous.setLastTime("id2", firstPassageOfTime);

		dateMock = Mockito.mock(DateUtils.class);
		Mockito.when(dateMock.currentTimeMillis()).thenReturn(firstPassageOfTime);
		rendezvous.setDateUnit(dateMock);

		Assert.assertEquals(1, rendezvous.whoIsAlive().size());

		dateMock = Mockito.mock(DateUtils.class);
		Mockito.when(dateMock.currentTimeMillis()).thenReturn(firstPassageOfTime,
				firstPassageOfTime * RendezvousTestHelper.DEFAULT_WAIT_FREQUENCY_TIMES);
		rendezvous.setDateUnit(dateMock);

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
			rendezvous.iAmAlive(new RendezvousItem(id, "cert", TIMEOUT));			
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
			rendezvous.iAmAlive(new RendezvousItem(id, "cert", TIMEOUT));
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
		long bigInterval = TIMEOUT * RendezvousImpl.DEFAULT_I_AM_ALIVE_MAX_MESSAGE_LOST + TIMEOUT_GRACE;
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
			rendezvous.iAmAlive(new RendezvousItem("Element" + i, "cert"));
		}

		Assert.assertFalse(rendezvous.getInError());
	}

	@Test
	public void testRendezvousWhiteList() throws InterruptedException {

		Mockito.when(properties.getProperty(PROP_EXPIRATION)).thenReturn(TIMEOUT + "");

		String knownMemberId = "knownMemberId";
		String unKnownMemberId = "unknownMemberId";

		WhiteListPlugin whiteListPlugin = Mockito.mock(WhiteListPlugin.class);

		Mockito.when(whiteListPlugin.contains(knownMemberId)).thenReturn(true);
		Mockito.when(whiteListPlugin.contains(unKnownMemberId)).thenReturn(false);

		rendezvous = new RendezvousImpl(null, properties, executor, whiteListPlugin);

		Assert.assertEquals(0, rendezvous.whoIsAlive().size());

		rendezvous.iAmAlive(new RendezvousItem(knownMemberId, "cert"));
		Assert.assertEquals(1, rendezvous.whoIsAlive().size());

		rendezvous.iAmAlive(new RendezvousItem(unKnownMemberId, "cert"));
		Assert.assertEquals(1, rendezvous.whoIsAlive().size());
	}
	
	@Test
	public void testMerge() {
		RendezvousImpl rendezvousImpl = new RendezvousImpl(null, properties, executor);
		LinkedList<RendezvousItem> managersAlive = new LinkedList<RendezvousItem>();
		String federationMemberIdOne = "One";
		
		RendezvousItem rendezvousItemOne = new RendezvousItem(federationMemberIdOne);
		Date now = new Date();
		long lastTimeOne = now.getTime() - 2000;
		
		DateUtils dateUtils = Mockito.mock(DateUtils.class);
		Mockito.when(dateUtils.currentTimeMillis()).thenReturn(lastTimeOne);
		
		rendezvousItemOne.setLastTime(lastTimeOne);
		rendezvousItemOne.setDateUtils(dateUtils);
		managersAlive.add(rendezvousItemOne);
		String federationMemberIdTwo = "Two";
		managersAlive.add(new RendezvousItem(federationMemberIdTwo));
		String federationMemberIdThree = "Three";
		managersAlive.add(new RendezvousItem(federationMemberIdThree));
		rendezvousImpl.setManagersAlive(managersAlive );
		
		List<RendezvousItem> managers = new ArrayList<RendezvousItem>();
		long lastTimeTwo = now.getTime() - 1000;
		RendezvousItem newRendezvousItemOne = new RendezvousItem(federationMemberIdOne);
		newRendezvousItemOne.setDateUtils(dateUtils);
		newRendezvousItemOne.setLastTime(lastTimeTwo);
		managers.add(newRendezvousItemOne);
		managers.add(new RendezvousItem("Four"));
		RendezvousResponseItem responseItem = new RendezvousResponseItem(managers);
		rendezvousImpl.merge(responseItem);
		
		for (RendezvousItem rendezvousItem : rendezvousImpl.getManagersAlive().values()) {
			if (rendezvousItem.getMemberId().equals(federationMemberIdOne)) {
				Assert.assertEquals(lastTimeOne, rendezvousItem.getLastTime());
				return;
			}
		}
		Assert.fail();
	}	
	
}
