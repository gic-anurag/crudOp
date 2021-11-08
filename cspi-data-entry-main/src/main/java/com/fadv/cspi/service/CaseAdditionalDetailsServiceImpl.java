package com.fadv.cspi.service;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fadv.cspi.entities.CaseAdditionalDetails;
import com.fadv.cspi.entities.CaseDetails;
import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.repository.CaseAdditionalDetailsRepository;
import com.fadv.cspi.response.pojo.CaseAdditionalDetailsResponsePOJO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CaseAdditionalDetailsServiceImpl implements CaseAdditionalDetailsService {

	private static final String ERROR_CODE_404 = "ERROR_CODE_404";

	@Autowired
	private CaseAdditionalDetailsRepository caseAdditionalDetailsRepository;

	@Autowired
	private CaseDetailsService caseDetailsService;

	private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
			false);

	@Override
	public CaseAdditionalDetails findByCaseDetailsId(Long caseDetailsId) throws ServiceException {

		CaseDetails caseDetails = caseDetailsService.findByCaseDetailsId(caseDetailsId);
		List<CaseAdditionalDetails> caseAdditionalDetails = caseAdditionalDetailsRepository
				.findByCaseDetails(caseDetails);

		if (CollectionUtils.isNotEmpty(caseAdditionalDetails)) {
			return caseAdditionalDetails.get(0);
		}
		throw new ServiceException("Case Additional Details not found for given case id", ERROR_CODE_404);
	}

	@Override
	public CaseAdditionalDetailsResponsePOJO getCaseAdditionalDetailsByCaseDetailsId(Long caseDetailsId)
			throws ServiceException {
		CaseAdditionalDetails caseAdditionalDetails = findByCaseDetailsId(caseDetailsId);

		CaseAdditionalDetailsResponsePOJO caseAdditionalDetailsResponsePOJO = mapper.convertValue(caseAdditionalDetails,
				CaseAdditionalDetailsResponsePOJO.class);

		CaseDetails caseDetails = caseAdditionalDetails.getCaseDetails();

		if (caseDetails != null) {
			caseAdditionalDetailsResponsePOJO.setCaseDetailsId(caseDetails.getCaseDetailsId());
		}
		return caseAdditionalDetailsResponsePOJO;
	}
}
