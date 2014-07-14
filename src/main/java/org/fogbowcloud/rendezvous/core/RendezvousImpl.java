package org.fogbowcloud.rendezvous.core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.fogbowcloud.rendezvous.core.model.DateUtils;
import org.fogbowcloud.rendezvous.xmpp.RendezvousPacketHelper;
import org.fogbowcloud.rendezvous.xmpp.model.RendezvousResponseItem;
import org.jamppa.component.PacketSender;

public class RendezvousImpl implements Rendezvous {

	public static final long DEFAULT_TIMEOUT = 3 * 60 * 1000; // in millis
	private static final Logger LOGGER = Logger.getLogger(RendezvousImpl.class);
	private static final long MANAGER_EXPIRATION_PERIOD = 60; // in seconds
	private static final long NEIGHBOR_SYNCHRONIZATION_PERIOD = 60; // in
																	// seconds

	private final long timeOut;
	private ScheduledExecutorService executor;
	private final ConcurrentHashMap<String, RendezvousItem> aliveManagers = new ConcurrentHashMap<String, RendezvousItem>();
	private boolean inError = false;
	private DateUtils dateUnit;
	private Set<String> neighborIds = new HashSet<String>();
	private PacketSender packetSender;

	public RendezvousImpl(long timeOut, PacketSender packetSender,
			String[] neighbors, ScheduledExecutorService executor) {
		if (timeOut < 0) {
			throw new IllegalArgumentException();
		}
		this.timeOut = timeOut;
		this.dateUnit = new DateUtils();
		this.packetSender = packetSender;
		this.neighborIds = new HashSet<String>(Arrays.asList(neighbors));
		this.executor = executor;
	}

	public RendezvousImpl(long timeOut, PacketSender packetSender,
			String[] neighbors) {
		this(timeOut, packetSender, neighbors, Executors
				.newScheduledThreadPool(10));
	}

	public RendezvousImpl(PacketSender packetSender, String[] neighbors) {
		this(DEFAULT_TIMEOUT, packetSender, neighbors);
	}

	public void setPacketSender(PacketSender packetSender) {
		this.packetSender = packetSender;
	}

	public void iAmAlive(ResourcesInfo resourcesInfo) {
		if (resourcesInfo == null) {
			throw new IllegalArgumentException();
		}
		LOGGER.info("Receiving iAmAlive from '" + resourcesInfo.getId()
				+ "': MemIdle : '" + resourcesInfo.getMemIdle()
				+ "'; MemInUse : '" + resourcesInfo.getMemInUse()
				+ "'; CpuIdle : '" + resourcesInfo.getCpuIdle()
				+ "'; CPuInUse : '" + resourcesInfo.getCpuInUse() + "'");
		aliveManagers.put(resourcesInfo.getId(), new RendezvousItem(
				resourcesInfo));
	}

	public List<RendezvousItem> whoIsAlive() {
		LOGGER.debug("WhoISAlive done.");
		return new ArrayList<RendezvousItem>(aliveManagers.values());
	}

	private void expireDeadManagers() {
		executor.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				checkExpiredAliveIDs();
			}
		}, 0, MANAGER_EXPIRATION_PERIOD, TimeUnit.SECONDS);
	}

	protected void checkExpiredAliveIDs() {
		Iterator<Entry<String, RendezvousItem>> iter = aliveManagers.entrySet()
				.iterator();
		while (iter.hasNext()) {
			try {
				Entry<String, RendezvousItem> entry = iter.next();
				RendezvousItem rendezvousItem = entry.getValue();
				if ((rendezvousItem.getLastTime() + timeOut) < dateUnit
						.currentTimeMillis()) {
					iter.remove();
					LOGGER.info(rendezvousItem.getResourcesInfo().getId()
							+ " expired.");
				}
			} catch (ConcurrentModificationException e) {
				inError = true;
			}
		}
	}

	protected boolean getInError() {
		return inError;
	}

	protected void setDateUnit(DateUtils dataUnit) {
		this.dateUnit = dataUnit;
	}

	protected void setLastTime(String id, long lastTime) {
		if (aliveManagers.get(id) != null) {
			aliveManagers.get(id).setLastTime(lastTime);
		}
	}

	public void triggerNeighborsSynchronization() {
		executor.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				syncNeighbors();
			}
		}, 0, NEIGHBOR_SYNCHRONIZATION_PERIOD, TimeUnit.SECONDS);
	}

	public void syncNeighbors() {
		RendezvousResponseItem responseItem = null;
		for (String neighbor : neighborIds) {
			try {
				responseItem = RendezvousPacketHelper.sendWhoIsAliveSyncIq(
						neighbor, packetSender);
				merge(responseItem);
			} catch (Throwable e) {
				LOGGER.warn("Couldn't sync with neighbor " + neighbor, e);
			}
		}
	}

	public Set<String> getNeighborIds() {
		return neighborIds;
	}

	public Set<String> getManagersAliveKeys() {
		return aliveManagers.keySet();
	}

	public ConcurrentHashMap<String, RendezvousItem> getManagersAlive() {
		return aliveManagers;
	}

	protected void setManagersAlive(LinkedList<RendezvousItem> managersAlive) {
		for (RendezvousItem item : managersAlive) {
			this.aliveManagers.put(item.getResourcesInfo().getId(), item);
		}
	}

	public void merge(RendezvousResponseItem responseItem) {
		neighborIds.addAll(responseItem.getNeighbors());
		for (RendezvousItem item : responseItem.getKnownManagersAlive()) {
			aliveManagers.put(item.getResourcesInfo().getId(), item);
		}
	}

	@Override
	public void init() {
		expireDeadManagers();
		triggerNeighborsSynchronization();
	}
}
