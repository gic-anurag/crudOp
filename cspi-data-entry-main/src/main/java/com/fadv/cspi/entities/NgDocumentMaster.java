package com.fadv.cspi.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
public class NgDocumentMaster extends AuditModel {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private long ngDocumentMasterId;

	@Column(nullable = true)
	private String ngDocumentMasterMongoId;

	@Column(nullable = false)
	private String documentName;

	@Column(nullable = true, columnDefinition = "varchar(255) default ''")
	private String documentDescription;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isSupportingDocument = false;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean enableContactCard = false;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean collection = false;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean componentMapping = false;

	@Column(nullable = true, columnDefinition = "varchar(255) default ''")
	private String groupId;

	@Column(nullable = true, columnDefinition = "varchar(255) default ''")
	private String modelType;

	@Column(nullable = true, columnDefinition = "varchar(255) default ''")
	private String contextId;

	@Column(nullable = true, columnDefinition = "varchar(255) default ''")
	private String contextName;

	@JsonProperty(access = Access.WRITE_ONLY)
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean active;

}
