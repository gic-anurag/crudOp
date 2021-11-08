package com.gic.fadv.verification.online.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.databind.node.ArrayNode;

import lombok.Data;

@Entity
@Data
public class OnlineVerificationChecks {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long onlineVerificationCheckId;
	private Long onlineManualVerificationId;
	private String checkId;
	private String apiName;
	private String result;
	private String initialResult;
	private String matchedIdentifiers;
	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", name = "input_file")
	private String inputFile;
	// @Type(type = "jsonb")
	// @Column(columnDefinition = "jsonb",name="output_file")
	private String outputFile;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", name = "output_result")
	private ArrayNode outputResult;

	private String createdDate;
	private String updatedDate;
	private String componentName;
	private String subComponentName;

	private String verifyId;
	private String pendingStatus;
	private String retryNo;
	
	@Column(columnDefinition = "boolean default false")
	private boolean sentToL3;
}
