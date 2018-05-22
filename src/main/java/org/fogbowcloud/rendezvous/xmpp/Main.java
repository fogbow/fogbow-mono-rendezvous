package org.fogbowcloud.rendezvous.xmpp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fogbowcloud.rendezvous.core.ConfigurationConstants;
import org.fogbowcloud.rendezvous.core.plugins.WhiteListPlugin;
import org.fogbowcloud.rendezvous.core.plugins.whitelist.AcceptAnyWhiteListPlugin;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class Main implements ApplicationRunner {

	private static final Logger LOGGER = Logger.getLogger(Main.class);

	//FIXME: move to the config constants class
	private static final String PROP_JID = "xmpp_jid";
	private static final String PROP_PASSWORD = "xmpp_password";
	private static final String PROP_HOST = "xmpp_host";
	private static final String PROP_PORT = "xmpp_port";
	private static final String NAME = "rendezvous";
	private static final String DESCRIPTION = "Rendezvous Component";
	private static final String PROP_EXPIRATION = "site_expiration";
	private static final String PROP_NEIGHBORS = "neighbors";	
	private static final String PROP_RETRY_INTERVAL = "xmpp_retry_interval";

	private static final Long DEFAULT_RETRY_INTERVAL = 30000L;

	private static RendezvousXMPPComponent rendezvousXmppComponent;

	private static void initializeRendezvousXMPPComponent(String configPath) {
		Properties properties = new Properties();
		try {
			FileInputStream input = getFileInputStream(configPath);
			properties.load(input);
			input.close();
		} catch (Exception e) {
			LOGGER.error("Configuration file not found.", e);
			return;
		}

		String jid = getJID(properties);
		String password = getPassword(properties);
		String server = getServer(properties);
		int port = getPort(properties);


		WhiteListPlugin whiteListPlugin = null;
		try {
			whiteListPlugin = (WhiteListPlugin) createInstance(
					ConfigurationConstants.WHITE_LIST_PLUGIN_CLASS, properties);
		} catch (Exception e) {
			LOGGER.warn("White list plugin not specified in the properties. Failing back to AcceptAnyWhiteListPlugin",
					e);
			whiteListPlugin = new AcceptAnyWhiteListPlugin();
		}
		
		rendezvousXmppComponent = new RendezvousXMPPComponent(jid, password,
				server, port, properties, whiteListPlugin);
		rendezvousXmppComponent.setDescription(DESCRIPTION);
		rendezvousXmppComponent.setName(NAME);
		
		while (true) {
			try {
				rendezvousXmppComponent.connect();
				break;
			} catch (Exception e) {
				waitRetryInterval(properties);
				LOGGER.error("Could not connect to XMPP server.", e);
			}			
		}
		
		rendezvousXmppComponent.process();
	}

	private static Object createInstance(String propName, Properties properties) throws Exception {
		return Class.forName(properties.getProperty(propName)).getConstructor(Properties.class)
				.newInstance(properties);
	}

	private static void waitRetryInterval(Properties properties) {
		try {
			Thread.sleep(getRetryInterval(properties));
		} catch (InterruptedException e) {			
		}
	}

	private static void checkStringEmpty(String string) {
		if (string == null || string.isEmpty()) {
			throw new IllegalArgumentException();
		}
	}

	private static long checkInvalidInteger(Properties properties, String prop) {
		try {
			long expiration = Long.parseLong(properties.getProperty(prop));
			if (expiration <= 0) {
				throw new IllegalArgumentException();
			}
			return expiration;
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}
	}

	static FileInputStream getFileInputStream(String path)
			throws FileNotFoundException {
		checkStringEmpty(path);
		return new FileInputStream(path);
	}

	static Long getRetryInterval(Properties properties) {
		String retryIntervalStr = properties.getProperty(PROP_RETRY_INTERVAL);
		try {
			return Long.valueOf(retryIntervalStr);			
		} catch (Exception e) {
			return DEFAULT_RETRY_INTERVAL;
		}
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

	@Override
	public void run(ApplicationArguments args) throws Exception {
		initializeRendezvousXMPPComponent(args.getSourceArgs()[0]);
	}
}
