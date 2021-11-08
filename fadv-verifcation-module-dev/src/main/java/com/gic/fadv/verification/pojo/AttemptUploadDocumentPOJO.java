package com.gic.fadv.verification.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class AttemptUploadDocumentPOJO {

	private Long uploadId;
	private String fileName;
	private String checkid;
	private Long componentDocumentid;
	private String documentType;
	private Date createDate;
}
