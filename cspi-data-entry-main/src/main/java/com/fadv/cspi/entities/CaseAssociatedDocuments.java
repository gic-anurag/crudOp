package com.fadv.cspi.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

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
public class CaseAssociatedDocuments extends AuditModel {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private long caseAssociatedDocumentsId;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private boolean isAssociateAfterCaseReceivedFromSp;

	@Column(nullable = false)
	private String rowId;

	private String parentRowId;

	@Column(nullable = false)
	private int startPage;

	@Column(nullable = false)
	private int endPage;

	@Column(nullable = false, columnDefinition = "text")
	private String filePath;

	@Column(nullable = false)
	private String akaName;

	@ManyToOne
	@JoinColumn(name = "case_details_id", nullable = false)
	private CaseDetails caseDetails;

	@ManyToOne
	@JoinColumn(name = "case_uploaded_documents_id", nullable = false)
	private CaseUploadedDocuments caseUploadedDocuments;

	@ManyToOne
	@JoinColumn(name = "ng_document_master_id", nullable = false)
	private NgDocumentMaster ngDocumentMaster;

	@JsonProperty(access = Access.WRITE_ONLY)
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean active;
}
