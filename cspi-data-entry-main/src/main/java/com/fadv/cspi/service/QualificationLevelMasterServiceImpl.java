package com.fadv.cspi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.repository.QualificationLevelMasterRepository;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class QualificationLevelMasterServiceImpl implements QualificationLevelMasterService {

	@Autowired
	public QualificationLevelMasterRepository qualificationLevelMasterRepository;

	@Override
	public Object getQualificationLevel(JsonNode qualificationNode) throws ServiceException {
		return qualificationLevelMasterRepository.findAll();

		
		/*
		 * String qualification = qualificationMNode.has(QUALIFICATION) ?
		 * qualificationMNode.get(QUALIFICATION).asText() : "";
		 * 
		 * Query query = new Query(); query.fields().exclude(QUALIFICATION);
		 * 
		 * if (StringUtils.isNotEmpty(qualification)) {
		 * query.addCriteria(Criteria.where(QUALIFICATION).is(qualification)); }
		 * 
		 * List<QualificationM> qualificationName = mongoTemplate.find(query,
		 * QualificationM.class); if (CollectionUtils.isNotEmpty(qualificationName)) {
		 * return qualificationName; }
		 * 
		 * throw new ServiceException(RECORD_NOT_FOUND, ERROR_CODE_404);
		 */

	}

}
