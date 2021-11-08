package com.gic.fadv.verification.spoc.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.attempts.model.AttemptHistory;
import com.gic.fadv.verification.attempts.model.AttemptStatusData;
import com.gic.fadv.verification.attempts.model.CaseSpecificInfo;
import com.gic.fadv.verification.attempts.model.CaseSpecificRecordDetail;
import com.gic.fadv.verification.attempts.repository.AttemptMasterRepository;
import com.gic.fadv.verification.attempts.repository.AttemptStatusDataRepository;
import com.gic.fadv.verification.attempts.repository.CaseSpecificInfoRepository;
import com.gic.fadv.verification.attempts.repository.CaseSpecificRecordDetailRepository;
import com.gic.fadv.verification.exception.ResourceNotFoundException;
import com.gic.fadv.verification.online.service.OnlineApiService;
import com.gic.fadv.verification.spoc.model.SPOCBulk;
import com.gic.fadv.verification.spoc.pojo.CaseReferencePOJO;
import com.gic.fadv.verification.spoc.pojo.CheckVerificationPOJO;
import com.gic.fadv.verification.spoc.pojo.FileUploadPOJO;
import com.gic.fadv.verification.spoc.pojo.QuestionPOJO;
import com.gic.fadv.verification.spoc.pojo.QuestionnairePOJO;
import com.gic.fadv.verification.spoc.pojo.SPOCBulkPOJO;
import com.gic.fadv.verification.spoc.pojo.TaskSpecsPOJO;
import com.gic.fadv.verification.spoc.repository.SPOCBulkRepository;
import com.gic.fadv.verification.spoc.service.SPOCBulkService;


import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india/")
public class SPOCBulkController {

	private static final Logger logger = LoggerFactory.getLogger(SPOCBulkController.class);

	@Autowired
	private SPOCBulkService spocBulkService;

	@Autowired
	private SPOCBulkRepository sPOCBulkRepository;

	@Autowired
	private OnlineApiService onlineApiService;

	@Autowired
	CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;

	@Autowired
	CaseSpecificInfoRepository caseSpecificInfoRepository;
	@Autowired
	AttemptMasterRepository attemptHistoryRepository;
	@Autowired
	AttemptStatusDataRepository attemptStatusDataRepository;
	@Autowired
	private EntityManager entityManager;
	
	// Questionnaire and verification specific URL
	@Value("${questionaire.list.l3.url}")
	private String questionaireURL;
	@Value("${verification.status.url.l3}")
	private String verificationStatusL3Url;

	private static final String L3_ERROR_RESPONSE = "{\"Error\": \"Error as reponse from L3\"}";

	@GetMapping("/spoc-bulk")
	public List<SPOCBulk> getAllSPOCBulk() {
		return sPOCBulkRepository.findAll();
	}

