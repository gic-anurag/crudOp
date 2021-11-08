package com.fadv.cspi.response.pojo;

import lombok.Data;

@Data
public class CaseClientDetailsResponsePOJO {

	private String caseOrigin;
	private String firstName;
	private String lastName;
	private String middleName;
	private String email;
	private String mobileMandatory;
	private String mobileNumber;
	private String officialEmail;
	private String recentEmployer;
	private String mobileCountryCode;
	private String caseDate;
	private String dob;
	private String startDate;
	private String dobMandatory;
	private String costCode;
	private String contactDate;
	private String contactCurrentEmployer;
	private String candidateAme;
	private String primaryPackage;
	private String srt;
	private String loaSubmitted;
	private String bvfSubmitted;

	private long sbuMasterId;
	private String sbuName;

	private long packageMasterId;
	private String packageName;

	private long subjectTypeMasterId;
	private String subjectTypeName;

	private long subjectDetailMasterId;
	private String subjectName;

	private long emailTemplateMasterId;
	private String emailTemplate;

	private long clientMasterId;
	private String clientName;
	private String clientCode;

	private long caseDetailsId;

	private long emailToMasterId;
	private String emailToName;
}
