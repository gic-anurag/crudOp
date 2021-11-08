package com.gic.fadv.cbvutvi4v.model;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import lombok.Data;

@Data
public class CBVUTVI4V {

	private Long orgId;
	private String companyName;
	private String companyAkaName;
	private String productName;
	private String reason;
	private String remarks;
	private String flag;
	private String component;
	private String location;
		
	public CBVUTVI4V() {
		super();
	}
	
	
}
