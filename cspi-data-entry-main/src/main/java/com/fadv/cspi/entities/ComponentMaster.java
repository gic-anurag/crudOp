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
public class ComponentMaster extends AuditModel {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private long componentMasterId;

	@Column(nullable = true)
	private String componentMasterMongoId;

	@Column(nullable = false)
	private String componentName;

	@Column(nullable = true, columnDefinition = "varchar(255) default ''")
	private String componentDesc;

	@Column(nullable = true, columnDefinition = "varchar(255) default ''")
	private String dataSource;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isDatabaseComponent = false;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isCloneable = false;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean derived = false;

	@JsonProperty(access = Access.WRITE_ONLY)
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean active;
}
