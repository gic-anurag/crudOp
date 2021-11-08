package com.gic.fadv.verification.bulk.pojo;

import com.fasterxml.jackson.databind.node.ArrayNode;

import lombok.Data;

@Data
public class BulkTemplateHeadersPOJO {

	Long bulkTemplateHeadersId;
	
	String templateName;

	ArrayNode templateHeader;

	String status;

	String templateDescription;
	
	String componentName;
}
