package com.example.demo.resource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.integration.IntegrationProperties.Channel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Channels;
import com.example.demo.riposioutry.ChannelRipo;

@RestController
public class ChannelController {
   
	@Autowired
	private ChannelRipo cr;
	
     @PostMapping("/addChannel")
	public String saveChannel(@RequestBody Channel channel) {
		return "added channel with id : " ;
		
	}
     @GetMapping("/findAllChannel")
     public List<Channels> getChannel(){
    	 return cr.findAll();
     
     public Optional<Channel> getBook(@PathVariable int id){
    	 return cr.findById(id);
    	 
     }
     }
     
	
	
}
