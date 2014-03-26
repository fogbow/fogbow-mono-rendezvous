package org.fogbowcloud.rendezvous.xmpp;

import java.io.FileInputStream;
import java.util.Properties;

public class Main {

	private static final String PROP_JID = "prop_jid";
	private static final String PROP_PASSWORD = "prop_password";
	private static final String PROP_HOST = "prop_host";
	private static final String PROP_PORT = "prop_port";
	private static final String PROP_EXPIRATION = "prop_expiration";
	private static final String DESCRIPTION = "Rendezvous Component";
	private static final String NAME = "rendezvous";

	private static void initializeRendezvousXMPPComponent(String configPath) throws Exception {
		Properties properties = new Properties();
		FileInputStream input = new FileInputStream(configPath);
		properties.load(input);
		String jid = properties.getProperty(PROP_JID);
		String password = properties.getProperty(PROP_PASSWORD);
		String server = properties.getProperty(PROP_HOST);
		int port = Integer.parseInt(properties.getProperty(PROP_PORT));
		long expiration = Long.parseLong(properties.getProperty(PROP_EXPIRATION));
		String description = properties.getProperty("description");
		String name = properties.getProperty("name");
		RendezvousXMPPComponent rendezvousXmppComponent = new RendezvousXMPPComponent(jid,
				password, server, port, expiration);
		rendezvousXmppComponent.setDescription(DESCRIPTION);
		rendezvousXmppComponent.setName(NAME);
		rendezvousXmppComponent.connect();
		rendezvousXmppComponent.process();
	}

	public static void main(String[] args) throws Exception {				
		initializeRendezvousXMPPComponent("config.properties");
	}

}
