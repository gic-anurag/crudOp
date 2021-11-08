package com.gic.fadv.verification.attempts.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.fadv.verification.attempts.interfaces.CaseSpecificDetailsInterface;
import com.gic.fadv.verification.attempts.repository.CaseSpecificInfoRepository;
import com.gic.fadv.verification.keycloak.model.FadvUsers;
import com.gic.fadv.verification.keycloak.repository.FadvUsersRepository;
import com.gic.fadv.verification.pojo.CaseSpecificInfoPOJO;

@Service
public class CaseSpecificRecordDetailServiceImpl implements CaseSpecificRecordDetailService {

	@Autowired
	private CaseSpecificInfoRepository caseSpecificInfoRepository;
	
	@Autowired
	private FadvUsersRepository fadvUsersRepository;
	
	private static final Logger logger = LoggerFactory.getLogger(CaseSpecificRecordDetailServiceImpl.class);
	
	@Override
	public List<CaseSpecificInfoPOJO> getCaseSpecificRecords(ObjectMapper mapper, JsonNode requestBody) {
		requestBody = requestBody != null ? requestBody : mapper.createObjectNode();
		String clientName = requestBody.has("clientName") ? requestBody.get("clientName").asText() : "";
		String candidateName = requestBody.has("candidateName") ? requestBody.get("candidateName").asText() : "";
		String crnNo = requestBody.has("CRNNumber") ? requestBody.get("CRNNumber").asText() : "";
		String fromDate = requestBody.has("fromDate") ? requestBody.get("fromDate").asText() : "";
		String toDate = requestBody.has("toDate") ? requestBody.get("toDate").asText() : "";
		String checkId = requestBody.has("checkId") ? requestBody.get("checkId").asText() : "";
		String productName = requestBody.has("productName") ? requestBody.get("productName").asText() : "";
		String componentName = requestBody.has("componentName") ? requestBody.get("componentName").asText() : "";

		candidateName = StringUtils.isEmpty(candidateName) ? candidateName : "%" + candidateName + "%";
		clientName = StringUtils.isEmpty(clientName) ? clientName : "%" + clientName + "%";
		String productNameSearch = StringUtils.isEmpty(productName) ? productName : "%" + productName + "%";
		String componentNameSearch = StringUtils.isEmpty(componentName) ? componentName : "%" + componentName + "%";

		Long userId = getUserId(requestBody);
		
		 //use this configuration for ignoring user check (specific to production)
			
			  if (userId == null) { userId = (long) 0; }
			 
		//===================================Change upto here=======================
		

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		fromDate = StringUtils.isEmpty(fromDate) ? simpleDateFormat.format(new Date()) : fromDate;
		toDate = StringUtils.isEmpty(toDate) ? fromDate : toDate;
		if (!StringUtils.isEmpty(fromDate) && !StringUtils.isEmpty(toDate) && userId != null) {
			List<CaseSpecificDetailsInterface> caseSpecificInfoList = caseSpecificInfoRepository
					.getCaseDetailsUsingFilters(fromDate, toDate, clientName, crnNo, candidateName, checkId,
							productNameSearch, componentNameSearch, userId);
			try {
				return makeAndReturnCaseResponseJson(mapper, caseSpecificInfoList);
			} catch (JsonProcessingException e) {
				logger.error("Exception while mapping records : {}", e.getMessage());
			}
		}
		return new ArrayList<>();
	}

	private Long getUserId(JsonNode requestBody) {
		Long userId = requestBody.has("userId") ? requestBody.get("userId").asLong() : 0;
		Optional<FadvUsers> fadvUserOpt = fadvUsersRepository.findById(userId);
		if (fadvUserOpt.isPresent()) {
			String role = fadvUserOpt.get().getUserRole();
			if (StringUtils.equalsIgnoreCase(role, "Super Admin") || StringUtils.equalsIgnoreCase(role, "Semi Admin")) {
				return (long) 0;
			} else {
				return userId;
			}
		}
		return null;
	}

	private List<CaseSpecificInfoPOJO> makeAndReturnCaseResponseJson(ObjectMapper mapper,
			List<CaseSpecificDetailsInterface> caseSpecificInfoList) throws JsonProcessingException {
		List<CaseSpecificInfoPOJO> caseSpecificInfoPOJOList = new ArrayList<>();
		for (CaseSpecificDetailsInterface caseSpecificDetailsInterface : caseSpecificInfoList) {
			CaseSpecificInfoPOJO caseSpecificInfoPOJO = new CaseSpecificInfoPOJO();
			caseSpecificInfoPOJO.setCaseReferenceNumber(caseSpecificDetailsInterface.getCaseReferenceNumber());
			caseSpecificInfoPOJO.setClientName(caseSpecificDetailsInterface.getClientName());
			caseSpecificInfoPOJO.setCandidateName(caseSpecificDetailsInterface.getCandidateName());
			caseSpecificInfoPOJO.setSbuName(caseSpecificDetailsInterface.getSbuName());
			caseSpecificInfoPOJO.setPackageName(caseSpecificDetailsInterface.getPackageName());

			caseSpecificInfoPOJO.setCheckId(caseSpecificDetailsInterface.getCheckId());
			caseSpecificInfoPOJO.setComponentName(caseSpecificDetailsInterface.getComponentName());
			caseSpecificInfoPOJO.setProductName(caseSpecificDetailsInterface.getProductName());
			caseSpecificInfoPOJO.setStatusofCheck(caseSpecificDetailsInterface.getStatusOfCheck());
			caseSpecificInfoPOJO.setCheckCreatedDate(caseSpecificDetailsInterface.getCheckCreatedDate());
			caseSpecificInfoPOJO.setCheckDueDate(caseSpecificDetailsInterface.getCheckDueDate());
			caseSpecificInfoPOJO.setCheckTAT(caseSpecificDetailsInterface.getCheckTat());
			caseSpecificInfoPOJO.setRequestId(caseSpecificDetailsInterface.getRequestId());

			JsonNode caseRecordJsonNode = mapper.readTree(caseSpecificDetailsInterface.getComponentRecord() != null
					? caseSpecificDetailsInterface.getComponentRecord()
					: "{}");
			caseRecordJsonNode = caseRecordJsonNode != null ? caseRecordJsonNode : mapper.createObjectNode();

			String akaName = caseRecordJsonNode.has("Aka Name") ? caseRecordJsonNode.get("Aka Name").asText() : "";
			String thirdPartyCompany = caseRecordJsonNode.has("Third party Company(city)")
					? caseRecordJsonNode.get("Third party Company(city)").asText()
					: "";
			String city = caseRecordJsonNode.has("City") ? caseRecordJsonNode.get("City").asText() : "";

			caseSpecificInfoPOJO.setAkaName(akaName);
			caseSpecificInfoPOJO.setFunctionalEntityName(akaName);

			caseSpecificInfoPOJO.setEntityLocation(thirdPartyCompany);

			if (caseSpecificInfoPOJO.getEntityLocation() == null
					|| caseSpecificInfoPOJO.getEntityLocation().isEmpty()) {
				caseSpecificInfoPOJO.setEntityLocation(city);
			}
			caseSpecificInfoPOJO.setCaseNumber(caseSpecificDetailsInterface.getCaseNumber());
			caseSpecificInfoPOJOList.add(caseSpecificInfoPOJO);

		}

		return caseSpecificInfoPOJOList;
	}
}
