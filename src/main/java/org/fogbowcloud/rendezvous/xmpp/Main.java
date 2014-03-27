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

	private static RendezvousXMPPComponent rendezvousXmppComponent;

	static void initializeRendezvousXMPPComponent(String configPath) throws Exception {
		Properties properties = new Properties();
		FileInputStream input = getFileInputStrean(configPath);
		properties.load(input);

		String jid = getJID(properties);
		String password = getPassword(properties);
		String server = getServer(properties);
		int port = getPort(properties);
		long expiration = getExpiration(properties);

		rendezvousXmppComponent = new RendezvousXMPPComponent(jid, password, server, port,
				expiration);
		rendezvousXmppComponent.setDescription(DESCRIPTION);
		rendezvousXmppComponent.setName(NAME);
		rendezvousXmppComponent.connect();
		rendezvousXmppComponent.process();
	}

	static FileInputStream getFileInputStrean(String path) throws FileNotFoundException {
		if (path == null) {
			throw new IllegalArgumentException();
		}
		return new FileInputStream(path);
	}

	static String getServer(Properties properties) {
		String server = properties.getProperty(PROP_HOST);
		if (server.equals("")) {
			throw new IllegalArgumentException();
		}
		return server;
	}

	static String getPassword(Properties properties) {
		String password = properties.getProperty(PROP_PASSWORD);
		if (password.equals("")) {
			throw new IllegalArgumentException();
		}
		return password;
	}

	static String getJID(Properties properties) {
		String jid = properties.getProperty(PROP_JID);
		if (jid.equals("")) {
			throw new IllegalArgumentException();
		}
		return jid;
	}

	static int getPort(Properties properties) {
		try {
			int port = Integer.parseInt(properties.getProperty(PROP_PORT));
			if (port <= 0) {
				throw new IllegalArgumentException();
			}
			return port;
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}

	}

	static long getExpiration(Properties properties) {
		try {
			long expiration = Long.parseLong(properties.getProperty(PROP_EXPIRATION));
			if (expiration <= 0) {
				throw new IllegalArgumentException();
			}
			return expiration;
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}
	}

	public static void main(String[] args) throws Exception {
		initializeRendezvousXMPPComponent(args[0]);
	}
}
