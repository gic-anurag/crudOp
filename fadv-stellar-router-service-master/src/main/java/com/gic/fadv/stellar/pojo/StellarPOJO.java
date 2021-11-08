package com.gic.fadv.stellar.pojo;

import lombok.Data;

@Data
public class StellarPOJO {

	//private long id;
	private Long orgId;
	private String entityName;
	private String akaName;
	private String qualification;
	private String level;
	private String startYear;
	private String endYear;
	
	public StellarPOJO() {
		super();
	}
	
	
}
