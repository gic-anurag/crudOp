package com.gic.fadv.verification.spoc.pojo;

import com.gic.fadv.verification.attempts.model.CaseSpecificInfo;

import lombok.Data;

@Data
public class CutomSpocDetailsPOJO {
	private String firstName;
	private String lastName;
	private String clientName;
	private CaseSpecificInfo caseSpecificInfo;
}
