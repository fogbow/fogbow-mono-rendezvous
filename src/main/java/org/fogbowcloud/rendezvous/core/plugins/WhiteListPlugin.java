package org.fogbowcloud.rendezvous.core.plugins;

/**
 * Created by thiagoepdc on 10/05/16.
 */
public interface WhiteListPlugin {

    boolean contains(String memberId);
}
