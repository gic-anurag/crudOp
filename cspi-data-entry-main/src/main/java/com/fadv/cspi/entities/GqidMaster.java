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
public class GqidMaster extends AuditModel {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private long gqidMasterId;

	@Column(nullable = true, columnDefinition = "varchar(255) default ''")
	private String gqidMasterMongoId;

	@Column(nullable = false, columnDefinition = "text default ''")
	private String questionName;

	@Column(nullable = false, columnDefinition = "text default ''")
	private String formLabel;

	@Column(nullable = false, columnDefinition = "text default ''")
	private String reportLabel;

	@Column(nullable = false, columnDefinition = "text default ''")
	private String packageQuestion;

	@Column(nullable = true, columnDefinition = "text default ''")
	private String deType;

	@Column(nullable = false, unique = true)
	private Integer globalQuestionId;

	@JsonProperty(access = Access.WRITE_ONLY)
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean active;

}
