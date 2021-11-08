package com.gic.fadv.cbvutvi4v.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.persistence.Id;
//import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class VerificationSLA {
	private String clientCode;
	private String clientName;
	@JsonProperty("SBUName") 
	private String sBUName;
	private String packageName;
	@JsonProperty("HTS")
	private String hTS;
	
	public VerificationSLA() {
		super();
	}
	
	
}

