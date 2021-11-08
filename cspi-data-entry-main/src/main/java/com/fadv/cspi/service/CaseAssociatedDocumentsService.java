package com.fadv.cspi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fadv.cspi.exception.ServiceException;

@Service
public interface CaseAssociatedDocumentsService {

	List<String> getAkaNamesByCaseId(Long caseId) throws ServiceException;

}
