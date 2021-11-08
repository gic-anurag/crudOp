package com.fadv.cspi.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fadv.cspi.controller.interfaces.CaseDetailsResponseInterface;
import com.fadv.cspi.entities.CaseDetails;
import com.fadv.cspi.entities.SubjectDetailMaster;
import com.fadv.cspi.entities.SubjectTypeMaster;
import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.pojo.CaseSearchCriteriaPOJO;
import com.fadv.cspi.repository.CaseDetailsRepository;
import com.fadv.cspi.response.pojo.CaseDetailsResponsePOJO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CaseDetailsServiceImpl implements CaseDetailsService {

	private static final String ERROR_CODE_404 = "ERROR_CODE_404";

	@Autowired
	private CaseDetailsRepository caseDetailsRepository;

	private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
			false);

	@Override
	public CaseDetails findByCaseDetailsId(Long caseId) throws ServiceException {

		Optional<CaseDetails> caseDetailsOptional = caseDetailsRepository.findById(caseId);
		if (caseDetailsOptional.isPresent()) {
			return caseDetailsOptional.get();
		}

		throw new ServiceException("Case Details not found for given case id", ERROR_CODE_404);
	}

	@Override
	public CaseDetailsResponsePOJO getCaseDetailsByCaseDetailsId(Long caseId) throws ServiceException {

		CaseDetails caseDetails = findByCaseDetailsId(caseId);
		CaseDetailsResponsePOJO caseDetailsResponsePOJO = mapper.convertValue(caseDetails,
				CaseDetailsResponsePOJO.class);
		SubjectDetailMaster subjectDetailMaster = caseDetails.getSubjectDetailMaster();
		SubjectTypeMaster subjectTypeMaster = caseDetails.getSubjectTypeMaster();
		if (subjectDetailMaster != null) {
			caseDetailsResponsePOJO.setSubjectDetailMasterId(subjectDetailMaster.getSubjectDetailMasterId());
		}
		if (subjectTypeMaster != null) {
			caseDetailsResponsePOJO.setSubjectTypeMasterId(subjectTypeMaster.getSubjectTypeMasterId());
		}
		return caseDetailsResponsePOJO;
	}

	@Override
	public List<CaseDetailsResponseInterface> getCaseDetailsUsingFilters(
			CaseSearchCriteriaPOJO caseSearchCriteriaPOJO) {

		String caseNo = caseSearchCriteriaPOJO.getCaseNo() != null ? caseSearchCriteriaPOJO.getCaseNo() : "";
		if (StringUtils.isNotEmpty(caseNo)) {
			return caseDetailsRepository.getCaseDetailsByCaseNo(caseNo);
		} else {
			Date startDate = caseSearchCriteriaPOJO.getStartDate() != null ? caseSearchCriteriaPOJO.getEndDate()
					: new Date();
			Date endDate = caseSearchCriteriaPOJO.getEndDate() != null ? caseSearchCriteriaPOJO.getEndDate()
					: new Date();
			String crnNo = caseSearchCriteriaPOJO.getCrNo() != null ? caseSearchCriteriaPOJO.getCrNo() : "";
			String caseCreationStatus = caseSearchCriteriaPOJO.getCaseCreationStatus() != null
					? caseSearchCriteriaPOJO.getCaseCreationStatus()
					: "";
			String clientName = caseSearchCriteriaPOJO.getClientName() != null ? caseSearchCriteriaPOJO.getClientName()
					: "";

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String startDateStr = simpleDateFormat.format(startDate);
			String endDateStr = simpleDateFormat.format(endDate);

			return caseDetailsRepository.getCaseDetailsByFilter(startDateStr, endDateStr, clientName, crnNo,
					caseCreationStatus);
		}
	}
}
