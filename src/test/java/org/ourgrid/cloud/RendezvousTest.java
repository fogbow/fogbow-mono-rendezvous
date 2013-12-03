package org.ourgrid.cloud;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.ourgid.cloud.Rendezvous;
import org.ourgid.cloud.RendezvousImpl;

public class RendezvousTest {

    @Test
	public void testImAliveSingleElement() {
    	Rendezvous r = new RendezvousImpl();
    	r.iAmAlive("abc");
    	List<String> valor = r.whoIsAlive();
    	Assert.assertEquals(1, valor.size());
    	Assert.assertEquals("abc", valor);
    	r.iAmAlive("leticia");
    	Assert.assertEquals(2, valor.size());
    	Assert.assertEquals("abc", valor.get(1));
	}
    
    @Test
    public void testImAlive2Elements() {
    	Rendezvous r = new RendezvousImpl();
    	r.iAmAlive("abc");
    	r.iAmAlive("leticia");
    	List<String> valor = r.whoIsAlive();
    	Assert.assertEquals(2, valor.size());
    	Assert.assertEquals("abc", valor.get(0));
    	Assert.assertEquals("leticia", valor.get(1));
    }
    
    @Test
    public void testImAlive10Elements() {
    	Rendezvous r = new RendezvousImpl();
    	List<String> valor = r.whoIsAlive();
    	for(int i = 0; i < 10;i++) {
    		r.iAmAlive("Hola");		
    	}
    	
    	valor = r.whoIsAlive();
    	
    	for(int i = 0; i < 10;i++) {
    	Assert.assertEquals("Hola", valor.get(i));	
    	}
    }
    
    @Test
    public void testImAlive10DifferentElements() {
    	Rendezvous r = new RendezvousImpl();
    	List<String> valor = r.whoIsAlive();
    	String s = "0";
    	for(int i = 0; i < 10;i++) {
			r.iAmAlive(s);
    		s = s + "0";
    		valor = r.whoIsAlive();
    		Assert.assertEquals(s, valor.get(i));	
    	}
    }
    
    @Test
    public void testwhoIsAliveNull()
    {
    	Rendezvous r = new RendezvousImpl();
    	List<String> valor = r.whoIsAlive();
    	Assert.assertEquals(null,valor);
    }
    
    @Test
    public void testwhoIsAlive1Element() {
    	Rendezvous r = new RendezvousImpl();
    	r.iAmAlive("OnlyElement");
    	Assert.assertEquals(1, r.whoIsAlive().size());
    	Assert.assertEquals("OnlyElement", r.whoIsAlive().get(0));
    }
    
    public void testwhoIsAlive2Elements() {
    	Rendezvous r = new RendezvousImpl();
    	r.iAmAlive("1stElement");
    	r.iAmAlive("2ndElement");
    	Assert.assertEquals(2, r.whoIsAlive().size());
    	Assert.assertEquals("1stElement", r.whoIsAlive().get(0));
    	Assert.assertEquals("2ndElement", r.whoIsAlive().get(1));
    }
    
    
}
