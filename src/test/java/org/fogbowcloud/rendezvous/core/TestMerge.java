package org.fogbowcloud.rendezvous.core;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

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
		StringBuilder neighborIdsPropertieValue = new StringBuilder(); 
		for (int i = 0; i < neighborIds.length; i++) {
			if (i != 0) {
				neighborIdsPropertieValue.append(",");
			}
			neighborIdsPropertieValue.append(neighborIds[i]);
		}
		Mockito.when(properties.getProperty("neighbors")).thenReturn(neighborIdsPropertieValue.toString());
		rendezvous = new RendezvousImpl(null, properties, executor);
	}

	@Test
	public void testMergeEmptyResponseItem() {
		// configuring rendezvous
		String[] neighborIds = new String[] { "A", "B" };
		mockNeighbors(neighborIds);
		LinkedList<RendezvousItem> managersAlive = new LinkedList<RendezvousItem>();
		rendezvous.setManagersAlive(managersAlive);

		// rendezvous response
		List<RendezvousItem> newManagers = new LinkedList<RendezvousItem>();
		RendezvousResponseItem responseItem = new RendezvousResponseItem(newManagers);

		rendezvous.merge(responseItem);
		Assert.assertEquals(neighborIds.length, rendezvous.getNeighborIds().size());
		Assert.assertEquals(0, rendezvous.getManagersAliveKeys().size());
	}

	@Test
	public void testMergeNoRepeatedElements() {
		// configuring rendezvous
		String[] neighborIds = new String[] { "A", "B", "C" };
		mockNeighbors(neighborIds);
		LinkedList<RendezvousItem> managersAlive = new LinkedList<RendezvousItem>(
				Arrays.asList(new RendezvousItem("m1", "cert")));
		rendezvous.setManagersAlive(managersAlive);

		// rendezvous response
		List<RendezvousItem> newManagers = new LinkedList<RendezvousItem>(
				Arrays.asList(rendezvousTestHelper.getRendezvousItem()));
		RendezvousResponseItem responseItem = new RendezvousResponseItem(newManagers);

		rendezvous.merge(responseItem);
		Assert.assertEquals(neighborIds.length, rendezvous.getNeighborIds().size());
		Assert.assertEquals(2, rendezvous.getManagersAliveKeys().size());
	}

}
