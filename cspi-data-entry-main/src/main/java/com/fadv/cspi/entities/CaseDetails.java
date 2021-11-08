package com.fadv.cspi.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.fadv.cspi.models.AuditModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@JsonInclude(value = Include.NON_NULL)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class CaseDetails extends AuditModel {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private long caseDetailsId;

	private String caseDetailsMongoId;

	private String crn;
	private String caseNo;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isDuplicateCase = false;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isCaseCloned = false;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isDataEntryCopy = false;

	private String isCaseSource;
	private int cdeDataEntryStatus;
	private String deType;
	private String caseCreationStatus;
	private String caseType;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", nullable = true)
	private JsonNode customerFields;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", nullable = true)
	private JsonNode caseMoreInfo;

	private String packageType;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private boolean addOnPackageCase;
	private String previousCaseNo;
	private String previousCrn;
	private String priority;
	private String remarks;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", nullable = true)
	private JsonNode addOnPackages;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean fileConverted = false;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean deComplete = false;

	private String manualScopingCompletedBy;
	private String deCompletedBy;
	private String clonedCaseReferenceId;

	@ManyToOne
	@JoinColumn(name = "subject_detail_master_id")
	private SubjectDetailMaster subjectDetailMaster;

	@ManyToOne
	@JoinColumn(name = "subject_type_master_id")
	private SubjectTypeMaster subjectTypeMaster;

	@JsonProperty(access = Access.WRITE_ONLY)
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean active;
}
