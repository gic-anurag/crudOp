package com.fadv.cspi.response.pojo;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class CaseDetailsResponsePOJO {

	private long caseDetailsId;
	private String crn;
	private String caseNo;
	private Boolean isDuplicateCase;
	private Boolean isCaseCloned;
	private Boolean isDataEntryCopy;
	private String isCaseSource;
	private int cdeDataEntryStatus;
	private String deType;
	private String caseCreationStatus;
	private String caseType;
	private JsonNode customerFields;
	private JsonNode caseMoreInfo;
	private String packageType;
	private boolean addOnPackageCase;
	private String previousCaseNo;
	private String previousCrn;
	private String priority;
	private String remarks;
	private JsonNode addOnPackages;
	private Boolean fileConverted;
	private Boolean deComplete;
	private String manualScopingCompletedBy;
	private String deCompletedBy;
	private String clonedCaseReferenceId;;
	private Long subjectDetailMasterId;
	private Long subjectTypeMasterId;
}
