package org.fogbowcloud.rendezvous.core.plugins;

import java.util.List;

/**
 * Created by thiagoepdc on 10/05/16.
 */
public interface WhiteListPlugin {

    boolean contains(String memberId);
}
