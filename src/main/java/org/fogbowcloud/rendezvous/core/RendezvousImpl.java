package org.fogbowcloud.rendezvous.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.fogbowcloud.rendezvous.core.model.DateUtils;
import org.fogbowcloud.rendezvous.core.plugins.WhiteListPlugin;
import org.fogbowcloud.rendezvous.core.plugins.whitelist.AcceptAnyWhiteListPlugin;
import org.fogbowcloud.rendezvous.xmpp.RendezvousPacketHelper;
import org.fogbowcloud.rendezvous.xmpp.model.RendezvousResponseItem;
import org.jamppa.component.PacketSender;

public class RendezvousImpl implements Rendezvous {

	private static final Logger LOGGER = Logger.getLogger(RendezvousImpl.class);
	private static final long MANAGER_EXPIRATION_PERIOD = 60; // in seconds
	private static final long NEIGHBOR_SYNCHRONIZATION_PERIOD = 60; // in
	private static final int DEFAULT_MAX_WHOISALIVE_MANAGER_COUNT = 100;
	private static final int DEFAULT_MAX_WHOISALIVESYNC_MANAGER_COUNT = 100;
	private static final int DEFAULT_MAX_WHOISALIVESYNC_NEIGHBOR_COUNT = 100;// seconds

	private static final String PROP_NEIGHBORS = "neighbors";
	private static final String PROP_MAX_WHOISALIVE_MANAGER_COUNT = "max_whoisalive_manager_count";
	private static final String PROP_MAX_WHOISALIVESYNC_MANAGER_COUNT = "max_whoisalivesync_manager_count";
	private static final String PROP_MAX_WHOISALIVESYNC_NEIGHBOR_COUNT = "max_whoisalivesync_neighbor_count";
	
	public static final String PROP_I_AM_ALIVE_PERIOD = "iamalive_period";
	public static final long DEFAULT_I_AM_ALIVE_PERIOD = 3 * 60 * 1000; // in millis
	public static final String PROP_I_AM_ALIVE_MAX_MESSAGE_LOST = "iamalive_max_message_lost";
	public static final int DEFAULT_I_AM_ALIVE_MAX_MESSAGE_LOST = 3;

	private ScheduledExecutorService executor;
	private final Map<String, RendezvousItem> aliveManagers = new ConcurrentHashMap<String, RendezvousItem>();
	private boolean inError = false;
	private DateUtils dateUnit;
	private Set<String> neighborsIds = new HashSet<String>();
	private PacketSender packetSender;
	private int maxWhoisaliveManagerCount;
	private int maxWhoisalivesyncManagerCount;
	private int maxWhoisalivesyncNeighborCount;
	private Properties properties;
	private WhiteListPlugin whiteListPlugin;

	public RendezvousImpl(PacketSender packetSender, Properties properties,
						  ScheduledExecutorService executor) {
		this(packetSender, properties, executor, new AcceptAnyWhiteListPlugin());
	}

	public RendezvousImpl(PacketSender packetSender, Properties properties,
						  ScheduledExecutorService executor,
						  WhiteListPlugin whiteListPlugin) {

		this.properties = properties;
		this.dateUnit = new DateUtils();
		this.packetSender = packetSender;
		this.executor = executor;
		this.whiteListPlugin = whiteListPlugin;
		this.neighborsIds = new HashSet<String>(
				Arrays.asList(getNeighborsFromProperties()));
		this.maxWhoisaliveManagerCount = (int) parseLongFromConfiguration(
				PROP_MAX_WHOISALIVE_MANAGER_COUNT,
				DEFAULT_MAX_WHOISALIVE_MANAGER_COUNT);
		this.maxWhoisalivesyncManagerCount = (int) parseLongFromConfiguration(
				PROP_MAX_WHOISALIVESYNC_MANAGER_COUNT,
				DEFAULT_MAX_WHOISALIVESYNC_MANAGER_COUNT);
		this.maxWhoisalivesyncNeighborCount = (int) parseLongFromConfiguration(
				PROP_MAX_WHOISALIVESYNC_NEIGHBOR_COUNT,
				DEFAULT_MAX_WHOISALIVESYNC_NEIGHBOR_COUNT);
	}

