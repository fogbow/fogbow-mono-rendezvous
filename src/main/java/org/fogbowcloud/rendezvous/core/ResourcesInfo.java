package org.fogbowcloud.rendezvous.core;

import java.util.List;

import org.fogbowcloud.rendezvous.core.model.Flavor;

public class ResourcesInfo {
	
	private String id;
	private String cpuIdle;
	private String cpuInUse;
	private String memIdle;
	private String memInUse;
	private List<Flavor> flavours;
	private String cert;
	
	public ResourcesInfo(String id, String cpuIdle, String cpuInUse,
			String memIdle, String memInUse, List<Flavor> flavours, String cert) {
		this.setCert(cert);
		setId(id);
		setCpuIdle(cpuIdle);
		setCpuInUse(cpuInUse);
		setMemIdle(memIdle);
		setMemInUse(memInUse);
		this.setFlavours(flavours);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		if (id == null || id.isEmpty()) {
			throw new IllegalArgumentException("ResourceInfo id is invalid.");
		}
		this.id = id;
	}

	public String getCpuIdle() {
		return cpuIdle;
	}

	public void setCpuIdle(String cpuIdle) {
		if (cpuIdle == null) {
			throw new IllegalArgumentException(
					"ResourceInfo cpu-idle is invalid.");
		}
		this.cpuIdle = cpuIdle;
	}

	public String getCpuInUse() {
		return cpuInUse;
	}

	public void setCpuInUse(String cpuInUse) {
		if (cpuInUse == null) {
			throw new IllegalArgumentException(
					"ResourceInfo cpu-inuse is invalid.");
		}
		this.cpuInUse = cpuInUse;
	}

	public String getMemIdle() {
		return memIdle;
	}

	public void setMemIdle(String memIdle) {
		if (memIdle == null) {
			throw new IllegalArgumentException(
					"ResourceInfo mem-idle is invalid.");
		}
		this.memIdle = memIdle;
	}

	public String getMemInUse() {
		return memInUse;
	}

	public void setMemInUse(String memInUse) {
		if (memInUse == null) {
			throw new IllegalArgumentException(
					"ResourceInfo mem-inuse is invalid.");
		}
		this.memInUse = memInUse;
	}

	public List<Flavor> getFlavours() {
		return flavours;
	}

	public void setFlavours(List<Flavor> flavours) {
		this.flavours = flavours;
	}

	public String getCert() {
		return cert;
	}

	public void setCert(String cert) {
		this.cert = cert;
	}
}
