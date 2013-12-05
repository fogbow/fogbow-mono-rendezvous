package org.ourgid.cloud;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RendezvousImpl implements Rendezvous {
	private int timeOut;
	List<String> aliveIDs = new ArrayList<String>();
	List<Long> timeAlive = new ArrayList<Long>();
	Timer timer = new Timer();

	public RendezvousImpl(int timeOut) {
		this.timeOut = timeOut;
		collectsNotAlive();
	}

	public RendezvousImpl() {

	}

	public void iAmAlive(String id) {
		if (id == null) {
			throw new IllegalArgumentException();
		}
		if (!aliveIDs.contains(id)) {
			aliveIDs.add(id);
			timeAlive.add(System.currentTimeMillis());
		} else {
			timeAlive.add(aliveIDs.indexOf(id), System.currentTimeMillis());
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
		}, 0, 50);
	}
}
