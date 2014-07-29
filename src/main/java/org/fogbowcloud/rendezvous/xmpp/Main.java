package org.fogbowcloud.rendezvous.xmpp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

public class Main {

	private static final String PROP_JID = "xmpp_jid";
	private static final String PROP_PASSWORD = "xmpp_password";
	private static final String PROP_HOST = "xmpp_host";
	private static final String PROP_PORT = "xmpp_port";
	private static final String PROP_EXPIRATION = "site_expiration";
	private static final String DESCRIPTION = "Rendezvous Component";
	private static final String NAME = "rendezvous";
	private static final String PROP_NEIGHBORS = "neighbors";
	private static final String PROP_MAX_WHOISALIVE_MANAGER_COUNT = "max_whoisalive_manager_count";

	private static RendezvousXMPPComponent rendezvousXmppComponent;

	private static void initializeRendezvousXMPPComponent(String configPath)
			throws Exception {
		Properties properties = new Properties();
		FileInputStream input = getFileInputStrean(configPath);
		properties.load(input);

		String jid = getJID(properties);
		String password = getPassword(properties);
		String server = getServer(properties);
		int port = getPort(properties);
		long expiration = getExpiration(properties);
		String[] neighbors = getNeighbors(properties);
		int maxWhoisaliveManagerCount = getWhoisaliveManagerCount(properties);
		rendezvousXmppComponent = new RendezvousXMPPComponent(jid, password,
				server, port, expiration, neighbors);
		rendezvousXmppComponent.setDescription(DESCRIPTION);
		rendezvousXmppComponent.setName(NAME);
		rendezvousXmppComponent.connect();
		rendezvousXmppComponent.process();
	}
	
	private static void checkStringEmpty(String string) {
		if (string == null || string.isEmpty()) {
			throw new IllegalArgumentException();
		}
	}
	
	private static long checkInvalidInteger(Properties properties, String prop) {
		try {
			long expiration = Long.parseLong(properties
					.getProperty(prop));
			if (expiration <= 0) {
				throw new IllegalArgumentException();
			}
			return expiration;
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}
	}
	
	private static int getWhoisaliveManagerCount(Properties properties) {
		return (int) checkInvalidInteger(properties, PROP_MAX_WHOISALIVE_MANAGER_COUNT);
	}

	static FileInputStream getFileInputStrean(String path)
			throws FileNotFoundException {
		checkStringEmpty(path);
		return new FileInputStream(path);
	}

	static String getServer(Properties properties) {
		String server = properties.getProperty(PROP_HOST);
		checkStringEmpty(server);
		return server;
	}

	static String getPassword(Properties properties) {
		String password = properties.getProperty(PROP_PASSWORD);
		checkStringEmpty(password);
		return password;
	}

	static String getJID(Properties properties) {
		String jid = properties.getProperty(PROP_JID);
		checkStringEmpty(jid);
		return jid;
	}

	static int getPort(Properties properties) {
		return (int) checkInvalidInteger(properties, PROP_PORT);
	}

	static long getExpiration(Properties properties) {
		return checkInvalidInteger(properties, PROP_EXPIRATION);
	}

	static String[] getNeighbors(Properties properties) {
		String neighborsList = properties.getProperty(PROP_NEIGHBORS);
		if (neighborsList.isEmpty()) {
			return new String[] {};
		}
		String[] neighborIds = neighborsList.split(",");
		return neighborIds;
	}

	public static void main(String[] args) throws Exception {
		initializeRendezvousXMPPComponent(args[0]);
	}
}
