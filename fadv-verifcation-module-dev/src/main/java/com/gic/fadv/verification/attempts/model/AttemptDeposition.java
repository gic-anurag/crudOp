package com.gic.fadv.verification.attempts.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;

@Entity
@Data
public class AttemptDeposition {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long depositionId;
    private String depositionName;
    private Long userid;
    private Date createDate=new Date();
    private String status;
 
}
