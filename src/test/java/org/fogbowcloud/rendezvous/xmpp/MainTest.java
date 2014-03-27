package org.fogbowcloud.rendezvous.xmpp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class MainTest {

	private final String DEFAULT_PATH_TEST_VALUE_EMPTY = "rendezvous.conf.testEmptyValue";
	private final String PATH_TEST_VALUE_WRONG = "rendezvous.conf.testWrongValue";

	private Properties properties = new Properties();
	private FileInputStream fileInputStream;

	@Before
	public void setUp() throws IOException {
		this.fileInputStream = Main.getFileInputStrean(DEFAULT_PATH_TEST_VALUE_EMPTY);
		this.properties.load(fileInputStream);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMainPathFileConfigNull() throws FileNotFoundException {
		Main.getFileInputStrean(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMainFileConfigValuePortEmpty() {
		Main.getPort(this.properties);		
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMainFileConfigValueExpirationEmpty() {
		Main.getExpiration(this.properties);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMainFileConfigJIDEmpty() {
		Main.getJID(this.properties);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMainFileConfigPasswordEmpty() {
		Main.getPassword(this.properties);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMainFileConfigServerEmpty() {
		Main.getServer(this.properties);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMainFileConfigValuePortString() throws IOException {
		this.fileInputStream = Main.getFileInputStrean(PATH_TEST_VALUE_WRONG);
		this.properties.load(fileInputStream);

		Main.getPort(this.properties);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMainFileConfigValueExpirationString() throws IOException {
		this.fileInputStream = Main.getFileInputStrean(PATH_TEST_VALUE_WRONG);
		this.properties.load(fileInputStream);

		Main.getExpiration(this.properties);
	}

}
