package com.gic.fadv.verification.online.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class ManupatraOutput {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long manupatraOutputId;
	
	private String checkId;
	
	private String title;
	
	private String result;
	
	private Date createdDate;
	
	private Date updatedDate;
}
