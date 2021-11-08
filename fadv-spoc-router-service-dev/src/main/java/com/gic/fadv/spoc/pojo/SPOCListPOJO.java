package com.gic.fadv.spoc.pojo;

import lombok.Data;

@Data
public class SPOCListPOJO {

	//private long id;
	private Long orgId;
	private String companyName;
	private String companyAkaName;
	private String cityName;
	private String stateName;
	private String countryName;
	
	public SPOCListPOJO() {
		super();
	}
	
	
}
