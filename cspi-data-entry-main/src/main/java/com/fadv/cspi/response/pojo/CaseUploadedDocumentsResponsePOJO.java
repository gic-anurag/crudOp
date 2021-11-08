package com.fadv.cspi.response.pojo;

import lombok.Data;

@Data
public class CaseUploadedDocumentsResponsePOJO {

	private long caseUploadedDocumentsId;

	private String transactionId;

	private String fileName;

	private String filePath;

	private String fileUrl;

	private String originalName;

	private Long fileSize;

	private String fileExtension;

	private String contentType;

	private String errorLog;

	private int pageCount;

	private String filePathName;

	private String uploadType;

	private String fileStatus;

	private String requestId;

	private long caseDetailsId;
}