	@PostMapping("/spoc-bulk-aka")
	public List<SPOCBulk> getSPOCBulkByAkaName(@RequestBody String akaNameReq) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode akaNameNode = mapper.readTree(akaNameReq);
			String akaName = akaNameNode.has("akaName") ? akaNameNode.get("akaName").asText() : "";
			return sPOCBulkRepository.findByAkaNameAndFlag(akaName, "A");
		} catch (JsonProcessingException e) {
			logger.info("Exception while parsing:{}", e.getMessage());
		}
		return new ArrayList<>();
	}

	@GetMapping("/spoc-bulk/{id}")
	public ResponseEntity<SPOCBulk> getSPOCBulkById(@PathVariable(value = "id") Long sPOCBulkId)
			throws ResourceNotFoundException {
		SPOCBulk sPOCBulk = sPOCBulkRepository.findById(sPOCBulkId)
				.orElseThrow(() -> new ResourceNotFoundException("Not Found" + " :: " + sPOCBulkId));
		return ResponseEntity.ok().body(sPOCBulk);
	}

	/**
	 * This method is used to get create a SPOCBulk
	 * 
	 * @param SPOCBulk
	 * @return
	 */
	@ApiOperation(value = "This service is used to create a SPOC", response = List.class)
	@PostMapping("/spoc-bulk")
	public SPOCBulk createSPOCBulk(@Valid @RequestBody SPOCBulkPOJO sPOCBulkPOJO) {
		List<SPOCBulk> spocBulkList= sPOCBulkRepository.findByCheckId(sPOCBulkPOJO.getCheckId());
		
		sPOCBulkPOJO.setAkaName(sPOCBulkPOJO.getAkaName().trim());
		sPOCBulkPOJO.setCaseReference(sPOCBulkPOJO.getCaseReference().trim());
		sPOCBulkPOJO.setCaseNumber(sPOCBulkPOJO.getCaseNumber().trim());
		sPOCBulkPOJO.setCheckId(sPOCBulkPOJO.getCheckId().trim());
		sPOCBulkPOJO.setCandidateName(sPOCBulkPOJO.getCandidateName().trim());
		sPOCBulkPOJO.setClientName(sPOCBulkPOJO.getClientName().trim());
		sPOCBulkPOJO.setFlag(sPOCBulkPOJO.getFlag().trim());
		sPOCBulkPOJO.setCreatedDate(new Date());
		sPOCBulkPOJO.setUpdatedDate(new Date());
		SPOCBulk sPOCBulk = new SPOCBulk();

		BeanUtils.copyProperties(sPOCBulkPOJO, sPOCBulk);
		if(CollectionUtils.isNotEmpty(spocBulkList)) {
			sPOCBulk.setId(spocBulkList.get(0).getId());
			sPOCBulk.setCreatedDate(spocBulkList.get(0).getCreatedDate());
		}
		return sPOCBulkRepository.save(sPOCBulk);
	}

	/**
	 * This method is used to update a SPOCBulk by Id
	 * 
	 * @param SPOCBulkId
	 * @param SPOCBulk
	 * @return
	 * @throws ResourceNotFoundException
	 */
	@ApiOperation(value = "This service is used to update a SPOC", response = SPOCBulk.class)
	@PutMapping("/spoc-bulk/{id}")
	public ResponseEntity<SPOCBulk> updateSPOCBulk(@PathVariable(value = "id") Long sPOCBulkId,
			@Valid @RequestBody SPOCBulkPOJO sPOCBulkPOJO) throws ResourceNotFoundException {
		SPOCBulk sPOCBulk1 = sPOCBulkRepository.findById(sPOCBulkId)
				.orElseThrow(() -> new ResourceNotFoundException("Not Found" + ":: " + sPOCBulkId));

		sPOCBulk1.setAkaName(sPOCBulkPOJO.getAkaName().trim());
		sPOCBulk1.setCaseReference(sPOCBulkPOJO.getCaseReference().trim());
		sPOCBulk1.setCaseNumber(sPOCBulkPOJO.getCaseNumber().trim());
		sPOCBulk1.setCheckId(sPOCBulkPOJO.getCheckId().trim());
		sPOCBulk1.setCandidateName(sPOCBulkPOJO.getCandidateName().trim());
		sPOCBulk1.setClientName(sPOCBulkPOJO.getClientName().trim());
		sPOCBulk1.setFlag(sPOCBulkPOJO.getFlag().trim());
		sPOCBulk1.setUpdatedDate(new Date());

		final SPOCBulk updatedSPOCBulk = sPOCBulkRepository.save(sPOCBulk1);
		return ResponseEntity.ok(updatedSPOCBulk);
	}

	@PostMapping("/spoc-bulk-download")
	public ResponseEntity<String> downloadSPOCBulk(@Valid @RequestBody List<String> checkIdList,
			HttpServletResponse response) {
		try {
			String filePath = spocBulkService.processSPOCBulk(checkIdList);
			if (filePath != null) {
				File fileNameObject = new File(filePath);
				logger.info(fileNameObject.getName());
				try (InputStream inputStream = new FileInputStream(fileNameObject)) {
					response.setHeader("Content-Type", "application/zip");
					IOUtils.copy(inputStream, response.getOutputStream());
					response.getOutputStream().flush();
//					Utility.removeAttachmentFile(fileNameObject)
					// Instantiate an executor service
					postFileOperation(filePath, checkIdList);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}

				return ResponseEntity.ok().body("Download zip created");
			}
		} catch (JsonProcessingException e) {
			logger.error("Exception while processing files: {}", e.getMessage());
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Some error occurred");
	}

	private void postFileOperation(String filePath, List<String> checkIdList) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> fileDeleteAndDBOperation(filePath, checkIdList));
		logger.info("Bot requested data end");
		executor.shutdown();
	}

	private void fileDeleteAndDBOperation(String filePath, List<String> checkIdList) {
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		File delFile = new File(filePath);
		try {
			Files.delete(delFile.toPath());
		} catch (Exception e) {
			logger.error("Exception while deleting file : {}", e.getMessage());
		}
		ObjectMapper mapper = new ObjectMapper();
		
		//Temporary Changes
		
		for (String checkId : checkIdList) {
			CaseSpecificInfo caseSpecificInfo = getCaseSpecificInfoBy(checkId);
			CaseSpecificRecordDetail caseSpecificRecordDetail = getCaseSpecificRecordDetailBy(checkId);
			if (caseSpecificInfo != null && caseSpecificRecordDetail != null) {
				try {
					sendVerifyDataToL3(mapper, caseSpecificInfo, caseSpecificRecordDetail, checkId);
				} catch (JsonProcessingException e) {
					logger.error("Exception in send verify json:{}", e.getMessage());
				}
			}
		}

	}

	private CaseSpecificInfo getCaseSpecificInfoBy(String checkId) {
		List<CaseSpecificInfo> caseSpecificInfo = caseSpecificInfoRepository.findByCheckId(checkId);
		if (CollectionUtils.isNotEmpty(caseSpecificInfo)) {
			return caseSpecificInfo.get(0);
		}
		return new CaseSpecificInfo();
	}

	private CaseSpecificRecordDetail getCaseSpecificRecordDetailBy(String checkId) {
		List<CaseSpecificRecordDetail> caseSpecifiRecordDetails = caseSpecificRecordDetailRepository
				.findByInstructionCheckId(checkId);
		if (CollectionUtils.isNotEmpty(caseSpecifiRecordDetails)) {
			return caseSpecifiRecordDetails.get(0);
		}
		return new CaseSpecificRecordDetail();
	}

	private String sendVerifyDataToL3(ObjectMapper mapper, CaseSpecificInfo caseSpecificInfoPOJO,
			CaseSpecificRecordDetail caseSpecificRecordDetailPOJO, String checkId) throws JsonProcessingException {
		//Temporary Changes
		
		//TaskSpecsPOJO taskSpecs = new TaskSpecsPOJO();

		//taskSpecs.setCaseReference(
		//		getCaseReference(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO, checkId));
		//taskSpecs.setCheckVerification(getCheckVerification(caseSpecificRecordDetailPOJO));

		//taskSpecs.setQuestionaire(getQuestionnaire(mapper, checkId));

		//FileUploadPOJO fileUpload = new FileUploadPOJO();
		//fileUpload.setVerificationReplyDocument(new ArrayList<>());
		//taskSpecs.setFileUpload(fileUpload);

		//String taskSpecsStr = mapper.writeValueAsString(taskSpecs);
		//logger.info("l3 verification json : {} ", taskSpecsStr);

		//String l3VerifyResponse = onlineApiService.sendDataToL3Post(verificationStatusL3Url, taskSpecsStr, null);
		//String l3Status = "";
		//if (l3VerifyResponse == null) {
		//	l3VerifyResponse = L3_ERROR_RESPONSE;
		//	l3Status = "Failed";
		//} else {
			sPOCBulkRepository.updateFlagByCheckId(checkId, "D");
			//l3Status = "Sent";
		//}

		//createSpocAttempt(caseSpecificRecordDetailPOJO, l3VerifyResponse, l3Status);
		//return l3VerifyResponse;
		return "success";	
	}

	private CaseReferencePOJO getCaseReference(ObjectMapper mapper, CaseSpecificInfo caseSpecificInfoPOJO,
			CaseSpecificRecordDetail caseSpecificRecordDetailPOJO, String checkId) throws JsonProcessingException {
		CaseReferencePOJO caseReference = mapper.readValue(caseSpecificInfoPOJO.getCaseReference(),
				CaseReferencePOJO.class);

		caseReference.setCheckId(checkId);
		caseReference.setNgStatus("F1");
		caseReference.setNgStatusDescription("Followup 1");

		caseReference.setSbuName(caseSpecificInfoPOJO.getSbuName());
		caseReference.setProductName(caseSpecificRecordDetailPOJO.getProduct());
		caseReference.setPackageName(caseSpecificInfoPOJO.getPackageName());
		caseReference.setComponentName(caseSpecificRecordDetailPOJO.getComponentName());

		return caseReference;
	}

	private CheckVerificationPOJO getCheckVerification(CaseSpecificRecordDetail caseSpecificRecordDetailPOJO) {
		CheckVerificationPOJO checkVerification = new CheckVerificationPOJO();

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
		Date todaysDate = new Date();
		String todaysDateStr = formatter.format(todaysDate);

		checkVerification.setCountry("India");

		checkVerification.setExecutiveSummaryComments("");
		checkVerification.setInternalNotes(
				"The initiation template along with documents has been extracted as per verifier's requirement.");
		checkVerification.setEndStatusOfTheVerification("Work in Progress");

		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		checkVerification.setEmailId("");
		checkVerification.setVerifierName("");
		checkVerification.setGeneralRemarks("");
		checkVerification.setModeOfVerification("Email ID-Official");
		checkVerification.setVerifiedDate("");
		checkVerification.setAction("verifyChecks");
		checkVerification.setSubAction("verifyChecks");
		checkVerification.setActionCode("");
		checkVerification.setComponentName(caseSpecificRecordDetailPOJO.getComponentName());
		checkVerification.setAttempts("");
		checkVerification.setDepartmentName("");
		checkVerification.setKeyWords("");
		checkVerification.setCost("");
		checkVerification.setIsThisAVerificationAttempt("No");
		checkVerification.setDateVerificationCompleted("");
		checkVerification.setDisposition("");
		checkVerification.setExpectedClosureDate("");

		checkVerification.setVerifierDesignation("");
		checkVerification.setFollowUpDateAndTimes("");
		checkVerification.setVerifierNumber("");

		return checkVerification;
	}

	private List<QuestionnairePOJO> getQuestionnaire(ObjectMapper mapper, String checkId)
			throws JsonProcessingException {

		List<QuestionPOJO> questionPOJOList = new ArrayList<>();
		List<QuestionnairePOJO> questionnairePOJOList = new ArrayList<>();

		String requestUrl = questionaireURL + checkId;
		String questionResponse = onlineApiService.sendDataToL3Get(requestUrl);
		ObjectNode attemptQuestionnaireNode = mapper.createObjectNode();
		JsonNode questionnaire = mapper.createObjectNode();

		logger.info("Questionnaire response : {}", questionResponse);
		if (questionResponse != null && StringUtils.isNotEmpty(questionResponse)) {
			attemptQuestionnaireNode = (ObjectNode) mapper.readTree(questionResponse);
		}
		if (attemptQuestionnaireNode != null && attemptQuestionnaireNode.isEmpty()
				&& attemptQuestionnaireNode.has("response")) {
			questionnaire = attemptQuestionnaireNode.get("response");
		}
		if (questionnaire != null && !questionnaire.isEmpty()) {
			questionPOJOList = mapper.readValue(questionnaire.toString(), new TypeReference<List<QuestionPOJO>>() {
			});
		}

		for (QuestionPOJO questionPOJO : questionPOJOList) {
			String globalQuestionId = questionPOJO.getGlobalQuestionId() != null ? questionPOJO.getGlobalQuestionId()
					: "";
			QuestionnairePOJO questionnairePOJO = new QuestionnairePOJO();
			questionnairePOJO.setCaseQuestionRefID(globalQuestionId);
			questionnairePOJO.setAnswer(questionPOJO.getAnswere());
			questionnairePOJO.setQuestion(questionPOJO.getQuestionName());
			questionnairePOJO.setReportData("");
			questionnairePOJO.setStatus("");
			questionnairePOJO.setVerifiedData("");
			questionnairePOJOList.add(questionnairePOJO);

		}

		return questionnairePOJOList;
	}

	private void createSpocAttempt(CaseSpecificRecordDetail caseSpecificRecordDetail, String l3Response,
			String l3Status) {
		AttemptHistory attemptHistory = new AttemptHistory();

		attemptHistory.setAttemptDescription(
				"The initiation template along with documents has been extracted as per verifier's requirement.");

		attemptHistory.setAttemptStatusid((long) 10);
		attemptHistory.setName("");//Source Name
		attemptHistory.setName("");
		attemptHistory.setEmailAddress("");
		attemptHistory.setJobTitle("");
		attemptHistory.setCheckid(caseSpecificRecordDetail.getInstructionCheckId());
		attemptHistory.setFollowupId((long) 1);
		attemptHistory.setRequestid(caseSpecificRecordDetail.getCaseSpecificDetailId());
		attemptHistory.setContactDate(new Date().toString());
		attemptHistory.setL3Response(l3Response);
		attemptHistory.setL3Status(l3Status);
		attemptHistory.setFollowupDate("");
		attemptHistory.setClosureExpectedDate("");
		AttemptHistory newAttemptHistory = attemptHistoryRepository.save(attemptHistory);
		createAttemptStatusData(newAttemptHistory);
	}

	private void createAttemptStatusData(AttemptHistory attemptHistory) {
		Long attemptId = attemptHistory.getAttemptid() != null ? attemptHistory.getAttemptid() : 0;
		if (attemptId != 0) {
			AttemptStatusData attemptStatusData = new AttemptStatusData();
			attemptStatusData.setAttemptId(attemptId);
			attemptStatusData.setDepositionId((long) 13);
			attemptStatusData.setEndstatusId((long) 1);
			attemptStatusData.setModeId((long) 14);
			attemptStatusDataRepository.save(attemptStatusData);
		}
	}
	
	
	@PostMapping("/spoc-bulk/search")
	public ResponseEntity<List<SPOCBulk>> getSpocBulkByFilter(@RequestBody SPOCBulkPOJO sPOCBulkPOJO) {
		boolean isFilter = false;
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<SPOCBulk> criteriaQuery = criteriaBuilder.createQuery(SPOCBulk.class);
		Root<SPOCBulk> itemRoot = criteriaQuery.from(SPOCBulk.class);

		List<Predicate> predicates = new ArrayList<>();
		List<SPOCBulk> sPOCBulkList = new ArrayList<>();

		if (sPOCBulkPOJO.getId() != 0) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("id"), sPOCBulkPOJO.getId()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(sPOCBulkPOJO.getAkaName())) {
			predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(itemRoot.get("akaName")),
					sPOCBulkPOJO.getAkaName().trim().toLowerCase()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(sPOCBulkPOJO.getCaseReference())) {
			predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(itemRoot.get("caseReference")),
					sPOCBulkPOJO.getCaseReference().trim().toLowerCase()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(sPOCBulkPOJO.getCaseNumber())) {
			predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(itemRoot.get("caseNumber")),
					sPOCBulkPOJO.getCaseNumber().trim().toLowerCase()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(sPOCBulkPOJO.getCheckId())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("checkId"),
					sPOCBulkPOJO.getCheckId()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(sPOCBulkPOJO.getCandidateName())) {
			predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(itemRoot.get("candidateName")),
					sPOCBulkPOJO.getCandidateName().trim().toLowerCase()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(sPOCBulkPOJO.getClientName())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("clientName"), sPOCBulkPOJO.getClientName().trim()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(sPOCBulkPOJO.getFlag())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("flag"), sPOCBulkPOJO.getFlag().trim()));
			isFilter = true;
		}
//		if (!StringUtils.isEmpty(sPOCBulkPOJO.getCreatedDate().toString())) {
//			predicates.add(criteriaBuilder.equal(itemRoot.get("createdDate"), sPOCBulkPOJO.getCreatedDate().toString()));
//			isFilter = true;
//		}
//		if (!StringUtils.isEmpty(sPOCBulkPOJO.getUpdatedDate().toString())) {
//			predicates.add(criteriaBuilder.equal(itemRoot.get("updatedDate"), sPOCBulkPOJO.getUpdatedDate().toString()));
//			isFilter = true;
//		}
		if (isFilter) {
			criteriaQuery.where(predicates.toArray(new Predicate[0]));
			sPOCBulkList = entityManager.createQuery(criteriaQuery).getResultList();
		}

		return ResponseEntity.ok().body(sPOCBulkList);
	}
}