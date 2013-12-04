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
    	List<String> Element = r.whoIsAlive();
    	Assert.assertEquals(1, Element.size());
    	Assert.assertEquals("abc", Element);
	}
    
    @Test
    public void testImAliveEqualElements() {
    	Rendezvous r = new RendezvousImpl();
    	for(int i = 0; i < 10; i++) {
    		r.iAmAlive("Hola");		
    	}
    	
    	List <String> ElementList = r.whoIsAlive();
    	
    	for(int i = 0; i < 10;i++) {
    	Assert.assertEquals("Hola", ElementList.get(i));	
    	}
    }
    
    @Test
    public void testImAliveManyElements() {
    	Rendezvous r = new RendezvousImpl();
    	for(int i = 0; i < 10; i++) {
    		String s = "Element " + i;
    		r.iAmAlive(s);
    		List <String> ElementList = r.whoIsAlive();
    		Assert.assertEquals(s, ElementList.get(i));	
    	}
    }
    @Test(expected = IllegalArgumentException.class)
    public void testImAliveNullParameter() {
    	Rendezvous r = new RendezvousImpl();
    	r.iAmAlive(null);
    }
   
    @Test
    public void testwhoIsAliveEmpty() {
    	Rendezvous r = new RendezvousImpl();
    	List<String> ElementList = r.whoIsAlive();
    	Assert.assertTrue(ElementList.size() == 0);
    }
    
    @Test
    public void testwhoIsAliveSingleElement() {
    	Rendezvous r = new RendezvousImpl();
    	r.iAmAlive("OnlyElement");
    	Assert.assertEquals(1, r.whoIsAlive().size());
    	Assert.assertEquals("OnlyElement", r.whoIsAlive().get(0));
    }
    
    @Test
    public void testwhoIsAliveManyElements() {
    	Rendezvous r = new RendezvousImpl();
    	for(int i = 0; i < 10; i++) {
    		r.iAmAlive("Element" + (i+1));
    	}
    	
    	for(int i = 0; i < 10; i++) {
    		Assert.assertEquals("Element" + (i+1), r.whoIsAlive().get(i)); 
    	}
    }
}
