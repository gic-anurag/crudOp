package com.gic.fadv.verification.attempts.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class AttemptStatus {
		
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attemptStatusid;
    private String attemptStatus;
    private String attemptType;
    private Long userid;
    private Date createDate=new Date();
    private String Status;
}
