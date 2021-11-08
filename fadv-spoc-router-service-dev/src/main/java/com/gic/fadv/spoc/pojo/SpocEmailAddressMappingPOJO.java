package com.gic.fadv.spoc.pojo;

import lombok.Data;

@Data
public class SpocEmailAddressMappingPOJO {
	
	private String companyName;
	private String thirdCompanyName;
	private String mappingType;
	private String mappingValue;
	private String toEmailAddress;
	private Long templateNo;
	private String ccEmailAddress;
}
