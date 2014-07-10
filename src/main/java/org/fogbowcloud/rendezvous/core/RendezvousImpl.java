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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.fogbowcloud.rendezvous.core.model.DateUtils;
import org.fogbowcloud.rendezvous.xmpp.RendezvousPacketHelper;
import org.fogbowcloud.rendezvous.xmpp.model.RendezvousResponseItem;
import org.jamppa.component.PacketSender;

public class RendezvousImpl implements Rendezvous {

	private static final Logger LOGGER = Logger.getLogger(RendezvousImpl.class);

	private final long timeOut;
	private final Timer timer = new Timer();
	private final ConcurrentHashMap<String, RendezvousItem> managersAlive = new ConcurrentHashMap<String, RendezvousItem>();
	private boolean inError = false;
	private DateUtils dateUnit;
	private Set<String> neighborIds = new HashSet<String>();
	public static final long TIMEOUT_DEFAULT = 3 * 60 * 1000;
	private static final long PERIOD = 50;
	private PacketSender packetSender;

	public RendezvousImpl(long timeOut, PacketSender packetSender, String[] neighbors) {
		if (timeOut < 0) {
			throw new IllegalArgumentException();
		}
		this.timeOut = timeOut;
		this.dateUnit = new DateUtils();
		this.packetSender = packetSender;
		neighborIds = new HashSet<String>(Arrays.asList(neighbors));
		collectsNotAlive();
		//TODO continuousSyncWithNeighbors();
	}
	
	public void setPacketSender(PacketSender packetSender) {
		this.packetSender = packetSender;
	}

	public RendezvousImpl(PacketSender packetSender, String[] neighbors) {
		this(TIMEOUT_DEFAULT, packetSender, neighbors);
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
		managersAlive.put(resourcesInfo.getId(), new RendezvousItem(
				resourcesInfo));
	}

	public List<RendezvousItem> whoIsAlive() {
		LOGGER.debug("WhoISAlive done.");
		return new ArrayList<RendezvousItem>(managersAlive.values());
	}

	private void collectsNotAlive() {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				checkExpiredAliveIDs();
			}
		}, 0, PERIOD);
	}

	protected void checkExpiredAliveIDs() {
		Iterator<Entry<String, RendezvousItem>> iter = managersAlive.entrySet()
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
		if (managersAlive.get(id) != null) {
			managersAlive.get(id).setLastTime(lastTime);
		}
	}
	
	//TODO integrate this method 
	public void continuousSyncWithNeighbors() {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				syncWhoIsAlive();
			}
		}, 0, PERIOD);
	}
	
	public void syncWhoIsAlive() {
		Iterator<String> iter = neighborIds.iterator();
		RendezvousResponseItem responseItem = null;
		while (iter.hasNext()) {
			String entry = iter.next();
			try {
				responseItem = RendezvousPacketHelper.sendWhoIsAliveSyncIq(
						entry, packetSender);
				merge(responseItem);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	public Set<String> getNeighborIds() {
		return neighborIds;
	}

	public Set<String> getManagersAliveKeys() {
		return managersAlive.keySet();
	}
	
	public ConcurrentHashMap<String, RendezvousItem> getManagersAlive() {
		return managersAlive;
	}
	
	// Method for testing
	public void setNeighborIds(Set<String> neighborIds) {
		this.neighborIds = neighborIds;
	}

	// Method for testing
	public void setManagersAlive(LinkedList<RendezvousItem> managersAlive) {
		for (RendezvousItem item : managersAlive) {
			this.managersAlive.put(item.getResourcesInfo().getId(), item);
		}
	}

	public void merge(RendezvousResponseItem responseItem) {
		neighborIds.addAll(responseItem.getNeighbors());
		for (RendezvousItem item : responseItem.getKnownManagersAlive()) {
			if (!managersAlive.containsKey(item.getResourcesInfo().getId())) {
				managersAlive.put(item.getResourcesInfo().getId(), item);
			}
		}

	}
}
