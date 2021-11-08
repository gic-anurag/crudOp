package com.gic.fadv.verification.online.model;

import java.util.List;

import lombok.Data;

@Data
public class FileUpload {
	private List<String> verificationReplyDocument;
	private String directory;
}
