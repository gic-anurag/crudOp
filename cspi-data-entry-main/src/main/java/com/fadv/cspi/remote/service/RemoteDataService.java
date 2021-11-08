package com.fadv.cspi.remote.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.remote.pojo.MrlRulePOJO;
import com.fadv.cspi.remote.pojo.RuleDescriptionPOJO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public interface RemoteDataService {

	List<String> getMrlDocs(JsonNode mrlNode) throws ServiceException;

	List<MrlRulePOJO> getMrlRuleList(Long caseId) throws ServiceException;

	List<RuleDescriptionPOJO> getMrlRuleDescription(Long caseId) throws ServiceException;

	List<RuleDescriptionPOJO> getSlaRuleDescription(Long caseId) throws ServiceException;

	List<ObjectNode> getMrlDocRules(JsonNode mrlNode) throws ServiceException;

}
