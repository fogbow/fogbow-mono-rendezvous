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

	public void iAmAlive(String id) {
		if (id == null) {
			throw new IllegalArgumentException();
		}
		aliveIDs.put(id, new RendezvousItem());
	}

	public List<String> whoIsAlive() {
		return new ArrayList<String>(aliveIDs.keySet());
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
				}
			} catch (ConcurrentModificationException e) {
				inError = true;
			}
		}
	}

	public boolean getIserror() {
		return inError;
	}
}
