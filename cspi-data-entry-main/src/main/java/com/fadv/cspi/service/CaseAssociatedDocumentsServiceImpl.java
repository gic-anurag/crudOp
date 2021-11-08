package com.fadv.cspi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fadv.cspi.entities.CaseAssociatedDocuments;
import com.fadv.cspi.entities.CaseDetails;
import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.repository.CaseAssociatedDocumentsRepository;

@Service
public class CaseAssociatedDocumentsServiceImpl implements CaseAssociatedDocumentsService {

	@Autowired
	private CaseAssociatedDocumentsRepository caseAssociatedDocumentsRepository;

	@Autowired
	private CaseDetailsService caseDetailsService;

	@Override
	public List<String> getAkaNamesByCaseId(Long caseId) throws ServiceException {

		CaseDetails caseDetails = caseDetailsService.findByCaseDetailsId(caseId);

		List<CaseAssociatedDocuments> caseAssociatedDocuments = caseAssociatedDocumentsRepository
				.findByCaseDetails(caseDetails);
		if (CollectionUtils.isNotEmpty(caseAssociatedDocuments)) {
			return new ArrayList<>(caseAssociatedDocuments.parallelStream()
					.filter(data -> data.getAkaName() != null && StringUtils.isNotEmpty(data.getAkaName()))
					.map(CaseAssociatedDocuments::getAkaName).collect(Collectors.toSet()));
		}
		return new ArrayList<>();
	}
}
