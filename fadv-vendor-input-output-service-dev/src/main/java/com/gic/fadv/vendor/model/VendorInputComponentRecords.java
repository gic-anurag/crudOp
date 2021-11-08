package com.gic.fadv.vendor.model;

import lombok.Data;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

@Entity
@Data
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Table(name = "vendor_input_component_records")
public class VendorInputComponentRecords {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String caseNumber;
	private String componentName;
	private String subComponentName;
	private String checkId;
	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", name = "check_record")
	private JsonNode checkRecord;
	private Date createdDate = new Date();
	// Need to Store Meta Data Also
}
