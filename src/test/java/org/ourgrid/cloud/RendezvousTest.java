package org.ourgrid.cloud;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.rules.ExpectedException;
import org.ourgid.cloud.Rendezvous;
import org.ourgid.cloud.RendezvousImpl;

public class RendezvousTest {

    @Test
	public void testImAliveSingleElement() {
    	Rendezvous r = new RendezvousImpl();
    	r.iAmAlive("abc");
    	List<String> element = r.whoIsAlive();
    	Assert.assertEquals(1, element.size());
    	Assert.assertEquals("abc", element);
	}
    
    
    @Test
    public void testImAliveManyElements() {
    	Rendezvous r = new RendezvousImpl();
    	for(int i = 0; i < 10; i++) {
    		String s = "Element " + i;
    		r.iAmAlive(s);
    		List <String> elementList = r.whoIsAlive();
    		Assert.assertTrue(elementList.contains(s));
    	}
    }
    
    @Test 
    public void testContainsSameIDs() {
    	Rendezvous r = new RendezvousImpl();
    	r.iAmAlive("id");
    	r.iAmAlive("id");
    	List <String> elementList = r.whoIsAlive();
    	Assert.assertTrue(elementList.size() == 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testImAliveNullParameter() {
    	Rendezvous r = new RendezvousImpl();
    	r.iAmAlive(null);
    }
   
    @Test
    public void testwhoIsAliveEmpty() {
    	Rendezvous r = new RendezvousImpl();
    	List<String> elementList = r.whoIsAlive();
    	Assert.assertTrue(elementList.size() == 0);
    }
    
    @Test
    public void testwhoIsAliveSingleElement() {
    	Rendezvous r = new RendezvousImpl();
    	r.iAmAlive("OnlyElement");
    	Assert.assertEquals(1, r.whoIsAlive().size());
    	Assert.assertTrue(r.whoIsAlive().contains("OnlyElement"));
    }
    
    @Test
    public void testwhoIsAliveManyElements() {
    	Rendezvous r = new RendezvousImpl();
    	for(int i = 0; i < 10; i++) {
    		r.iAmAlive("Element" + (i+1));
    	}
    	
    	for(int i = 0; i < 10; i++) {
    		Assert.assertTrue(r.whoIsAlive().contains("Element" + (i+1)));
    	}
    }
}
