package com.fadv.cspi.service;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fadv.cspi.entities.CaseClientDetails;
import com.fadv.cspi.entities.CaseDetails;
import com.fadv.cspi.entities.ClientMaster;
import com.fadv.cspi.entities.EmailTemplateMaster;
import com.fadv.cspi.entities.EmailToMaster;
import com.fadv.cspi.entities.PackageMaster;
import com.fadv.cspi.entities.SbuMaster;
import com.fadv.cspi.entities.SubjectDetailMaster;
import com.fadv.cspi.entities.SubjectTypeMaster;
import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.repository.CaseClientDetailsRepository;
import com.fadv.cspi.response.pojo.CaseClientDetailsResponsePOJO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CaseClientDetailsServiceImpl implements CaseClientDetailsService {

	private static final String ERROR_CODE_404 = "ERROR_CODE_404";

	@Autowired
	private CaseClientDetailsRepository caseClientDetailsRepository;

	@Autowired
	private CaseDetailsService caseDetailsService;

	private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
			false);

	@Override
	public CaseClientDetails findByCaseDetailsId(Long caseDetailsId) throws ServiceException {

		CaseDetails caseDetails = caseDetailsService.findByCaseDetailsId(caseDetailsId);
		List<CaseClientDetails> caseClientDetails = caseClientDetailsRepository.findByCaseDetails(caseDetails);

		if (CollectionUtils.isNotEmpty(caseClientDetails)) {
			return caseClientDetails.get(0);
		}
		throw new ServiceException("Case Client Details not found for given case id", ERROR_CODE_404);
	}

	@Override
	public CaseClientDetailsResponsePOJO getCaseClientDetailsByCaseDetailsId(Long caseDetailsId)
			throws ServiceException {
		CaseClientDetails caseClientDetails = findByCaseDetailsId(caseDetailsId);

		CaseClientDetailsResponsePOJO caseClientDetailsResponsePOJO = mapper.convertValue(caseClientDetails,
				CaseClientDetailsResponsePOJO.class);

		CaseDetails caseDetails = caseClientDetails.getCaseDetails();
		SubjectDetailMaster subjectDetailMaster = caseClientDetails.getSubjectDetailMaster();
		SubjectTypeMaster subjectTypeMaster = caseClientDetails.getSubjectTypeMaster();
		ClientMaster clientMaster = caseClientDetails.getClientMaster();
		PackageMaster packageMaster = caseClientDetails.getPackageMaster();
		SbuMaster sbuMaster = caseClientDetails.getSbuMaster();
		EmailTemplateMaster emailTemplateMaster = caseClientDetails.getEmailTemplateMaster();
		EmailToMaster emailToMaster = caseClientDetails.getEmailToMaster();

		if (caseDetails != null) {
			caseClientDetailsResponsePOJO.setCaseDetailsId(caseDetails.getCaseDetailsId());
		}
		if (subjectDetailMaster != null) {
			caseClientDetailsResponsePOJO.setSubjectDetailMasterId(subjectDetailMaster.getSubjectDetailMasterId());
			caseClientDetailsResponsePOJO.setSubjectName(subjectDetailMaster.getSubjectName());
		}
		if (subjectTypeMaster != null) {
			caseClientDetailsResponsePOJO.setSubjectTypeMasterId(subjectTypeMaster.getSubjectTypeMasterId());
			caseClientDetailsResponsePOJO.setSubjectTypeName(subjectTypeMaster.getTypeName());
		}
		if (clientMaster != null) {
			caseClientDetailsResponsePOJO.setClientMasterId(clientMaster.getClientMasterId());
			caseClientDetailsResponsePOJO.setClientName(clientMaster.getClientName());
			caseClientDetailsResponsePOJO.setClientCode(clientMaster.getClientCode());
		}
		if (packageMaster != null) {
			caseClientDetailsResponsePOJO.setPackageMasterId(packageMaster.getPackageMasterId());
			caseClientDetailsResponsePOJO.setPackageName(packageMaster.getPackageName());
		}
		if (sbuMaster != null) {
			caseClientDetailsResponsePOJO.setSbuMasterId(sbuMaster.getSbuMasterId());
			caseClientDetailsResponsePOJO.setSbuName(sbuMaster.getSbuName());
		}
		if (emailTemplateMaster != null) {
			caseClientDetailsResponsePOJO.setEmailTemplateMasterId(emailTemplateMaster.getEmailTemplateMasterId());
			caseClientDetailsResponsePOJO.setEmailTemplate(emailTemplateMaster.getEmailTemplate());
		}
		if (emailToMaster != null) {
			caseClientDetailsResponsePOJO.setEmailToMasterId(emailToMaster.getEmailToMasterId());
			caseClientDetailsResponsePOJO.setEmailToName(emailToMaster.getEmailToName());
		}
		return caseClientDetailsResponsePOJO;
	}
}
