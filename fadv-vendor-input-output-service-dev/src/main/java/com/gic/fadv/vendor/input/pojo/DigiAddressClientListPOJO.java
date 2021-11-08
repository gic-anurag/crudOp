package com.gic.fadv.vendor.input.pojo;

import lombok.Data;

@Data
public class DigiAddressClientListPOJO {

	private String clientCode;
	private String clientName;
	private String sbuName;
	private String packageName;
	private String digitalVerificationIsApproved;
	private String status;
}
