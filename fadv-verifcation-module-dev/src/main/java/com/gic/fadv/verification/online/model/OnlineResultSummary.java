package com.gic.fadv.verification.online.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class OnlineResultSummary {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long onlineSummaryId;
	private String checkId;
	private String caseNumber;
	private String apiName;
	private String source;
	private String summary;
	private String headline;
	private String content;
	private String reason;
	private String remarks;
	private Date createdDate;
	private Date updatedDate;
}
