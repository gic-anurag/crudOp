package com.gic.fadv.vendor.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
	private String status;
}
