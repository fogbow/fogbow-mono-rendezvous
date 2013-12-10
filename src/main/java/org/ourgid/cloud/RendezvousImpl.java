package org.ourgid.cloud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

public class RendezvousImpl implements Rendezvous {
	
	private static final long TIMEOUT_DEFAULT = 3 * 60 * 1000;
	private static final long PERIOD = 50;
	
	private long timeOut;
	private Timer timer = new Timer();
	private HashMap <String, RendezvousItem> aliveIDs = new HashMap<String, RendezvousItem>();
	
	public RendezvousImpl(long timeOut) {
		if(timeOut < 0) throw new IllegalArgumentException();
		this.timeOut = timeOut;
		collectsNotAlive();
	}

	public RendezvousImpl() {
		this(TIMEOUT_DEFAULT);
	}

	public synchronized void iAmAlive(String id) {
		if (id == null) {
			throw new IllegalArgumentException();
		}	
		aliveIDs.put(id, new RendezvousItem());	
	}

	public synchronized List<String> whoIsAlive()  {
		List<String> aliveIds = new ArrayList<String>( aliveIDs.keySet());
		return aliveIds;
	}

	private synchronized void collectsNotAlive () {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				for (Entry<String, RendezvousItem> entry : aliveIDs.entrySet()) {
				    if((entry.getValue()).getLastTime() + timeOut < System.currentTimeMillis()) {
				    	 aliveIDs.remove(entry.getKey());
				    }
				}
			}
		}, 0, PERIOD);
	}
}
