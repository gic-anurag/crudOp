package com.fadv.cspi.service;

import org.springframework.stereotype.Service;

import com.fadv.cspi.entities.CaseAdditionalDetails;
import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.response.pojo.CaseAdditionalDetailsResponsePOJO;

@Service
public interface CaseAdditionalDetailsService {

	CaseAdditionalDetails findByCaseDetailsId(Long caseId) throws ServiceException;

	CaseAdditionalDetailsResponsePOJO getCaseAdditionalDetailsByCaseDetailsId(Long caseDetailsId)
			throws ServiceException;

}
