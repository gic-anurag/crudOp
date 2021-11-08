package com.fadv.cspi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.repository.QualificationMasterRepository;
import com.fasterxml.jackson.databind.JsonNode;;

@Service
public class QualificationMasterServiceImpl implements QualificationMasterService {

	@Autowired
	public QualificationMasterRepository qualificationMasterRepository;

	@Override
	public Object getQualificationMaster(JsonNode qualificationMNode) throws ServiceException {

		return qualificationMasterRepository.findAll();

	}

}
