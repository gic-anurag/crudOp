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
public class ClientMaster extends AuditModel {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private long clientMasterId;

	@Column(nullable = true)
	private String clientMasterMongoId;

	@Column(nullable = false)
	private String clientName;

	@Column(nullable = false, columnDefinition = "varchar(255) default ''")
	private String clientCode;

	@Column(nullable = true, columnDefinition = "varchar(255) default ''")
	private String remarks;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private boolean customerSpecificFields;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private boolean caseSpecificMoreData;

	@JsonProperty(access = Access.WRITE_ONLY)
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean active;
}
