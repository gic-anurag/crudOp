package com.gic.fadv.verification.checks.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CaseReferencePOJO {
	public String caseUUID;
	public String clientId;
	public String sbuId;
	public String packageId;
	public String crnNo;
	public String crnCreatedDate;
	public String caseNo;
	public String caseType;
	public String scrnCreatedDate;
}
