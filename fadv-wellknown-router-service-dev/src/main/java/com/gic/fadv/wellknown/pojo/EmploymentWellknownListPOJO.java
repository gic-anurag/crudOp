package com.gic.fadv.wellknown.pojo;

import lombok.Data;

@Data
public class EmploymentWellknownListPOJO {

	//private long id;
	//private Long orgId;
	private String companyName;
	private String companyAkaName;
	private String identifiedDate;
	private String wellKnownCompanyName;
	private String category;
	private String reason;
	
	public EmploymentWellknownListPOJO() {
		super();
	}
	
	
}
