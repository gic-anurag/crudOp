package com.gic.fadv.verification.attempts.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gic.fadv.verification.attempts.model.AttemptUploadDocument;

@Service
public interface FileUploadService {
	public HttpStatus uploadFile(MultipartFile file, AttemptUploadDocument attemptUploadDocument);

	ResponseEntity<InputStreamResource> downloadUploadedFile(Long id);
}
