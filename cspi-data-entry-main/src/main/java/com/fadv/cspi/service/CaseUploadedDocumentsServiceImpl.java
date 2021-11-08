package com.fadv.cspi.service;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fadv.cspi.entities.CaseDetails;
import com.fadv.cspi.entities.CaseUploadedDocuments;
import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.repository.CaseUploadedDocumentsRepository;
import com.fadv.cspi.response.pojo.CaseUploadedDocumentsResponsePOJO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CaseUploadedDocumentsServiceImpl implements CaseUploadedDocumentsService {

	private static final String ERROR_CODE_404 = "ERROR_CODE_404";

	@Autowired
	private CaseUploadedDocumentsRepository caseUploadedDocumentsRepository;

	@Autowired
	private CaseDetailsService caseDetailsService;

	private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
			false);

	@Override
	public CaseUploadedDocuments findByCaseDetailsId(Long caseDetailsId) throws ServiceException {

		CaseDetails caseDetails = caseDetailsService.findByCaseDetailsId(caseDetailsId);
		List<CaseUploadedDocuments> caseUploadedDocuments = caseUploadedDocumentsRepository
				.findByUploadTypeAndCaseDetails("converted", caseDetails);

		if (CollectionUtils.isNotEmpty(caseUploadedDocuments)) {
			return caseUploadedDocuments.get(0);
		}
		throw new ServiceException("Case Uploaded Documents not found for given case id", ERROR_CODE_404);
	}

	@Override
	public CaseUploadedDocumentsResponsePOJO getCaseUploadedDocumentsByCaseDetailsId(Long caseDetailsId)
			throws ServiceException {
		CaseUploadedDocuments caseUploadedDocuments = findByCaseDetailsId(caseDetailsId);

		CaseUploadedDocumentsResponsePOJO caseUploadedDocumentsResponsePOJO = mapper.convertValue(caseUploadedDocuments,
				CaseUploadedDocumentsResponsePOJO.class);

		CaseDetails caseDetails = caseUploadedDocuments.getCaseDetails();

		if (caseDetails != null) {
			caseUploadedDocumentsResponsePOJO.setCaseDetailsId(caseDetails.getCaseDetailsId());
		}
		return caseUploadedDocumentsResponsePOJO;
	}
}
