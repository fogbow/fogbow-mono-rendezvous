package org.fogbowcloud.rendezvous.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.fogbowcloud.rendezvous.core.model.DateUtils;
import org.fogbowcloud.rendezvous.xmpp.util.FederationMember;

public class RendezvousItem extends FederationMember {

	private static final String ISO_8601_DATE_FORMAT_STR = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static final SimpleDateFormat ISO_8601_DATE_FORMAT = new SimpleDateFormat(
			ISO_8601_DATE_FORMAT_STR, Locale.ROOT);
	static {
		ISO_8601_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	private DateUtils dateUtils;
	private String memberId;
	private long lastTime;
	private long timeout;
	private String cert;

	public RendezvousItem(String federationMemberId, String cert) {
		this(federationMemberId, cert, 0);
	}
	
	public RendezvousItem(String federationMemberId, String cert, long timeout) {
		this(federationMemberId);
		setCert(cert);
		setTimeout(timeout);
	}

	public RendezvousItem(String federationMemberId) {
		if (federationMemberId == null || federationMemberId.isEmpty()) {
			throw new IllegalArgumentException();
		}
		setMemberId(federationMemberId);
		setLastTime(new DateUtils().currentTimeMillis());
		this.dateUtils = new DateUtils();
	}

	public long getLastTime() {
		return lastTime;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getCert() {
		return cert;
	}

	public void setCert(String cert) {
		this.cert = cert;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public String getFormattedTime() {
		return ISO_8601_DATE_FORMAT.format(new Date(lastTime));
	}

	/**
	 * This method was implemented just for unit test.
	 * 
	 * @param lastTime
	 */
	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}
		
	public void setDateUtils(DateUtils dateUtils) {
		this.dateUtils = dateUtils;
	}

	public boolean isOlderThan(RendezvousItem rendezvousItem) {
		long now = dateUtils.currentTimeMillis();
		if ((now - lastTime) < (now - rendezvousItem.getLastTime())) {
			return true;
		}
		return false;
	}

}
