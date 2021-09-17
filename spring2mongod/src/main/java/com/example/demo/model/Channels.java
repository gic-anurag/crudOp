package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString


@Document(collection="youtube")
public class Channels {
	@Id
	
	private String name;
    private int id;
    private int Subscribers;
    private String genre;
    
    
}
