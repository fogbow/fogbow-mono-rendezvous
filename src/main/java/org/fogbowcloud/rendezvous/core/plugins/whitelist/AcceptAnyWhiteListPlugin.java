package org.fogbowcloud.rendezvous.core.plugins.whitelist;

import org.fogbowcloud.rendezvous.core.plugins.WhiteListPlugin;

/**
 * Created by thiagoepdc on 10/05/16.
 */
public class AcceptAnyWhiteListPlugin implements WhiteListPlugin {

    @Override
    public boolean contains(String memberId) {
        return true;
    }
}
