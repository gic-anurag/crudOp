package com.fadv.cspi.service;

import org.springframework.stereotype.Service;

import com.fadv.cspi.entities.CaseClientDetails;
import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.response.pojo.CaseClientDetailsResponsePOJO;

@Service
public interface CaseClientDetailsService {

	CaseClientDetails findByCaseDetailsId(Long caseId) throws ServiceException;

	CaseClientDetailsResponsePOJO getCaseClientDetailsByCaseDetailsId(Long caseDetailsId) throws ServiceException;

}