	private String[] getNeighborsFromProperties() {
		String neighborsList = properties.getProperty(PROP_NEIGHBORS);
		if (neighborsList == null || neighborsList.isEmpty()) {
			return new String[] {};
		}
		String[] neighborIds = neighborsList.split(",");
		return neighborIds;
	}

	private long parseLongFromConfiguration(String propName, long defaultVaue) {
		String propValue = properties.getProperty(propName);
		if (propValue == null || propValue.isEmpty()) {
			return defaultVaue;
		}
		long timeOut = Long.parseLong(propValue);
		if (timeOut < 0) {
			throw new IllegalArgumentException();
		}
		return timeOut;
	}

	public void setPacketSender(PacketSender packetSender) {
		this.packetSender = packetSender;
	}

	public void iAmAlive(RendezvousItem rendezvousItem) {

		if (rendezvousItem == null) {
			throw new IllegalArgumentException();
		}

		String memberId = rendezvousItem.getMemberId();

		LOGGER.info("Receiving iAmAlive from " + memberId);

		if (whiteListPlugin.contains(memberId)) {
			aliveManagers.put(memberId, rendezvousItem);
		} else {
			LOGGER.info("Ignoring iAmAlive from unknown " + memberId);
		}
	}

	public List<RendezvousItem> whoIsAlive() {
		LOGGER.debug("WhoISAlive done.");
		checkExpiredAliveIDs();
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

	public void checkExpiredAliveIDs() {
		Iterator<Entry<String, RendezvousItem>> iter = aliveManagers.entrySet()
				.iterator();
		while (iter.hasNext()) {
			try {
				Entry<String, RendezvousItem> entry = iter.next();
				RendezvousItem rendezvousItem = entry.getValue();
				if (rendezvousItem.getLastTime() + rendezvousItem.getTimeout() < dateUnit.currentTimeMillis()) {
					iter.remove();
					LOGGER.info(rendezvousItem.getMemberId() + " expired.");
				}
			} catch (ConcurrentModificationException e) {
				inError = true;
			}
		}
	}

	protected boolean getInError() {
		return inError;
	}

	public void setDateUnit(DateUtils dataUnit) {
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
		for (String neighbor : neighborsIds) {
			try {
				responseItem = RendezvousPacketHelper
						.sendWhoIsAliveSyncIq(neighbor, packetSender);
				merge(responseItem);
			} catch (Throwable e) {
				LOGGER.warn("Couldn't sync with neighbor " + neighbor, e);
			}
		}
		checkExpiredAliveIDs();
	}

	public Set<String> getNeighborIds() {
		return neighborsIds;
	}

	public Set<String> getManagersAliveKeys() {
		return aliveManagers.keySet();
	}

	public Map<String, RendezvousItem> getManagersAlive() {
		return aliveManagers;
	}

	protected void setManagersAlive(LinkedList<RendezvousItem> managersAlive) {
		for (RendezvousItem item : managersAlive) {
			this.aliveManagers.put(item.getMemberId(), item);
		}
	}

	public void merge(RendezvousResponseItem responseItem) {
		for (RendezvousItem item : responseItem.getManagers()) {
			RendezvousItem currentRendezvousItem = aliveManagers.get(item.getMemberId());
			if (currentRendezvousItem != null && currentRendezvousItem.isOlderThan(item)) {
				continue;
			}
			aliveManagers.put(item.getMemberId(), item);
		}
	}

	@Override
	public void init() {
		expireDeadManagers();
		triggerNeighborsSynchronization();
	}

	public int getMaxWhoisaliveManagerCount() {
		return maxWhoisaliveManagerCount;
	}

	public int getMaxWhoisaliveSyncNeighborCount() {
		return maxWhoisalivesyncNeighborCount;
	}

	public int getMaxWhoisaliveSyncManagerCount() {
		return maxWhoisalivesyncManagerCount;
	}
}
