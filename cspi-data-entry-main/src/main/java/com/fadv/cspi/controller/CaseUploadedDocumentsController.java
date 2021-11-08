package com.fadv.cspi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.pojo.ResponseStatusPOJO;
import com.fadv.cspi.service.CaseUploadedDocumentsService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/india/")
public class CaseUploadedDocumentsController {

	private static final String SUCCESS_CODE_200 = "SUCCESS_CODE_200";

	private static final Logger logger = LoggerFactory.getLogger(CaseUploadedDocumentsController.class);

	@Autowired
	private CaseUploadedDocumentsService caseUploadedDocumentsService;

	@GetMapping(path = "case-uploaded-documents/{caseId}", produces = "application/json")
	public ResponseEntity<ResponseStatusPOJO> getCaseAdditionalDetails(@PathVariable(value = "caseId") Long caseId) {
		logger.info("Case id to find : {}", caseId);
		try {
			return new ResponseEntity<>(
					new ResponseStatusPOJO(true, "Record found", SUCCESS_CODE_200,
							caseUploadedDocumentsService.getCaseUploadedDocumentsByCaseDetailsId(caseId)),
					HttpStatus.OK);
		} catch (ServiceException e1) {
			return new ResponseEntity<>(new ResponseStatusPOJO(false, e1.getMessage(), e1.getMessageId()),
					HttpStatus.OK);
		} catch (Exception e2) {
			return new ResponseEntity<>(new ResponseStatusPOJO(false, e2.getMessage()), HttpStatus.OK);
		}
	}
}
