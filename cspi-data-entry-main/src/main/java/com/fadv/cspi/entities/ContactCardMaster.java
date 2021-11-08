package com.fadv.cspi.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

import com.fadv.cspi.models.AuditModel;
import com.fadv.cspi.pojo.AdditionalFieldsPOJO;
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
public class ContactCardMaster extends AuditModel {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private long contactCardMasterId;

	@Column(nullable = true)
	private String contactCardMasterMongoId;

	private String akaName;
	private String entityName;
	private String deploymentType;
	private String enterAkaName;
	private String universityEmploymentName;
	private String existingEmploymentCollegeUniversity;
	private String areaLocalityName;
	private String referenceId;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", nullable = true)
	private List<AdditionalFieldsPOJO> additionalFields;

	@ManyToOne
	@JoinColumn(name = "city_master_id", nullable = false)
	private CityMaster cityMaster;

	@ManyToOne
	@JoinColumn(name = "component_master_id", nullable = false)
	private ComponentMaster componentMaster;

	@JsonProperty(access = Access.WRITE_ONLY)
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean active;

}
