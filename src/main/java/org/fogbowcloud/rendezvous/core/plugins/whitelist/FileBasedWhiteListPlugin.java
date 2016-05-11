package org.fogbowcloud.rendezvous.core.plugins.whitelist;

import org.apache.log4j.Logger;
import org.fogbowcloud.rendezvous.core.plugins.WhiteListPlugin;
import org.fogbowcloud.rendezvous.xmpp.Main;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.fogbowcloud.rendezvous.core.ConfigurationConstants.WHITE_LIST_PATH;

/**
 * Created by thiagoepdc on 10/05/16.
 */
public class FileBasedWhiteListPlugin implements WhiteListPlugin {

    private static final Logger LOGGER = Logger.getLogger(Main.class);

    private List<String> whiteListMembers;

    public FileBasedWhiteListPlugin(Properties properties) {
        String whiteListPath = properties.getProperty(WHITE_LIST_PATH);
        this.whiteListMembers = readListFromFile(whiteListPath);
    }

    private List<String> readListFromFile(String pathname) {

        List<String> lines = new LinkedList<String>();

        try {

            FileInputStream fis = new FileInputStream(pathname);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));

            String member;
            while ((member = bufferedReader.readLine()) != null) {
                if (!member.trim().isEmpty()) {
                    lines.add(member.trim());
                }
            }

            bufferedReader.close();

        } catch (FileNotFoundException e) {
            LOGGER.error("White list file <" + pathname + "> not found.");
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOGGER.error("Error accessing White list file <" + pathname + "> .");
            throw new RuntimeException(e);
        }

        return lines;
    }

    @Override
    public boolean contains(String memberId) {
        return whiteListMembers.contains(memberId);
    }
}
