package org.fogbowcloud.rendezvous.core.plugins.whitelist;

import java.util.Properties;

import org.fogbowcloud.rendezvous.core.plugins.WhiteListPlugin;

/**
 * Created by thiagoepdc on 10/05/16.
 */
public class AcceptAnyWhiteListPlugin implements WhiteListPlugin {
	
	public AcceptAnyWhiteListPlugin() {}
	
	public AcceptAnyWhiteListPlugin(Properties properties) {}

    @Override
    public boolean contains(String memberId) {
        return true;
    }
}
