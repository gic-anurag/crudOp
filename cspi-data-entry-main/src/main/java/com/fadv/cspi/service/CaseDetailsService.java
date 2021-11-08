package com.fadv.cspi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fadv.cspi.controller.interfaces.CaseDetailsResponseInterface;
import com.fadv.cspi.entities.CaseDetails;
import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.pojo.CaseSearchCriteriaPOJO;
import com.fadv.cspi.response.pojo.CaseDetailsResponsePOJO;

@Service
public interface CaseDetailsService {

	CaseDetails findByCaseDetailsId(Long caseDetailsId) throws ServiceException;

	List<CaseDetailsResponseInterface> getCaseDetailsUsingFilters(CaseSearchCriteriaPOJO caseSearchCriteriaPOJO);

	CaseDetailsResponsePOJO getCaseDetailsByCaseDetailsId(Long caseDetailsId) throws ServiceException;

}
