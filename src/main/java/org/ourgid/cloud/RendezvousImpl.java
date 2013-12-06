package org.ourgid.cloud;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RendezvousImpl implements Rendezvous {
	
	private static final long TIMEOUT_DEFAULT = 3 * 60 * 1000;
	private static final long PERIOD = 50;
	
	private long timeOut;
	private List<String> aliveIDs = new ArrayList<String>();
	private List<Long> timeAlive = new ArrayList<Long>();
	private Timer timer = new Timer();
	
	public RendezvousImpl(long timeOut) {
		if(timeOut < 0) throw new IllegalArgumentException();
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
		if (!aliveIDs.contains(id)) {
			aliveIDs.add(id);
			timeAlive.add(System.currentTimeMillis());
		} else {
			timeAlive.set(aliveIDs.indexOf(id), System.currentTimeMillis());
		}
	}

	public List<String> whoIsAlive() {
		return aliveIDs;
	}

	private void collectsNotAlive() {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				for (int i = aliveIDs.size() - 1; i >= 0; i--) {
					if (System.currentTimeMillis() >= timeAlive.get(i)
							+ timeOut) {
						aliveIDs.remove(i);
						timeAlive.remove(i);
					}
				}
			}
		}, 0, PERIOD);
	}
}
