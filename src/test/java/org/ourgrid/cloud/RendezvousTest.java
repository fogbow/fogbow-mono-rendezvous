package org.ourgrid.cloud;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.ourgid.cloud.Rendezvous;
import org.ourgid.cloud.RendezvousImpl;

public class RendezvousTest {

    @Test
	public void testImAlive() {
    	Rendezvous r = new RendezvousImpl();
    	r.iAmAlive("abc");
    	List<String> valor = r.whoIsAlive();
    	Assert.assertEquals(1, valor.size());
	}
}
