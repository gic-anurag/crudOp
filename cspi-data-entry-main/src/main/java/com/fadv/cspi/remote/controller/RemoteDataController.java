package com.fadv.cspi.remote.controller;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.pojo.ResponseStatusPOJO;
import com.fadv.cspi.remote.service.RemoteDataService;
import com.fasterxml.jackson.databind.JsonNode;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/india/")
public class RemoteDataController {

	private static final String SUCCESS_CODE_200 = "SUCCESS_CODE_200";
	private static final String RECORD_FOUND = "Record found";

	@Autowired
	private RemoteDataService remoteDataService;

	@PostMapping(path = "/mrl-docs", produces = "application/json", consumes = "application/json")
	public ResponseEntity<ResponseStatusPOJO> getMrlDocs(@RequestBody JsonNode mrlNode) {
		try {
			return new ResponseEntity<>(
					new ResponseStatusPOJO(true, RECORD_FOUND, SUCCESS_CODE_200, remoteDataService.getMrlDocs(mrlNode)),
					HttpStatus.OK);
		} catch (ServiceException e1) {
			return new ResponseEntity<>(new ResponseStatusPOJO(false, e1.getMessage(), e1.getMessageId()),
					HttpStatus.OK);
		} catch (Exception e2) {
			return new ResponseEntity<>(new ResponseStatusPOJO(false, e2.getMessage()), HttpStatus.OK);
		}
	}

	@GetMapping(path = "/mrl-docs-list/{caseId}", produces = "application/json")
	public ResponseEntity<ResponseStatusPOJO> getMrlDocs(@NotEmpty @PathVariable("caseId") Long caseId) {

		try {
			remoteDataService.getMrlRuleList(caseId);
			return new ResponseEntity<>(new ResponseStatusPOJO(true, RECORD_FOUND, SUCCESS_CODE_200,
					remoteDataService.getMrlRuleList(caseId)), HttpStatus.OK);
		} catch (ServiceException e1) {
			return new ResponseEntity<>(new ResponseStatusPOJO(false, e1.getMessage(), e1.getMessageId()),
					HttpStatus.OK);
		} catch (Exception e2) {
			return new ResponseEntity<>(new ResponseStatusPOJO(false, e2.getMessage()), HttpStatus.OK);
		}
	}

	@GetMapping(path = "/mrl-rule-list/{caseId}", produces = "application/json")
	public ResponseEntity<ResponseStatusPOJO> getMrlRules(@NotEmpty @PathVariable("caseId") Long caseId) {

		try {
			remoteDataService.getMrlRuleList(caseId);
			return new ResponseEntity<>(new ResponseStatusPOJO(true, RECORD_FOUND, SUCCESS_CODE_200,
					remoteDataService.getMrlRuleDescription(caseId)), HttpStatus.OK);
		} catch (ServiceException e1) {
			return new ResponseEntity<>(new ResponseStatusPOJO(false, e1.getMessage(), e1.getMessageId()),
					HttpStatus.OK);
		} catch (Exception e2) {
			return new ResponseEntity<>(new ResponseStatusPOJO(false, e2.getMessage()), HttpStatus.OK);
		}
	}

	@GetMapping(path = "/sla-rule-list/{caseId}", produces = "application/json")
	public ResponseEntity<ResponseStatusPOJO> getSlaRules(@NotEmpty @PathVariable("caseId") Long caseId) {

		try {
			remoteDataService.getMrlRuleList(caseId);
			return new ResponseEntity<>(new ResponseStatusPOJO(true, RECORD_FOUND, SUCCESS_CODE_200,
					remoteDataService.getSlaRuleDescription(caseId)), HttpStatus.OK);
		} catch (ServiceException e1) {
			return new ResponseEntity<>(new ResponseStatusPOJO(false, e1.getMessage(), e1.getMessageId()),
					HttpStatus.OK);
		} catch (Exception e2) {
			return new ResponseEntity<>(new ResponseStatusPOJO(false, e2.getMessage()), HttpStatus.OK);
		}
	}

	@PostMapping(path = "/mrl-docs-rule-list", produces = "application/json", consumes = "application/json")
	public ResponseEntity<ResponseStatusPOJO> getMrlDocRules(@RequestBody JsonNode mrlNode) {

		try {
			return new ResponseEntity<>(new ResponseStatusPOJO(true, RECORD_FOUND, SUCCESS_CODE_200,
					remoteDataService.getMrlDocRules(mrlNode)), HttpStatus.OK);
		} catch (ServiceException e1) {
			return new ResponseEntity<>(new ResponseStatusPOJO(false, e1.getMessage(), e1.getMessageId()),
					HttpStatus.OK);
		} catch (Exception e2) {
			return new ResponseEntity<>(new ResponseStatusPOJO(false, e2.getMessage()), HttpStatus.OK);
		}
	}
}
