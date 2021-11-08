package com.fadv.cspi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.pojo.ResponseStatusPOJO;
import com.fadv.cspi.service.QualificationLevelMasterService;
import com.fasterxml.jackson.databind.JsonNode;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/india")
public class QualificationLevelMasterController {

	private static final String SUCCESS_CODE_200 = "SUCCESS_CODE_200";
	private static final String RECORD_FOUND = "Record found";
	
	
	@Autowired
	private QualificationLevelMasterService qualifiactionLevelMaster;

	
	@PostMapping(path = "/data/api/data/qualification_level_m", produces = "application/json")
	public ResponseEntity<ResponseStatusPOJO> getQualificationLevel(@RequestBody JsonNode qualificationNode) {
		try {
			return new ResponseEntity<>(new ResponseStatusPOJO(true, RECORD_FOUND, SUCCESS_CODE_200,
					qualifiactionLevelMaster.getQualificationLevel(qualificationNode)), HttpStatus.OK);
		} catch (ServiceException e1) {
			return new ResponseEntity<>(new ResponseStatusPOJO(false, e1.getMessage(), e1.getMessageId()),
					HttpStatus.OK);
		} catch (Exception e2) {
			return new ResponseEntity<>(new ResponseStatusPOJO(false, e2.getMessage()), HttpStatus.OK);
		}
	}

	
	
	
	
	
}









