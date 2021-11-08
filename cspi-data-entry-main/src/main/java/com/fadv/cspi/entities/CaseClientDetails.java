package com.fadv.cspi.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.fadv.cspi.models.AuditModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@JsonInclude(value = Include.NON_NULL)
public class CaseClientDetails extends AuditModel {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private long caseClientDetailsId;

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

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "sbu_master_id")
	private SbuMaster sbuMaster;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "package_master_id")
	private PackageMaster packageMaster;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "subject_type_master_id")
	private SubjectTypeMaster subjectTypeMaster;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "subject_detail_master_id")
	private SubjectDetailMaster subjectDetailMaster;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "email_template_master_id")
	private EmailTemplateMaster emailTemplateMaster;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "client_master_id")
	private ClientMaster clientMaster;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "case_details_id", nullable = false)
	private CaseDetails caseDetails;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "email_to_master_id")
	private EmailToMaster emailToMaster;

	@JsonProperty(access = Access.WRITE_ONLY)
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean active;
}
