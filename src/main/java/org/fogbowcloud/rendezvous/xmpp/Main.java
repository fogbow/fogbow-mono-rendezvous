package org.fogbowcloud.rendezvous.xmpp;

import java.io.FileInputStream;
import java.util.Properties;

public class Main {

	private static final String PROP_JID = "xmpp_jid";
	private static final String PROP_PASSWORD = "xmpp_password";
	private static final String PROP_HOST = "xmpp_host";
	private static final String PROP_PORT = "xmpp_port";
	private static final String PROP_EXPIRATION = "site_expiration";
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
		
		RendezvousXMPPComponent rendezvousXmppComponent = new RendezvousXMPPComponent(jid,
				password, server, port, expiration);
		rendezvousXmppComponent.setDescription(DESCRIPTION);
		rendezvousXmppComponent.setName(NAME);
		rendezvousXmppComponent.connect();
		rendezvousXmppComponent.process();
	}

	public static void main(String[] args) throws Exception {				
		initializeRendezvousXMPPComponent(args[0]);
	}

}
