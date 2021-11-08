package com.gic.fadv.verification.bulk.pojo;

import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Data;

@Data
public class BulkVerifyPOJO {
	private BulkCaseReferencePOJO caseReference;

	private List<String> checkId;
	
	private ObjectNode batchProcess;
	
	private FileUploadPOJO fileUpload;
}
