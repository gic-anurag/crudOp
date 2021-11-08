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
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@JsonInclude(value = Include.NON_NULL)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class CaseDataEntry extends AuditModel {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private long caseDataEntryId;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", nullable = true)
	private JsonNode dataEntryData;

	@Column(nullable = false)
	private String rowId;

	private String parentRowId;

	@ManyToOne
	@JoinColumn(name = "case_details_id", nullable = false)
	private CaseDetails caseDetails;

	@ManyToOne
	@JoinColumn(name = "case_associated_documents_id")
	private CaseAssociatedDocuments caseAssociatedDocuments;

	@ManyToOne
	@JoinColumn(name = "sbu_master_id")
	private SbuMaster sbuMaster;

	@ManyToOne
	@JoinColumn(name = "client_master_id")
	private ClientMaster clientMaster;

	@ManyToOne
	@JoinColumn(name = "package_master_id")
	private PackageMaster packageMaster;

	@ManyToOne
	@JoinColumn(name = "ng_document_master_d", nullable = false)
	private NgDocumentMaster ngDocumentMaster;
}
