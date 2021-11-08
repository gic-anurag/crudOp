package com.gic.fadv.verification.bulk.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FileUploadPOJO {
	private String directory;

	@JsonProperty("Batch Upload Document")
	private List<Object> batchUploadDocument;
}