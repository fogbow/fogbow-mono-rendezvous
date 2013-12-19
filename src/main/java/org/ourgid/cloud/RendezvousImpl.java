package org.ourgid.cloud;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

public class RendezvousImpl implements Rendezvous {

	private static final long TIMEOUT_DEFAULT = 3 * 60 * 1000;
	private static final long PERIOD = 50;

	private final long timeOut;
	private final Timer timer = new Timer();
	private final Map<String, RendezvousItem> aliveIDs;
	private boolean iserror = false;
	private ReentrantLock lock = new ReentrantLock();

	public RendezvousImpl(long timeOut, Map<String, RendezvousItem> aliveIDs) {
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
		this(timeout, new HashMap<String, RendezvousItem>());
	}

	public void iAmAlive(String id) {
		lock.lock();
		try {
			if (id == null) {
				throw new IllegalArgumentException();
			}
			aliveIDs.put(id, new RendezvousItem());
		} finally {
			lock.unlock();
		}
	}

	public List<String> whoIsAlive() {
		List<String> aliveIds = new ArrayList<String>(aliveIDs.keySet());
		return aliveIds;
	}

	private void collectsNotAlive() {
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				lock.lock();
				try {
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
				} finally {
					lock.unlock();
				}
			}
		}, 0, PERIOD);
	}

	public boolean getIserror() {
		return iserror;
	}
}
