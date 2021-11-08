package com.gic.fadv.verification.mapping.pojo;

import java.util.List;

import lombok.Data;

@Data
public class VendorQuestionaireMappingRequest {

	private String componentName;
	private String type;
	private List<VendorQuestionaireTransactionPojo> dataArray;

}
