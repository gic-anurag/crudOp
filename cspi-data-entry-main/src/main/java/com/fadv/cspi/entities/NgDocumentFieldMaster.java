package com.fadv.cspi.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

import com.fadv.cspi.models.AuditModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@JsonInclude(value = Include.NON_NULL)
public class NgDocumentFieldMaster extends AuditModel {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private long ngDocumentFieldMasterId;

	@Column(nullable = true)
	private String ngDocumentFieldMasterMongoId;

	@Column(nullable = false)
	private String displayName;

	@Column(nullable = false)
	private String fieldKeyName;

	@Column(nullable = false)
	private String uiControl;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean disable = false;

	@Column(nullable = true)
	private Integer primaryKey;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isMandatory = false;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean showInGrid = false;

	@Column(nullable = true)
	private Integer sequenceId = 1;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", nullable = true)
	private ArrayNode possibleValues;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", nullable = true)
	private JsonNode remoteApi;

	@ManyToOne
	@JoinColumn(name = "ng_document_master_id", nullable = false)
	private NgDocumentMaster ngDocumentMaster;

	@JsonProperty(access = Access.WRITE_ONLY)
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean active;

}
