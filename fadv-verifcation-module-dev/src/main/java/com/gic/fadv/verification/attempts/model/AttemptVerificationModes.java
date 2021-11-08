package com.gic.fadv.verification.attempts.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;
@Entity
@Data
public class AttemptVerificationModes {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long verificationModeId;
	private String verificationMode;
	private String catergoryMode;
	private long userid;
	private Date createDate=new Date();
    private String Status;  
}
