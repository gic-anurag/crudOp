
package com.example.demo.riposioutry;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.model.Channels;

public interface ChannelRipo extends MongoRepository<Channels, Integer>{

	
}
