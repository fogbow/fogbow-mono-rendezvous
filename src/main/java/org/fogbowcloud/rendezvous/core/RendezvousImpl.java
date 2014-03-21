package org.fogbowcloud.rendezvous.core;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.fogbowcloud.rendezvous.core.model.DateUtils;

public class RendezvousImpl implements Rendezvous {

	private static final Logger LOGGER = Logger.getLogger(RendezvousImpl.class);

	private final long timeOut;
	private final Timer timer = new Timer();
	private final ConcurrentHashMap<String, RendezvousItem> aliveIDs = new ConcurrentHashMap<String, RendezvousItem>();
	private boolean inError = false;
	private DateUtils dateUnit;

	public static final long TIMEOUT_DEFAULT = 3 * 60 * 1000;
	private static final long PERIOD = 50;

	public RendezvousImpl(long timeOut) {
		if (timeOut < 0) {
			throw new IllegalArgumentException();
		}
		this.timeOut = timeOut;
		this.dateUnit = new DateUtils();
		collectsNotAlive();
	}

	public RendezvousImpl() {
		this(TIMEOUT_DEFAULT);
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
		aliveIDs.put(resourcesInfo.getId(), new RendezvousItem(resourcesInfo));
	}

	public List<RendezvousItem> whoIsAlive() {
		LOGGER.debug("WhoISAlive done.");
		return new ArrayList<RendezvousItem>(aliveIDs.values());
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
		Iterator<Entry<String, RendezvousItem>> iter = aliveIDs.entrySet().iterator();
		while (iter.hasNext()) {
			try {
				Entry<String, RendezvousItem> entry = iter.next();
				RendezvousItem rendezvousItem = entry.getValue();
				if ((rendezvousItem.getLastTime() + timeOut) < dateUnit.currentTimeMillis()) {
					iter.remove();
					LOGGER.info(rendezvousItem.getResourcesInfo().getId() + " expired.");
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
		if (aliveIDs.get(id) != null) {
			aliveIDs.get(id).setLastTime(lastTime);
		}
	}
}
