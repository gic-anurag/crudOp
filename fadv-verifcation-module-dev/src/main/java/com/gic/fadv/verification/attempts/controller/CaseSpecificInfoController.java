package com.gic.fadv.verification.attempts.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.fadv.verification.attempts.interfaces.CaseSpecificDetailsInterface;
import com.gic.fadv.verification.attempts.model.CaseSpecificInfo;
import com.gic.fadv.verification.attempts.model.CaseSpecificRecordDetail;
import com.gic.fadv.verification.attempts.repository.CaseSpecificInfoRepository;
import com.gic.fadv.verification.attempts.repository.CaseSpecificRecordDetailRepository;
import com.gic.fadv.verification.attempts.service.CaseSpecificRecordDetailService;
import com.gic.fadv.verification.pojo.CaseSpecificInfoPOJO;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class CaseSpecificInfoController {

	@Autowired
	private CaseSpecificInfoRepository caseSpecificInfoRepository;
	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;
	
	@Autowired
	private CaseSpecificRecordDetailService caseSpecificRecordDetailService;

	private static final Logger logger = LoggerFactory.getLogger(CaseSpecificInfoController.class);
	
	@PostMapping(path = "/get-case-specific-info")
	public List<CaseSpecificInfoPOJO> getCaseSpecificInfo(@RequestBody JsonNode requestBody) {
		logger.info("request : {}", requestBody);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		return caseSpecificRecordDetailService.getCaseSpecificRecords(mapper, requestBody);
	}

		
	@PostMapping(path = "/case-specific-info", produces = "application/json")
	public Optional<CaseSpecificInfo> postCaseSpecificInfo(@Valid @RequestBody CaseSpecificInfo requestBody) {

		CaseSpecificInfo caseSpecificInfo = new CaseSpecificInfo();
		caseSpecificInfo.setCandidateName(requestBody.getCandidateName());
		caseSpecificInfo.setCaseDetails(requestBody.getCaseDetails());
		caseSpecificInfo.setCaseMoreInfo(requestBody.getCaseMoreInfo());
		caseSpecificInfo.setCaseReference(requestBody.getCaseReference());
		caseSpecificInfo.setCaseRefNumber(requestBody.getCaseRefNumber());
		caseSpecificInfo.setCaseNumber(requestBody.getCaseNumber());
		caseSpecificInfo.setClientCode(requestBody.getClientCode());
		caseSpecificInfo.setClientName(requestBody.getClientName());
		caseSpecificInfo.setSbuName(requestBody.getSbuName());
		caseSpecificInfo.setPackageName(requestBody.getPackageName());
		caseSpecificInfo.setClientSpecificFields(requestBody.getClientSpecificFields());
		caseSpecificInfo.setUpdatedDate(new Date());
		
		String caseNumber = requestBody.getCaseNumber() != null ? requestBody.getCaseNumber() : "";
		
		if (!StringUtils.isEmpty(caseNumber)) {
			List<CaseSpecificInfo> caseSpecificInfos = caseSpecificInfoRepository.findByCaseNumber(caseNumber);
			if (!caseSpecificInfos.isEmpty()) {
				caseSpecificInfo.setCaseSpecificId(caseSpecificInfos.get(0).getCaseSpecificId());
				caseSpecificInfo.setCreatedDate(caseSpecificInfos.get(0).getCreatedDate());
			}
		} else {
			caseSpecificInfo.setCreatedDate(requestBody.getCreatedDate());
		}
		
		CaseSpecificInfo caseSpecificInfoNew = caseSpecificInfoRepository.save(caseSpecificInfo);

		if (requestBody.getCaseSpecificRecordDetail() != null) {
			for (CaseSpecificRecordDetail caseSpecificRecordDetail : requestBody.getCaseSpecificRecordDetail()) {
				CaseSpecificRecordDetail caseSpecificRecordDetailNew = new CaseSpecificRecordDetail();
				caseSpecificRecordDetailNew.setCaseSpecificId(caseSpecificInfoNew.getCaseSpecificId());
				caseSpecificRecordDetailNew.setComponentName(caseSpecificRecordDetail.getComponentName());
				caseSpecificRecordDetailNew.setProduct(caseSpecificRecordDetail.getProduct());
				caseSpecificRecordDetailNew.setComponentRecordField(caseSpecificRecordDetail.getComponentRecordField());
				caseSpecificRecordDetailNew.setInstructionCheckId(caseSpecificRecordDetail.getInstructionCheckId());
				caseSpecificRecordDetailRepository.save(caseSpecificRecordDetailNew);
			}
		}

		return caseSpecificInfoRepository.findById(caseSpecificInfoNew.getCaseSpecificId());
	}

	@GetMapping(path = "/case-specific-info/{check-id}", produces = "application/json")
	public List<CaseSpecificInfoPOJO> getCaseSpecificInfo(
			@PathVariable(name = "check-id", required = false) String checkId) {
		// Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		checkId = checkId != null ? checkId : "";
		if (StringUtils.isNotEmpty(checkId)) {
			System.out.println("Value of Check ID" + checkId);
			List<CaseSpecificInfo> caseSpecificInfo = caseSpecificInfoRepository.findByCheckId(checkId);
			System.out.println("Size of caseSpecificInfo" + caseSpecificInfo.size());
			List<CaseSpecificInfoPOJO> caseSpecificInfoPOJOList = new ArrayList<CaseSpecificInfoPOJO>();
			for (CaseSpecificInfo temp : caseSpecificInfo) {
				System.out.println("Size of getCaseSpecificRecordDetail" + temp.getCaseSpecificRecordDetail().size());
				for (int i = 0; i < temp.getCaseSpecificRecordDetail().size(); i++) {
					System.out.println("Iteration Number" + i);
					if (checkId.equals(temp.getCaseSpecificRecordDetail().get(i).getInstructionCheckId())) {

						CaseSpecificInfoPOJO caseSpecificInfoPOJO = new CaseSpecificInfoPOJO();
						// caseSpecificInfoPOJO.setRequestId(temp.getCaseSpecificId());
						caseSpecificInfoPOJO.setCaseReferenceNumber(temp.getCaseRefNumber());
						caseSpecificInfoPOJO.setClientName(temp.getClientName());
						caseSpecificInfoPOJO.setCandidateName(temp.getCandidateName());
						caseSpecificInfoPOJO.setSbuName(temp.getSbuName());
						caseSpecificInfoPOJO.setPackageName(temp.getPackageName());
						// Will come from Record Array
						caseSpecificInfoPOJO
								.setCheckId(temp.getCaseSpecificRecordDetail().get(i).getInstructionCheckId() + "");
						caseSpecificInfoPOJO
								.setComponentName(temp.getCaseSpecificRecordDetail().get(i).getComponentName());
						caseSpecificInfoPOJO.setProductName(temp.getCaseSpecificRecordDetail().get(i).getProduct());
//						caseSpecificInfoPOJO.setFunctionalEntityName(temp.getCaseSpecificRecordDetail().get(i).getFunctionalEntityName());
//						caseSpecificInfoPOJO.setEntityLocation(temp.getCaseSpecificRecordDetail().get(i).getEntityLocation());
						caseSpecificInfoPOJO
								.setStatusofCheck(temp.getCaseSpecificRecordDetail().get(i).getCheckStatus());
						caseSpecificInfoPOJO.setCheckCreatedDate(
								temp.getCaseSpecificRecordDetail().get(i).getCheckCreatedDate() + "");
						caseSpecificInfoPOJO
								.setCheckDueDate(temp.getCaseSpecificRecordDetail().get(i).getCheckDueDate() + "");
						caseSpecificInfoPOJO.setCheckTAT(temp.getCaseSpecificRecordDetail().get(i).getCheckTat());
						caseSpecificInfoPOJO
								.setRequestId(temp.getCaseSpecificRecordDetail().get(i).getCaseSpecificDetailId());
						/*
						 * Logic for taking akaName
						 */
						String caseRecordJson = temp.getCaseSpecificRecordDetail().get(i).getComponentRecordField();
						JsonNode caseRecordJsonNode = null;
						try {
							caseRecordJsonNode = mapper.readTree(caseRecordJson);
						} catch (JsonProcessingException e) {
							logger.error(e.getMessage(), e);
							e.printStackTrace();
						}
						logger.info("Value of JsonNode : {}", caseRecordJsonNode);
						if (caseRecordJsonNode != null && caseRecordJsonNode.has("Aka Name")) {
							caseSpecificInfoPOJO.setAkaName(caseRecordJsonNode.get("Aka Name").asText());
							caseSpecificInfoPOJO.setFunctionalEntityName(caseRecordJsonNode.get("Aka Name").asText());
						}
						if (caseRecordJsonNode != null && caseRecordJsonNode.has("Third party Company(city)")) {
							caseSpecificInfoPOJO
									.setEntityLocation(caseRecordJsonNode.get("Third party Company(city)").asText());
						}
						if (caseSpecificInfoPOJO.getEntityLocation() == null
								|| caseSpecificInfoPOJO.getEntityLocation().isEmpty()) {
							if (caseRecordJsonNode != null && caseRecordJsonNode.has("City")) {
								caseSpecificInfoPOJO.setEntityLocation(caseRecordJsonNode.get("City").asText());
							}
						}
						caseSpecificInfoPOJO.setCaseNumber(temp.getCaseNumber());
						caseSpecificInfoPOJOList.add(caseSpecificInfoPOJO);
					}
				}
			}
			return caseSpecificInfoPOJOList;
		} else {
			return null;
		}
		// return caseSpecificInfoPOJOList;
	}
}
