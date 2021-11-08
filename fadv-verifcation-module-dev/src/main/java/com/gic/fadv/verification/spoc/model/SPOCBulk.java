package com.gic.fadv.verification.spoc.model;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


import lombok.Data;

@Entity
@Table(name = "spoc_bulk")
@Data
public class SPOCBulk {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;
	
	private String akaName;
	private String caseReference;
	private String caseNumber;
	private String checkId;
	private String candidateName;
	private String clientName;
	private String flag;
	private Date createdDate;
	private Date updatedDate;
}
