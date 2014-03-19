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
import org.jamppa.component.XMPPComponent;

public class RendezvousImpl implements Rendezvous {

	private static final Logger LOGGER = Logger.getLogger(RendezvousImpl.class);

	public static final long TIMEOUT_DEFAULT = 3 * 60 * 1000;
	private static final long PERIOD = 50;

	private final long timeOut;
	private final Timer timer = new Timer();
	private final ConcurrentHashMap<String, RendezvousItem> aliveIDs = new ConcurrentHashMap<String, RendezvousItem>();
	private boolean inError = false;

	public RendezvousImpl(long timeOut) {
		if (timeOut < 0) {
			throw new IllegalArgumentException();
		}
		this.timeOut = timeOut;
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

	private void checkExpiredAliveIDs() {
		Iterator<Entry<String, RendezvousItem>> iter = aliveIDs.entrySet()
				.iterator();
		while (iter.hasNext()) {
			try {
				Entry<String, RendezvousItem> entry = iter.next();
				if ((entry.getValue()).getLastTime() + timeOut < System
						.currentTimeMillis()) {
					iter.remove();
					LOGGER.info(entry.getValue().getResourcesInfo().getId()
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
}
