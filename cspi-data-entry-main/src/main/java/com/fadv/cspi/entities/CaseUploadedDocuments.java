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
public class CaseUploadedDocuments extends AuditModel {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private long caseUploadedDocumentsId;

	private String transactionId;

	@Column(nullable = false)
	private String fileName;

	@Column(columnDefinition = "text")
	private String filePath;

	@Column(columnDefinition = "text")
	private String fileUrl;

	@Column(nullable = false)
	private String originalName;

	private Long fileSize;

	@Column(nullable = false)
	private String fileExtension;

	@Column(nullable = false)
	private String contentType;

	private String errorLog;

	private int pageCount;

	@Column(nullable = true, columnDefinition = "text")
	private String filePathName;

	private String uploadType;

	private String fileStatus;

	private String requestId;

	@ManyToOne
	@JoinColumn(name = "case_details_id", nullable = false)
	private CaseDetails caseDetails;

	@JsonProperty(access = Access.WRITE_ONLY)
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean active;
}
