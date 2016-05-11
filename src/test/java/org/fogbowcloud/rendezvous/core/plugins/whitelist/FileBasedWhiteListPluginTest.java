package org.fogbowcloud.rendezvous.core.plugins.whitelist;

import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by thiagoepdc on 11/05/16.
 */
public class FileBasedWhiteListPluginTest {

    @Test
    public void testWhileListFromFile() throws Exception {

        String propertiesFile = "src/test/resources/whitelist/whitelist.conf";

        Properties properties = new Properties();
        FileInputStream input = new FileInputStream(propertiesFile);
        properties.load(input);

        FileBasedWhiteListPlugin plugin = new FileBasedWhiteListPlugin(properties);

        Assert.assertFalse(plugin.contains("unknow_member"));
        Assert.assertTrue(plugin.contains("manager1"));
        Assert.assertTrue(plugin.contains("manager2"));
    }

}