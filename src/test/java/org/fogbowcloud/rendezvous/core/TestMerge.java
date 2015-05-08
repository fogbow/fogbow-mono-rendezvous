package org.fogbowcloud.rendezvous.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

import org.fogbowcloud.rendezvous.core.model.Flavor;
import org.fogbowcloud.rendezvous.xmpp.model.RendezvousResponseItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.xmpp.component.ComponentException;

public class TestMerge {

	private RendezvousTestHelper rendezvousTestHelper;
	RendezvousImpl rendezvous;
	ScheduledExecutorService executor;
	Properties properties;

	@Before
	public void setUp() throws ComponentException {
		rendezvousTestHelper = new RendezvousTestHelper();
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

	private void mockNeighbors(String[] neighborIds) {
		Mockito.when(properties.getProperty("neighbors")).thenReturn(
				neighborIds.toString());
		rendezvous = new RendezvousImpl(null, properties, executor);
	}

	@Test
	public void testMergeEmptyResponseItem() {
		// configuring rendezvous
		String[] neighborIds = new String[] { "IDSENT" };
		mockNeighbors(neighborIds);
		LinkedList<RendezvousItem> managersAlive = new LinkedList<RendezvousItem>();
		rendezvous.setManagersAlive(managersAlive);

		// rendezvous response
		List<String> neighbors = new LinkedList<String>();
		List<RendezvousItem> newManagers = new LinkedList<RendezvousItem>();
		RendezvousResponseItem responseItem = new RendezvousResponseItem(
				neighbors, newManagers);

		rendezvous.merge(responseItem);
		Assert.assertEquals(1, rendezvous.getNeighborIds().size());
		Assert.assertEquals(0, rendezvous.getManagersAliveKeys().size());
	}

	@Test
	public void testMergeNoRepeatedElements() {
		// configuring rendezvous
		String[] neighborIds = new String[] { "IDSENT" };
		mockNeighbors(neighborIds);
		LinkedList<RendezvousItem> managersAlive = new LinkedList<RendezvousItem>(
				Arrays.asList(new RendezvousItem(new ResourcesInfo("m1", "cpuIdle", "cpuInUse",
						"memIdle", "memInUse", "instanceIdle", "instanceInUse",
						new ArrayList<Flavor>(), "cert"))));
		rendezvous.setManagersAlive(managersAlive);

		// rendezvous response
		List<String> neighbors = new LinkedList<String>(Arrays.asList("r1"));
		List<RendezvousItem> newManagers = new LinkedList<RendezvousItem>(
				Arrays.asList(new RendezvousItem(rendezvousTestHelper
						.getResources())));
		RendezvousResponseItem responseItem = new RendezvousResponseItem(
				neighbors, newManagers);

		rendezvous.merge(responseItem);
		Assert.assertEquals(2, rendezvous.getNeighborIds().size());
		Assert.assertEquals(2, rendezvous.getManagersAliveKeys().size());
	}

	@Test
	public void testMergeRepeatedElements() {
		// configuring rendezvous
		String[] neighborIds = new String[] { "r1", "r2", "r3" };
		mockNeighbors(neighborIds);
		LinkedList<RendezvousItem> managersAlive = new LinkedList<RendezvousItem>();
		RendezvousItem item2 = new RendezvousItem(
				rendezvousTestHelper.getResources());
		managersAlive.add(item2);
		rendezvous.setManagersAlive(managersAlive);
		// rendezvous response
		List<String> neighbors = new LinkedList<String>(Arrays.asList("r2",
				"r3", "r4", "r5"));
		List<RendezvousItem> newManagers = new LinkedList<RendezvousItem>(
				Arrays.asList(item2));
		RendezvousResponseItem responseItem = new RendezvousResponseItem(
				neighbors, newManagers);

		rendezvous.merge(responseItem);
		Assert.assertEquals(5, rendezvous.getNeighborIds().size());
		Assert.assertEquals(1, rendezvous.getManagersAliveKeys().size());
	}
}
