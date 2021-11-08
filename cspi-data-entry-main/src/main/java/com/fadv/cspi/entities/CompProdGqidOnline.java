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
public class CompProdGqidOnline extends AuditModel {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private long compProdGqidOnlineId;

	@Column(nullable = true)
	private String compProdGqidOnlineMongoId;

	@ManyToOne
	@JoinColumn(name = "component_master_id", nullable = false)
	private ComponentMaster componentMaster;

	@ManyToOne
	@JoinColumn(name = "product_master_id", nullable = false)
	private ProductMaster productMaster;

	@ManyToOne
	@JoinColumn(name = "gqid_master_id", nullable = false)
	private GqidMaster gqidMaster;

	private String reportComments;

	private String status;

	private String verifiedData;

	private String questionType;

	@JsonProperty(access = Access.WRITE_ONLY)
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean active;
}
