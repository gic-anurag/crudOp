package com.fadv.cspi.service;

import org.springframework.stereotype.Service;

import com.fadv.cspi.exception.ServiceException;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public interface QualificationLevelMasterService {

	


	Object getQualificationLevel(JsonNode qualificationNode) throws ServiceException;


	
	
}
