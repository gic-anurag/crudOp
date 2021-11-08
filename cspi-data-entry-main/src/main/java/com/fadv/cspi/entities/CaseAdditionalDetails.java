package com.fadv.cspi.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@JsonInclude(value = Include.NON_NULL)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class CaseAdditionalDetails extends com.fadv.cspi.models.AuditModel {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private long caseAdditionalDetailsId;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", nullable = true)
	private JsonNode address;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", nullable = true)
	private JsonNode contactNumber;

	private boolean singleDataEntry;

	private String akaMiddleName;

	private String akaLocalName;

	private String genderId;

	private String confidentialCase;

	private String authLetter;

	private String remarks;

	private String pointContactId;

	private String locationId;

	private String priorityId;

	private String typeOfCheckId;

	private String modeReceiptId;

	private String clientReference;

	private String localName;

	private String akaFirstName;

	private String akaLastName;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "case_details_id", nullable = false)
	private CaseDetails caseDetails;

	@JsonProperty(access = Access.WRITE_ONLY)
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean active;
}
