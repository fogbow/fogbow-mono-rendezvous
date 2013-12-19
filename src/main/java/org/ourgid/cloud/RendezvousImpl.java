package org.ourgid.cloud;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class RendezvousImpl implements Rendezvous {

	private static final long TIMEOUT_DEFAULT = 3 * 60 * 1000;
	private static final long PERIOD = 50;

	private final long timeOut;
	private final Timer timer = new Timer();
	private final ConcurrentHashMap<String, RendezvousItem> aliveIDs;
	private boolean iserror = false;

	public RendezvousImpl(long timeOut,
			ConcurrentHashMap<String, RendezvousItem> aliveIDs) {
		if (timeOut < 0) {
			throw new IllegalArgumentException();
		}
		this.timeOut = timeOut;
		this.aliveIDs = aliveIDs;
		collectsNotAlive();
	}

	public RendezvousImpl() {
		this(TIMEOUT_DEFAULT);
	}

	public RendezvousImpl(long timeout) {
		this(timeout, new ConcurrentHashMap<String, RendezvousItem>());
	}

	public void iAmAlive(String id) {
		if (id == null) {
			throw new IllegalArgumentException();
		}
		aliveIDs.put(id, new RendezvousItem());
	}

	public List<String> whoIsAlive() {
		List<String> aliveIds = new ArrayList<String>(aliveIDs.keySet());
		return aliveIds;
	}

	private void collectsNotAlive() {
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				Iterator<Entry<String, RendezvousItem>> iter = aliveIDs
						.entrySet().iterator();
				while (iter.hasNext()) {
					try {
						Entry<String, RendezvousItem> entry = iter.next();
						if ((entry.getValue()).getLastTime() + timeOut < System
								.currentTimeMillis()) {
							iter.remove();
						}
					} catch (ConcurrentModificationException e) {
						iserror = true;
					}
				}
			}
		}, 0, PERIOD);
	}

	public boolean getIserror() {
		return iserror;
	}
}
