package org.fogbowcloud.rendezvous.http;

import java.util.List;

import org.fogbowcloud.rendezvous.core.Rendezvous;
import org.fogbowcloud.rendezvous.core.RendezvousItem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(value = "members")
public class MemebersController {

	private Rendezvous rendezvous;
	
	public MemebersController() {
		// TODO: instantiate rendezvous
		this.rendezvous = null;
	}

	@GetMapping
	public ResponseEntity<List<RendezvousItem>> getMembers() {
		return new ResponseEntity<List<RendezvousItem>>(this.rendezvous.whoIsAlive(), HttpStatus.OK);
	}
}
