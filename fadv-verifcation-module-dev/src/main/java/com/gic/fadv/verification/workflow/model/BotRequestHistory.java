package com.gic.fadv.verification.workflow.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import lombok.Data;

@Entity
@Data
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class BotRequestHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long botRequestId;

	private String caseNumber;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", name = "request_body")
	private JsonNode requestBody;

	private String requestStatus;

	private Date createdDate;

	private Date updatedDate;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", name = "response_body")
	private JsonNode responseBody;

	private String emailSent;

	private Date emailDate;

	@Column(columnDefinition = "INTEGER default 0")
	private int retryCount;
}
