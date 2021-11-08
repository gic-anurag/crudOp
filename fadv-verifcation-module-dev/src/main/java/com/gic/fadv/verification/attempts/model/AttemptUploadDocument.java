package com.gic.fadv.verification.attempts.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import lombok.Data;

@Entity
@Data
public class AttemptUploadDocument {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long uploadId;
	private Long requestId;
	private String checkid;
	private String fileName;
	private String documentPath;
	private Date createDate=new Date();
	private Long userid;
	private Long componentId;
	private Long componentDocumentid;
	
	@OneToOne
	@JoinColumn(name = "componentDocumentid", insertable = false, updatable = false)
	private ComponentDocumentType componentDocumentType;
	private String status;
}
