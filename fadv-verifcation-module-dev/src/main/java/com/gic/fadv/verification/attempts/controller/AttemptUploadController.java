package com.gic.fadv.verification.attempts.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gic.fadv.verification.attempts.model.AttemptUploadDocument;
import com.gic.fadv.verification.attempts.repository.AttemptUploadDocumentRepository;

import com.gic.fadv.verification.attempts.service.FileUploadService;
import com.gic.fadv.verification.pojo.AttemptUploadDocumentPOJO;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;

import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class AttemptUploadController {

	@Autowired
	private FileUploadService fileUploadService;

	@Autowired
	private AttemptUploadDocumentRepository attemptUploadDocumentRepository;


	private static final Logger logger = LoggerFactory.getLogger(AttemptUploadController.class);

	@PostMapping("/upload-file")
	public HttpStatus upload(@RequestParam("fileName") MultipartFile fileName,
			@RequestParam(name = "userId", required = false) Long userId, @RequestParam("checkId") String checkId,
			@RequestParam(name = "componentId", required = false) Long componentId,
			@RequestParam(name = "componentDocumentid", required = false) Long componentDocumentid,
			@RequestParam("requestId") Long requestId) {

		logger.info("Value from UI : {}, {}, {}, {}, {}, {} ", fileName, userId, checkId, componentId,
				componentDocumentid, requestId);

		AttemptUploadDocument attemptUploadDocument = new AttemptUploadDocument();
		attemptUploadDocument.setCheckid(checkId);
		attemptUploadDocument.setComponentDocumentid(componentDocumentid);
		attemptUploadDocument.setUserid(userId);
		attemptUploadDocument.setComponentId(componentId);
		attemptUploadDocument.setRequestId(requestId);

		logger.info("attemptUploadDocument : {}", attemptUploadDocument);

		return fileUploadService.uploadFile(fileName, attemptUploadDocument);
	}

	@GetMapping("/upload-file/{checkid}")
	public List<AttemptUploadDocumentPOJO> getUploadedFiles(
			@PathVariable(name = "checkid", required = false) String checkId) {
		List<AttemptUploadDocument> attemptUploadDocuments = attemptUploadDocumentRepository.findByCheckid(checkId);
		List<AttemptUploadDocumentPOJO> attemptUploadDocumentPOJOs = new ArrayList<>();

		for (AttemptUploadDocument attemptUploadDocument : attemptUploadDocuments) {
			AttemptUploadDocumentPOJO attemptUploadDocumentPOJO = new AttemptUploadDocumentPOJO();

			attemptUploadDocumentPOJO.setUploadId(attemptUploadDocument.getUploadId());
			attemptUploadDocumentPOJO.setCheckid(attemptUploadDocument.getCheckid());
			attemptUploadDocumentPOJO.setComponentDocumentid(attemptUploadDocument.getComponentDocumentid());
			attemptUploadDocumentPOJO.setCreateDate(attemptUploadDocument.getCreateDate());
			attemptUploadDocumentPOJO
					.setDocumentType(attemptUploadDocument.getComponentDocumentType().getDocumentType());
			attemptUploadDocumentPOJO.setFileName(attemptUploadDocument.getFileName());

			attemptUploadDocumentPOJOs.add(attemptUploadDocumentPOJO);
		}

		return attemptUploadDocumentPOJOs;
	}

	@DeleteMapping("/delete-file/{checkid}")
	public ResponseEntity<Object> deleteByCheckId(@PathVariable(name = "checkid", required = true) String checkId) {
		try {
			attemptUploadDocumentRepository.deleteByCheckId(checkId);
			return ResponseEntity.ok().body("DELETED");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("INTERNAL_SERVER_ERROR");
		}
	}

	@GetMapping("/download-file/{id}")
	public ResponseEntity<InputStreamResource> downloadFileFromServer(
			@Valid @PathVariable(name = "id", required = true) Long id) {
		logger.info("request received to download file with id : {}", id);
		return fileUploadService.downloadUploadedFile(id);
	}
}
