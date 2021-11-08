package com.fadv.cspi.service;

import org.springframework.stereotype.Service;

import com.fadv.cspi.entities.CaseUploadedDocuments;
import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.response.pojo.CaseUploadedDocumentsResponsePOJO;

@Service
public interface CaseUploadedDocumentsService {

	CaseUploadedDocuments findByCaseDetailsId(Long caseId) throws ServiceException;

	CaseUploadedDocumentsResponsePOJO getCaseUploadedDocumentsByCaseDetailsId(Long caseDetailsId)
			throws ServiceException;

}
