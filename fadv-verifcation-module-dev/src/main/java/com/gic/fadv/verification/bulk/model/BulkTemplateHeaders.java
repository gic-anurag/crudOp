package com.gic.fadv.verification.bulk.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import lombok.Data;

@Entity
@Data
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class BulkTemplateHeaders {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long bulkTemplateHeadersId;

	@Column(unique = true, nullable = false)
	String templateName;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb")
	ArrayNode templateHeader;

	String status;

	Date createdDate;

	Date updatedDate;

	String templateDescription;
	
	String componentName;
}
