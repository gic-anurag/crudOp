package com.fadv.cspi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fadv.cspi.service.QualificationMasterService;
import com.fasterxml.jackson.databind.JsonNode;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/india")
public class QualificationMasterController {

	private static final String SUCCESS_CODE_200 = "SUCCESS_CODE_200";
	private static final String RECORD_FOUND = "Record found";

	@Autowired
	private QualificationMasterService qualificationMasterService;



	@PostMapping(path = "/data/api/data/qualification_m", produces = "application/json", consumes = "application/json")
	public ResponseEntity<com.fadv.cspi.pojo.ResponseStatusPOJO> getQualificationM(@RequestBody JsonNode qualificationMNode) {
		try {
			return new ResponseEntity<>(new com.fadv.cspi.pojo.ResponseStatusPOJO(true, RECORD_FOUND, SUCCESS_CODE_200,
					qualificationMasterService.getQualificationMaster(qualificationMNode)), HttpStatus.OK);
		} catch (com.fadv.cspi.exception.ServiceException e1) {
			return new ResponseEntity<>(new com.fadv.cspi.pojo.ResponseStatusPOJO(false, e1.getMessage(), e1.getMessageId()),
					HttpStatus.OK);
		} catch (Exception e2) {
			return new ResponseEntity<>(new com.fadv.cspi.pojo.ResponseStatusPOJO(false, e2.getMessage()), HttpStatus.OK);
		}
	}













}