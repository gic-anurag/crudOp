package com.gic.fadv.vendor.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Entity
@Data
public class AttemptHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long attemptid;
	private Long requestid;
	private String checkid;
	private String name;
	private String jobTitle;
	private String emailAddress;
	private String faxNumber;
	private String contactDate;
	private String followupDate;
	private Long attemptStatusid;
	private String numberProvided;
	private String attemptDescription;
	private String closureExpectedDate;
	private Long userid;
	private Date createDate = new Date();
	private String sourcePhone;
	private Integer isCurrent;
	private String contactCardName;
	private Long followupId;
	private Long refAttemptId;
	private String executiveSummary;
	private String additionalFieldsTag;
	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", name = "additional_info")
	private JsonNode additionalFields;
	private String l3Status;
	private String l3Response;
}
