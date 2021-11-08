package com.gic.fadv.vendor.output.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.vendor.model.AttemptHistory;
import com.gic.fadv.vendor.model.AttemptQuestionnaire;
import com.gic.fadv.vendor.model.AttemptStatusData;
import com.gic.fadv.vendor.model.AttemptUploadDocument;
import com.gic.fadv.vendor.model.VendorInputComponentRecords;
import com.gic.fadv.vendor.model.VerificationEventStatus;
import com.gic.fadv.vendor.output.interfaces.CaseAndCheckDetailInterface;
import com.gic.fadv.vendor.output.pojo.GlobalQuestionAndFormLabelPOJO;
import com.gic.fadv.vendor.output.pojo.L3QuestionnairePOJO;
import com.gic.fadv.vendor.output.pojo.VendorQuestioneireMappingPOJO;
import com.gic.fadv.vendor.pojo.CheckVerificationPOJO;
import com.gic.fadv.vendor.pojo.FileUploadPOJO;
import com.gic.fadv.vendor.pojo.L3ApiRequestHistoryPOJO;
import com.gic.fadv.vendor.pojo.L3CaseReferencePOJO;
import com.gic.fadv.vendor.pojo.QuestionnairePOJO;
import com.gic.fadv.vendor.pojo.TaskSpecsPOJO;
import com.gic.fadv.vendor.repository.AttemptHistoryRepository;
import com.gic.fadv.vendor.repository.AttemptQuestionnaireRepository;
import com.gic.fadv.vendor.repository.AttemptStatusDataRepository;
import com.gic.fadv.vendor.repository.AttemptUploadDocumentRepository;
import com.gic.fadv.vendor.repository.VendorInputComponentRecordsRepository;
import com.gic.fadv.vendor.repository.VerificationEventStatusRepository;
import com.gic.fadv.vendor.service.ApiService;
import com.gic.fadv.vendor.service.VendorSftpConnectionService;
import com.gic.fadv.vendor.utility.Utility;
import com.gic.fadv.vendor.utility.ZipUtility;

@Service
public class VendorOutputServiceNewImpl implements VendorOutputServiceNew {

	private static final String CLIENT_NAME = "Client Name";

	private static final String ADDRESS2 = "Address";

	private static final String MAJOR = "Major";

	private static final String QUALIFICATION = "Qualification";

	private static final String NAME_OF_THE_CANDIDATE2 = "Name of the Candidate";

	private static final String EDUCATION = "education";

	private static final String F2 = "F2";

	private static final String OUTPUT_FILE_VERIFIED = "Output File Verified";

	private static final String ADDRESS = "address";

	private static final String WRITTEN = "Written";

	private static final String VERBAL = "Verbal";

	private static final String MANUAL_TAGGING = "Manual Tagging";

	private static final String VERIFIED = "Verified";

	private static final String CLIENT_CODE = "Client Code";

	private static final String RELATIONSHIP_WITH_CANDIDATE = "Relationship with Candidate";

	private static final String VERIFIER_NAME = "Verifier Name";

	private static final String COLLEGE_NAME = "College Name";

	private static final String VERIFIER_PERIOD_OF_STAY_TO = "verifier Period of stay To";

	private static final String VERIFIER_PERIOD_OF_STAY_FROM = "verifier Period of stay From";

	private static final String ADDRESS_OF_THE_CANDIDATE = "Address of the Candidate";

	private static final String NAME_OF_THE_CANDIDATE = NAME_OF_THE_CANDIDATE2;

	private static final String FORWARD_SLASH = "/";

	private static final String INSTRUCTION_ID = "Instruction ID";
	private static final String L3_ERROR_RESPONSE = "{\"Error\": \"Error as reponse from L3\"}";

	private static boolean isIndividual = false;

	@Autowired
	private VendorSftpConnectionService vendorSftpConnectionService;

	@Autowired
	private ApiService apiService;

	@Autowired
	private VendorInputComponentRecordsRepository vendorInputComponentRecordsRepository;

	@Autowired
	private AttemptHistoryRepository attemptHistoryRepository;

	@Autowired
	private AttemptStatusDataRepository attemptStatusDataRepository;

	@Autowired
	private VerificationEventStatusRepository verificationEventStatusRepository;

	@Autowired
	private AttemptUploadDocumentRepository attemptUploadDocumentRepository;

	@Autowired
	private AttemptQuestionnaireRepository attemptQuestionnaireRepository;

	@Value("${local.file.download.location}")
	private String localFileDownloadLocation;

	@Value("${education.auto.tagging.rest.url}")
	private String educationAutoTaggingUrl;

	@Value("${address.auto.tagging.rest.url}")
	private String addressAutoTaggingUrl;

	@Value("${question.id.list.url}")
	private String questionIdListUrl;

	@Value("${education.written.mandate.rest.url}")
	private String educationWrittenMandateUrl;

	@Value("${digi.address.client.rest.url}")
	private String digiAddressClientUrl;

	@Value("${vendor.questionnaire.rest.url}")
	private String vendorQuestionnaireUrl;

	@Value("${verification.status.url.l3}")
	private String verificationStatusL3Url;

	@Value("${l3.request.history.rest.url}")
	private String l3RequestHistoryUrl;

	@Value("${address.mandate.client.rest.url}")
	private String addressMandateUrl;

	@Value("${remote.shared.file.path}")
	private String remoteSharedFilePath;

	@Value("${verify.checks.file.path}")
	private String verifyCheckFilePath;

	@Value("${verify.checks.file.path.local}")
	private String verifyCheckFilePathLocal;

	@Value("${questionaire.list.l3.url}")
	private String questionaireListL3Url;

	private static final Logger logger = LoggerFactory.getLogger(VendorOutputServiceNewImpl.class);

	ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Override
	public void processVendorOutputRequest(String serviceName, String fileName, String filePath) {
		if (StringUtils.isNotEmpty(filePath) && StringUtils.isNotEmpty(fileName)) {
			if (Boolean.TRUE.equals(downloadFileFromSftp(fileName, filePath))) {
				logger.info("{} Output file downloaded successfully", serviceName);
				if (StringUtils.equalsIgnoreCase(serviceName, ADDRESS)) {
					logger.info("Address output file execution start");
					readAddressExcel(fileName, filePath);
				} else if (StringUtils.equalsIgnoreCase(serviceName, EDUCATION)) {
					logger.info("Education output file execution start");
					unzipEducationOutputFile(fileName);
				}
			} else {
				logger.info("{} Output file download failed", serviceName);
			}
		}
	}

	private boolean downloadFileFromSftp(String fileName, String filePath) {
		String source = filePath + FORWARD_SLASH + fileName;
		String destination = localFileDownloadLocation + fileName;
		logger.info("SFTP file being download from : {} to : {}", source, destination);
		boolean fileDownlod = vendorSftpConnectionService.downloadFromSftp(source, destination);
		try {
			TimeUnit.SECONDS.sleep(10);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return fileDownlod;
	}

	private void unzipEducationOutputFile(String fileName) {

		logger.info("Unzip education output files");
		try {
			ZipUtility.unZipArchive(localFileDownloadLocation, localFileDownloadLocation + fileName);
			logger.info("UnZip Archive is Sucessful");
			readEducationOutputExcel();
		} catch (IOException e) {
			logger.error("Exception while unzipping education output archive : {}", e.getMessage());
		}
		File zipFile = new File(localFileDownloadLocation + fileName);
		try {
			Files.delete(zipFile.toPath());
			logger.info("Zip : {} file deleted successfully", zipFile);
		} catch (IOException e) {
			logger.error("Exception while deleting zip : {} file : {}", zipFile, e.getMessage());
		}
	}

	private void readEducationOutputExcel() {
		Date todaysDate = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM");
		int strMonth = Integer.parseInt(simpleDateFormat.format(todaysDate));

		simpleDateFormat = new SimpleDateFormat("dd");
		int strDay = Integer.parseInt(simpleDateFormat.format(todaysDate));

		simpleDateFormat = new SimpleDateFormat("yyyy");
		int strYear = Integer.parseInt(simpleDateFormat.format(todaysDate));

		String educationCompletedFile = "EducationCompletedReport_" + strDay + "_" + strMonth + "_" + strYear + ".xlsx";
		try {
			List<ObjectNode> recordResultArr = Utility
					.getExcelDataAsJsonObject(localFileDownloadLocation + educationCompletedFile);
			logger.info("Value of Downloaded Education Excel Json : {}", recordResultArr);
			processRecordResultListEducation(recordResultArr);
		} catch (IOException e) {
			logger.error("Exception while reading education excel file : {}", e.getMessage());
		}
		try {
			File educationFile = new File(localFileDownloadLocation + educationCompletedFile);
			Files.delete(educationFile.toPath());
			logger.info("Education excel : {} file deleted successfully", educationCompletedFile);
		} catch (IOException e) {
			logger.error("Exception while deleting Education excel : {} file : {}", educationCompletedFile,
					e.getMessage());
		}

	}

	private void processRecordResultListEducation(List<ObjectNode> recordResultArr) {
		String clientCode = "";
		String clientName = "";

		for (JsonNode recordResult : recordResultArr) {
			if (recordResult.has(CLIENT_CODE)) {
				clientCode = recordResult.get(CLIENT_CODE).asText();
			}
			if (recordResult.has(CLIENT_NAME)) {
				clientName = recordResult.get(CLIENT_NAME).asText();
			}
			processRecordResultEducation(recordResult, clientCode, clientName);
		}
	}

	private void processRecordResultEducation(JsonNode recordResult, String clientCode, String clientName) {
		String checkId = recordResult.has(INSTRUCTION_ID) ? recordResult.get(INSTRUCTION_ID).asText() : "";
		if (StringUtils.isNotEmpty(checkId) && StringUtils.isNotEmpty(clientCode)) {

			// Check if PDF file is Available
			logger.info("Value of File {}//{}.pdf", localFileDownloadLocation, checkId);
			File file = new File(localFileDownloadLocation + checkId.trim() + ".pdf");

			List<VendorInputComponentRecords> vendorInputComponentRecordsList = vendorInputComponentRecordsRepository
					.findByCheckId(checkId);
			VendorInputComponentRecords vendorInputComponentRecords = null;
			if (CollectionUtils.isNotEmpty(vendorInputComponentRecordsList)) {
				vendorInputComponentRecords = vendorInputComponentRecordsList.get(0);
			}
			String educationAutoTaggingRes = apiService.sendDataToGet(educationAutoTaggingUrl + clientCode);

			logger.info("VendorInputComponentRecords : {}", vendorInputComponentRecords);
			logger.info("EduAutoTaggingClientList : {}", educationAutoTaggingRes);

			CaseAndCheckDetailInterface caseAndCheckDetailInterface = vendorInputComponentRecordsRepository
					.getDetailsUsingCheckId(checkId);

//			String educationWrittenMandateRes = apiService.sendDataToGet(educationWrittenMandateUrl + clientCode)

			String educationWrittenMandateRes = checkPostMandate(clientCode, clientName,
					caseAndCheckDetailInterface.getSbuName(), caseAndCheckDetailInterface.getPackageName(),
					"Education - Written Mandate", "Education");

			logger.info("EducationWrittenMandateRes : {}", educationWrittenMandateRes);

			if (vendorInputComponentRecords != null && educationAutoTaggingRes != null
					&& StringUtils.equalsIgnoreCase(educationAutoTaggingRes, "Yes")) {
				checkDatabaseForEducation(recordResult, vendorInputComponentRecords, file, educationWrittenMandateRes,
						checkId, caseAndCheckDetailInterface);
			} else {
				logger.info(
						"Education : {} data Does not Exists in either vendorInputComponentRecords or autoTaggingClientList",
						checkId);
			}

			try {
				Files.delete(file.toPath());
				logger.info("Check Id : {}.pdf file deleted successfully", checkId);
			} catch (IOException e) {
				logger.error("Exception while deleting checkid : {}.pdf file : {}", checkId, e.getMessage());
			}
		}
	}

	private void checkDatabaseForEducation(JsonNode recordResult,
			VendorInputComponentRecords vendorInputComponentRecords, File file, String educationWrittenMandateRes,
			String checkId, CaseAndCheckDetailInterface caseAndCheckDetailInterface) {

		String verificationMode = recordResult.has("Mode Of Verification")
				? recordResult.get("Mode Of Verification").asText()
				: "";
		String writtenVerStatus = recordResult.has("Written-Verification Status")
				? recordResult.get("Written-Verification Status").asText()
				: "";
		String verbalVerStatus = recordResult.has("Verbal-Verification Status")
				? recordResult.get("Verbal-Verification Status").asText()
				: "";
		educationWrittenMandateRes = educationWrittenMandateRes != null ? educationWrittenMandateRes : "";
		if ((StringUtils.equalsIgnoreCase(verificationMode, WRITTEN)
				&& StringUtils.equalsIgnoreCase(writtenVerStatus.trim(), "Genuine") && file.exists())
				|| (StringUtils.equalsIgnoreCase(verificationMode.trim(), VERBAL)
						&& StringUtils.equalsIgnoreCase(verbalVerStatus.trim(), "Genuine")
						&& !StringUtils.equalsIgnoreCase(educationWrittenMandateRes.trim(), "Yes - Mandate written"))) {
			JsonNode checkInputRecords = vendorInputComponentRecords.getCheckRecord();
			logger.info("Education : CheckInputRecords : {}", checkInputRecords);

			if (checkInputRecords != null && !checkInputRecords.isEmpty()) {

				validateEducationData(checkInputRecords, recordResult, caseAndCheckDetailInterface, checkId,
						verificationMode);
			}
		}
	}

	private void validateEducationData(JsonNode checkInputRecords, JsonNode recordResult,
			CaseAndCheckDetailInterface caseAndCheckDetailInterface, String checkId, String verificationMode) {

		createVendorOutputVerificationEvent(caseAndCheckDetailInterface, checkId, "Initiated",
				"Request Initiated for Education");

		String verifierName = "";
		String verifierTitle = "";
		if (StringUtils.equalsIgnoreCase(verificationMode, VERBAL)) {
			verifierName = recordResult.has("Verbal-Verifier Name") ? recordResult.get("Verbal-Verifier Name").asText()
					: "";
			verifierTitle = recordResult.has("Verbal-Verifier Designation")
					? recordResult.get("Verbal-Verifier Designation").asText()
					: "";
		} else if (StringUtils.equalsIgnoreCase(verificationMode, WRITTEN)) {
			verifierName = recordResult.has("Written-Verifier Name")
					? recordResult.get("Written-Verifier Name").asText()
					: "";
			verifierTitle = recordResult.has("Written-Verifier Designation")
					? recordResult.get("Written-Verifier Designation").asText()
					: "";
		}

		String inputQuali = checkInputRecords.has(QUALIFICATION) ? checkInputRecords.get(QUALIFICATION).asText() : "";
		String inputCandidate = checkInputRecords.has("Candidate Name")
				? checkInputRecords.get("Candidate Name").asText()
				: "";
		String inputMajor = checkInputRecords.has(MAJOR) ? checkInputRecords.get(MAJOR).asText() : "";
		String inputYear = checkInputRecords.has("Year of Passing \nE.g.Month-Year (July-2005)")
				? checkInputRecords.get("Year of Passing \nE.g.Month-Year (July-2005)").asText()
				: "";
		String inputUniversityName = checkInputRecords.has("University Name")
				? checkInputRecords.get("University Name").asText()
				: "";
		String inputCollegeName = checkInputRecords.has(COLLEGE_NAME) ? checkInputRecords.get(COLLEGE_NAME).asText()
				: "";

		String outputQuali = recordResult.has(QUALIFICATION) ? recordResult.get(QUALIFICATION).asText() : "";
		String outputCandidate = recordResult.has(NAME_OF_THE_CANDIDATE2)
				? recordResult.get(NAME_OF_THE_CANDIDATE2).asText()
				: "";
		String outputMajor = recordResult.has(MAJOR) ? recordResult.get(MAJOR).asText() : "";
		String outputPassingyear = recordResult.has("Year Of Passing") ? recordResult.get("Year Of Passing").asText()
				: "";
		String grduationYear = recordResult.has("Year of Graduation") ? recordResult.get("Year of Graduation").asText()
				: "";
		String outputUniversity = recordResult.has("University") ? recordResult.get("University").asText() : "";
		String outputCollege = recordResult.has(COLLEGE_NAME) ? recordResult.get(COLLEGE_NAME).asText() : "";

		String verifiedFrom = recordResult.has("Verified From") ? recordResult.get("Verified From").asText() : "";

		inputCandidate = StringUtils.isNotEmpty(inputCandidate) ? inputCandidate.replaceAll("\\s+", " ")
				: inputCandidate;
		outputCandidate = StringUtils.isNotEmpty(outputCandidate) ? outputCandidate.replaceAll("\\s+", " ")
				: outputCandidate;

		if (StringUtils.isNotEmpty(inputQuali) && StringUtils.isNotEmpty(outputQuali)
				&& StringUtils.isNotEmpty(inputCandidate) && StringUtils.isNotEmpty(outputCandidate)
				&& StringUtils.isNotEmpty(inputMajor) && StringUtils.isNotEmpty(outputMajor)
				&& StringUtils.equalsIgnoreCase(outputQuali.trim(), inputQuali.trim())
				&& StringUtils.equalsIgnoreCase(outputCandidate.trim(), inputCandidate.trim())
				&& StringUtils.equalsIgnoreCase(outputMajor.trim(), inputMajor.trim())
				&& ((StringUtils.isNotEmpty(inputYear) && StringUtils.isNotEmpty(outputPassingyear)
						&& StringUtils.equalsIgnoreCase(outputPassingyear.trim(), inputYear.trim()))
						|| StringUtils.isNotEmpty(grduationYear))
				&& ((StringUtils.isNotEmpty(inputUniversityName) && StringUtils.isNotEmpty(outputUniversity)
						&& StringUtils.equalsIgnoreCase(outputUniversity.trim(), inputUniversityName.trim()))
						|| (StringUtils.isNotEmpty(inputCollegeName) && StringUtils.isNotEmpty(outputCollege)
								&& StringUtils.equalsIgnoreCase(outputCollege.trim(), inputCollegeName.trim())))
				&& StringUtils.isNotEmpty(verifiedFrom)) {

			try {

//========================== NOTE THIS IS A TEMPORARY Change ========================================================
//				String l3Response = sendDataToVerifyL3(caseAndCheckDetailInterface, recordResult, verificationMode,
//				false, verifierName);
//		createVerifiedAttempt(caseAndCheckDetailInterface, verifierName, verifierTitle, checkId,
//				getL3Status(l3Response), l3Response);
//		createVendorOutputVerificationEvent(caseAndCheckDetailInterface, checkId, VERIFIED,
//				OUTPUT_FILE_VERIFIED);
				if (StringUtils.equalsIgnoreCase(verificationMode, WRITTEN)) {
					String checkIdFileName = checkId.trim() + ".pdf";
					vendorSftpConnectionService.copyFileToAnotherPath(remoteSharedFilePath,
							localFileDownloadLocation + checkIdFileName, checkIdFileName);
				}

				String l3Response = sendDataToVerifyL3(caseAndCheckDetailInterface, recordResult, verificationMode,
						false, verifierName);
				createFollowUpAttempt(caseAndCheckDetailInterface, verifierName, verifierTitle, checkId,
						getL3Status(l3Response), l3Response);
				createVendorOutputVerificationEvent(caseAndCheckDetailInterface, checkId, MANUAL_TAGGING, F2);
//===================================================================================================================

			} catch (IOException e) {
				logger.error("Exception while mapping l3 request : {}", e.getMessage());
			}
		} else {
			try {
				if (StringUtils.equalsIgnoreCase(verificationMode, WRITTEN)) {
					String checkIdFileName = checkId.trim() + ".pdf";
					vendorSftpConnectionService.copyFileToAnotherPath(remoteSharedFilePath,
							localFileDownloadLocation + checkIdFileName, checkIdFileName);
				}
				String l3Response = sendDataToVerifyL3(caseAndCheckDetailInterface, recordResult, verificationMode,
						false, verifierName);
				createFollowUpAttempt(caseAndCheckDetailInterface, verifierName, verifierTitle, checkId,
						getL3Status(l3Response), l3Response);
				createVendorOutputVerificationEvent(caseAndCheckDetailInterface, checkId, MANUAL_TAGGING, F2);
			} catch (IOException e) {
				logger.error("Exception while mapping l3 request : {}", e.getMessage());
			}
		}
	}

	private void createVerifiedAttempt(CaseAndCheckDetailInterface caseAndCheckDetailInterface, String verifierName,
			String relWithCandidate, String checkId, String l3Status, String l3Response) {

		Long requestId = (long) 0;
		if (caseAndCheckDetailInterface != null) {
			requestId = caseAndCheckDetailInterface.getCaseSpecificRecordId();
		}

		AttemptHistory attemptHistory = new AttemptHistory();

		attemptHistory.setAttemptStatusid((long) 54);
		attemptHistory.setAttemptDescription("Verified through vendor portal.");
		attemptHistory.setName(verifierName);
		attemptHistory.setJobTitle(relWithCandidate);
		attemptHistory.setCheckid(checkId);

		attemptHistory.setFollowupId((long) 31);
		attemptHistory.setRequestid(requestId);
		attemptHistory.setL3Status(l3Status);
		attemptHistory.setL3Response(l3Response);

		Date contactDate = new Date();
		attemptHistory.setContactDate(contactDate.toString());
		AttemptHistory newAttemptHistory = attemptHistoryRepository.save(attemptHistory);
		createAttemptStatusData(newAttemptHistory, 31);
	}

	private void createFollowUpAttempt(CaseAndCheckDetailInterface caseAndCheckDetailInterface, String verifierName,
			String relWithCandidate, String checkId, String l3Status, String l3Response) {

		Long requestId = (long) 0;
		if (caseAndCheckDetailInterface != null) {
			requestId = caseAndCheckDetailInterface.getCaseSpecificRecordId();
		}
		AttemptHistory attemptHistory = new AttemptHistory();

		attemptHistory.setAttemptStatusid((long) 65);
		attemptHistory.setAttemptDescription("Follow up");
		attemptHistory.setName(verifierName);
		attemptHistory.setJobTitle(relWithCandidate);
		attemptHistory.setCheckid(checkId);
		attemptHistory.setFollowupId((long) 2);
		attemptHistory.setRequestid(requestId);
		attemptHistory.setL3Status(l3Status);
		attemptHistory.setL3Response(l3Response);

		Date contactDate = new Date();
		attemptHistory.setContactDate(contactDate.toString());
		AttemptHistory newAttemptHistory = attemptHistoryRepository.save(attemptHistory);
		createAttemptStatusData(newAttemptHistory, 2);

	}

	private void createAttemptStatusData(AttemptHistory attemptHistory, int followUpId) {
		Long attemptId = attemptHistory.getAttemptid() != null ? attemptHistory.getAttemptid() : 0;
		if (attemptId != 0) {
			AttemptStatusData attemptStatusData = new AttemptStatusData();
			attemptStatusData.setAttemptId(attemptId);
			attemptStatusData.setDepositionId((long) 13);
			attemptStatusData.setEndstatusId((long) followUpId);
			attemptStatusData.setModeId((long) 14);
			attemptStatusDataRepository.save(attemptStatusData);
		}
	}

	private void readAddressExcel(String fileName, String filePath) {
		try {
			List<ObjectNode> recordResultArr = Utility.getExcelDataAsJsonObject(localFileDownloadLocation + fileName);
			logger.info("Value of Downloaded Address Excel Json : {}", recordResultArr);
			processRecordResultListAddress(recordResultArr, filePath, fileName);
		} catch (IOException e) {
			logger.error("Exception while reading address excel file : {}", e.getMessage());
		}
	}

	private void processRecordResultListAddress(List<ObjectNode> recordResultArr, String filePath, String fileName) {
		String clientCode = "";
		String clientName = "";

		for (JsonNode recordResult : recordResultArr) {
			if (recordResult.has(CLIENT_CODE)) {
				clientCode = recordResult.get(CLIENT_CODE).asText();
			}
			if (recordResult.has(CLIENT_NAME)) {
				clientName = recordResult.get(CLIENT_NAME).asText();
			}

			processRecordResultAddress(recordResult, clientCode, clientName, filePath);
		}
		File myObj = new File(localFileDownloadLocation + fileName);
		try {
			Files.delete(myObj.toPath());
			logger.info("Address output File deleted successfully from local: {}", myObj);
		} catch (IOException e) {
			logger.error("Address output File delete failed from local: {}", e.getMessage());
		}
	}

	private void processRecordResultAddress(JsonNode recordResult, String clientCode, String clientName,
			String filePath) {
		String checkId = recordResult.has(INSTRUCTION_ID) ? recordResult.get(INSTRUCTION_ID).asText() : "";
		if (StringUtils.isNotEmpty(checkId) && StringUtils.isNotEmpty(clientCode)) {

			// Check if PDF file is Available
			logger.info("Value of File {}{}.pdf", localFileDownloadLocation, checkId);
			String checkIdFileName = checkId.trim() + ".pdf";

			if (Boolean.TRUE.equals(downloadFileFromSftp(checkIdFileName, filePath))) {
				List<VendorInputComponentRecords> vendorInputComponentRecordsList = vendorInputComponentRecordsRepository
						.findByCheckId(checkId);
				VendorInputComponentRecords vendorInputComponentRecords = null;
				if (CollectionUtils.isNotEmpty(vendorInputComponentRecordsList)) {
					vendorInputComponentRecords = vendorInputComponentRecordsList.get(0);
				}
				String addressAutoTaggingRes = apiService.sendDataToGet(addressAutoTaggingUrl + clientCode);
				String digiAddressClientRes = apiService.sendDataToGet(digiAddressClientUrl + clientCode);

				logger.info("VendorInputComponentRecords : {}", vendorInputComponentRecords);
				logger.info("AddressAutoTaggingClientList : {}", addressAutoTaggingRes);
				logger.info("digiAddressClientRes : {}", digiAddressClientRes);

				checkDatabaseForAddress(recordResult, checkId, vendorInputComponentRecords, addressAutoTaggingRes,
						clientCode, clientName, digiAddressClientRes);

				File myObj = new File(localFileDownloadLocation + checkIdFileName);
				try {
					Files.delete(myObj.toPath());
					logger.info("Address CheckId File deleted successfully from local: {}", myObj);
				} catch (IOException e) {
					logger.error("Address CheckId File delete failed from local: {}", e.getMessage());
				}

			} else {
				logger.info("Address : {}.pdf Does not Exists", checkId);
			}

		}
	}

	private void checkDatabaseForAddress(JsonNode recordResult, String checkId,
			VendorInputComponentRecords vendorInputComponentRecords, String addressAutoTaggingRes, String clientCode,
			String clientName, String digiAddressClientRes) {
		if (vendorInputComponentRecords != null && addressAutoTaggingRes != null
				&& StringUtils.equalsIgnoreCase(addressAutoTaggingRes, "Yes") && digiAddressClientRes != null
				&& StringUtils.equalsIgnoreCase(digiAddressClientRes, "Yes")) {

			JsonNode checkInputRecords = vendorInputComponentRecords.getCheckRecord();
			logger.info("Address : CheckInputRecords : {}", checkInputRecords);

			if (checkInputRecords != null && !checkInputRecords.isEmpty()) {
				String inputStayFrom = checkInputRecords.has("Date From") ? checkInputRecords.get("Date From").asText()
						: "";
				String inputStayTo = checkInputRecords.has("DateTo") ? checkInputRecords.get("DateTo").asText() : "";
				String outputStayFrom = recordResult.has(VERIFIER_PERIOD_OF_STAY_FROM)
						? fullDateFormat(recordResult.get(VERIFIER_PERIOD_OF_STAY_FROM).asText())
						: "";
				String outputStayTo = recordResult.has(VERIFIER_PERIOD_OF_STAY_TO)
						? fullDateFormat(recordResult.get(VERIFIER_PERIOD_OF_STAY_TO).asText())
						: "";

				CaseAndCheckDetailInterface caseAndCheckDetailInterface = vendorInputComponentRecordsRepository
						.getDetailsUsingCheckId(checkId);

				String posMandate = checkPostMandate(clientCode, clientName, caseAndCheckDetailInterface.getSbuName(),
						caseAndCheckDetailInterface.getPackageName(),
						"Is the Period of Stay required in the Address check component in the Verification", ADDRESS2);
				if ((StringUtils.equalsIgnoreCase(posMandate, "Yes") && StringUtils.isNotEmpty(inputStayFrom)
						&& StringUtils.isNotEmpty(inputStayTo) && StringUtils.isNotEmpty(outputStayFrom)
						&& StringUtils.isNotEmpty(outputStayTo)
						&& StringUtils.equalsIgnoreCase(inputStayFrom.trim(), outputStayFrom.trim())
						&& StringUtils.equalsIgnoreCase(inputStayTo.trim(), outputStayTo.trim()))
						|| !StringUtils.equalsIgnoreCase(posMandate, "Yes")) {

					validateAddressData(checkInputRecords, recordResult, caseAndCheckDetailInterface, checkId, false);
				} else {
					logger.info(
							"Failed to match criteria for verified date from and date to check inputStayFrom : {}, outputStayFrom : {}, "
									+ "inputStayTo : {}, outputStayTo : {}, posMandate : {}",
							inputStayFrom, outputStayFrom, inputStayTo, outputStayTo, posMandate);
					validateAddressData(checkInputRecords, recordResult, caseAndCheckDetailInterface, checkId, true);
				}
			}
		} else {
			logger.info(
					"Address : {} data Does not Exists in either vendorInputComponentRecords or addressAutoTaggingRes ",
					checkId);
		}
	}

	private String fullDateFormat(String dateStr) {
		logger.info("Date before format : {}", dateStr);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
		try {
			Date date = simpleDateFormat.parse(dateStr);
			simpleDateFormat = new SimpleDateFormat("MMMM-dd-yyyy");
			dateStr = simpleDateFormat.format(date);

			logger.info("Update date after format : {}", dateStr);
		} catch (ParseException e) {
			logger.error("Exception occurred while parsing date : {}", e.getMessage());
		}
		return dateStr;
	}

	private String checkPostMandate(String clientCode, String clientName, String sbuName, String packageName,
			String headerName, String componentName) {
		ObjectNode requestNode = mapper.createObjectNode();
		requestNode.put("clientCode", clientCode);
		requestNode.put("clientName", clientName);
		requestNode.put("sbuName", sbuName);
		requestNode.put("packageName", packageName);
		requestNode.put("category", componentName);
		requestNode.put("headerName", headerName);

		String response = apiService.sendDataToPost(addressMandateUrl, requestNode.toString());
		return response != null ? response : "";
	}

	private void validateAddressData(JsonNode checkInputRecords, JsonNode recordResult,
			CaseAndCheckDetailInterface caseAndCheckDetailInterface, String checkId, boolean isManual) {

		createVendorOutputVerificationEvent(caseAndCheckDetailInterface, checkId, "Initiated",
				"Request Initiated for Address");

		String inputCandidate = checkInputRecords.has("CandidateName") ? checkInputRecords.get("CandidateName").asText()
				: "";
		String inputAddr = checkInputRecords.has(ADDRESS2) ? checkInputRecords.get(ADDRESS2).asText() : "";

		String outputVerStatus = recordResult.has("Verification Status")
				? recordResult.get("Verification Status").asText()
				: "";
		String outputVerName = recordResult.has(VERIFIER_NAME) ? recordResult.get(VERIFIER_NAME).asText() : "";
		String outputRel = recordResult.has(RELATIONSHIP_WITH_CANDIDATE)
				? recordResult.get(RELATIONSHIP_WITH_CANDIDATE).asText()
				: "";
		String outputOwn = recordResult.has("Ownership Status") ? recordResult.get("Ownership Status").asText() : "";
		String outputSign = recordResult.has("Verifier Signature") ? recordResult.get("Verifier Signature").asText()
				: "";

		String outputCandidate = recordResult.has(NAME_OF_THE_CANDIDATE)
				? recordResult.get(NAME_OF_THE_CANDIDATE).asText()
				: "";
		String outputAddr = recordResult.has(ADDRESS_OF_THE_CANDIDATE)
				? recordResult.get(ADDRESS_OF_THE_CANDIDATE).asText()
				: "";

		inputCandidate = StringUtils.isNotEmpty(inputCandidate) ? inputCandidate.replaceAll("\\s+", " ")
				: inputCandidate;
		outputCandidate = StringUtils.isNotEmpty(outputCandidate) ? outputCandidate.replaceAll("\\s+", " ")
				: outputCandidate;

		if (Boolean.FALSE.equals(isManual) && StringUtils.isNotEmpty(inputCandidate)
				&& StringUtils.isNotEmpty(inputAddr) && StringUtils.isNotEmpty(outputVerName)
				&& StringUtils.isNotEmpty(outputRel) && StringUtils.isNotEmpty(outputOwn)
				&& StringUtils.isNotEmpty(outputSign) && StringUtils.isNotEmpty(outputAddr)
				&& StringUtils.isNotEmpty(outputCandidate)
				&& StringUtils.equalsIgnoreCase(outputVerStatus.trim(), "Verified Successfully")
				&& StringUtils.equalsIgnoreCase(inputCandidate.trim(), outputCandidate.trim())
				&& StringUtils.equalsIgnoreCase(inputAddr.trim(), outputAddr.trim())) {
			try {
				String checkIdFileName = checkId + ".pdf";
				vendorSftpConnectionService.copyFileToAnotherPath(remoteSharedFilePath,
						localFileDownloadLocation + checkIdFileName, checkIdFileName);

//========================== NOTE THIS IS A TEMPORARY Change ========================================================
//				String l3Response = sendDataToVerifyL3(caseAndCheckDetailInterface, recordResult, ADDRESS, false,
//						outputVerName);
//				createVerifiedAttempt(caseAndCheckDetailInterface, outputVerName, outputRel, checkId,
//						getL3Status(l3Response), l3Response);
//				createVendorOutputVerificationEvent(caseAndCheckDetailInterface, checkId, VERIFIED,
//						OUTPUT_FILE_VERIFIED);
				String l3Response = sendDataToVerifyL3(caseAndCheckDetailInterface, recordResult, ADDRESS, true,
						outputVerName);
				createFollowUpAttempt(caseAndCheckDetailInterface, outputVerName, outputRel, checkId,
						getL3Status(l3Response), l3Response);
				createVendorOutputVerificationEvent(caseAndCheckDetailInterface, checkId, MANUAL_TAGGING, F2);
//===================================================================================================================
			} catch (IOException e) {
				logger.error("Exception while mapping l3 request : {}", e.getMessage());
			}
		} else {
			try {
				String checkIdFileName = checkId + ".pdf";
				vendorSftpConnectionService.copyFileToAnotherPath(remoteSharedFilePath,
						localFileDownloadLocation + checkIdFileName, checkIdFileName);

				String l3Response = sendDataToVerifyL3(caseAndCheckDetailInterface, recordResult, ADDRESS, true,
						outputVerName);
				createFollowUpAttempt(caseAndCheckDetailInterface, outputVerName, outputRel, checkId,
						getL3Status(l3Response), l3Response);
				createVendorOutputVerificationEvent(caseAndCheckDetailInterface, checkId, MANUAL_TAGGING, F2);
			} catch (IOException e) {
				logger.error("Exception while mapping l3 request : {}", e.getMessage());
			}
		}
	}

	private String getL3Status(String l3Response) {
		if (StringUtils.equalsIgnoreCase(l3Response, L3_ERROR_RESPONSE)) {
			return "failed";
		} else {
			return "success";
		}
	}

	private VerificationEventStatus createVendorOutputVerificationEvent(
			CaseAndCheckDetailInterface caseAndCheckDetailInterface, String checkId, String status, String event) {

		Long requestId = (long) 0;
		String caseNo = "";
		if (caseAndCheckDetailInterface != null) {
			requestId = caseAndCheckDetailInterface.getCaseSpecificRecordId();
			caseNo = caseAndCheckDetailInterface.getCaseNumber();
		}

		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();
		verificationEventStatus.setStatus(status);
		verificationEventStatus.setEvent(event);

		verificationEventStatus.setCheckId(checkId);
		verificationEventStatus.setStage("Vendor");
		verificationEventStatus.setEventType("auto");
		verificationEventStatus.setCaseNo(caseNo);
		verificationEventStatus.setUserId(null);
		verificationEventStatus.setRequestId(requestId);
		return verificationEventStatusRepository.save(verificationEventStatus);

	}

	private String sendDataToVerifyL3(CaseAndCheckDetailInterface caseAndCheckDetailInterface, JsonNode recordNode,
			String type, boolean isManual, String verifierName) throws JsonProcessingException {
		String caseReferenceStr = caseAndCheckDetailInterface.getCaseReference();
		if (caseReferenceStr != null && StringUtils.isNotEmpty(caseReferenceStr)) {
			L3CaseReferencePOJO l3CaseReferencePOJO = mapper.readValue(caseAndCheckDetailInterface.getCaseReference(),
					L3CaseReferencePOJO.class);
			l3CaseReferencePOJO.setSbuName(caseAndCheckDetailInterface.getSbuName());
			l3CaseReferencePOJO.setPackageName(caseAndCheckDetailInterface.getPackageName());
			l3CaseReferencePOJO.setProductName(caseAndCheckDetailInterface.getProduct());
			l3CaseReferencePOJO.setComponentName(caseAndCheckDetailInterface.getComponentName());
			l3CaseReferencePOJO.setCheckId(caseAndCheckDetailInterface.getCheckId());
			if (Boolean.TRUE.equals(isManual)) {
				l3CaseReferencePOJO.setNgStatus("Vendor Verification received - Manual tagging");
				l3CaseReferencePOJO.setNgStatusDescription("Vendor Verification received - Manual tagging");
			} else {
				l3CaseReferencePOJO.setNgStatus(VERIFIED);
				l3CaseReferencePOJO.setNgStatusDescription(VERIFIED);
			}

			TaskSpecsPOJO taskSpecs = new TaskSpecsPOJO();
			taskSpecs.setCaseReference(l3CaseReferencePOJO);
			taskSpecs.setCheckVerification(
					getCheckVerification(caseAndCheckDetailInterface, type, isManual, verifierName));
			taskSpecs.setQuestionaire(getQuetionnaireData(caseAndCheckDetailInterface, recordNode, type));

			FileUploadPOJO fileUpload = new FileUploadPOJO();
			if (StringUtils.equalsIgnoreCase(type, ADDRESS) || StringUtils.equalsIgnoreCase(type, WRITTEN)) {
				fileUpload
						.setVerificationReplyDocument(Arrays.asList(caseAndCheckDetailInterface.getCheckId() + ".pdf"));
				fileUpload.setDirectory(verifyCheckFilePath);
				saveUploadFileAttempt(verifyCheckFilePathLocal, caseAndCheckDetailInterface.getCheckId() + ".pdf",
						caseAndCheckDetailInterface.getCheckId(),
						caseAndCheckDetailInterface.getCaseSpecificRecordId());
			} else {
				fileUpload.setVerificationReplyDocument(new ArrayList<>());
				fileUpload.setDirectory("");
			}
			taskSpecs.setFileUpload(fileUpload);

			String taskSpecsStr = mapper.writeValueAsString(taskSpecs);
			logger.info("l3 verification json : {} ", taskSpecsStr);

//			L3ApiRequestHistoryPOJO l3ApiRequestHistoryPOJO = saveInitiatedL3ApiRequest(verificationStatusL3Url,
//					caseAndCheckDetailInterface.getCaseNumber(), caseAndCheckDetailInterface.getCheckId(), taskSpecsStr,
//					"Vendor", "Verification", eventId)
			String l3VerifyResponse = apiService.sendDataToL3Post(verificationStatusL3Url, taskSpecsStr, null);
//			String l3VerifyResponse = null;
//			savePostL3ApiRequest(l3ApiRequestHistoryPOJO, mapper, l3VerifyResponse)

			if (l3VerifyResponse == null) {
				return L3_ERROR_RESPONSE;
			}
			return l3VerifyResponse;
		}
		return L3_ERROR_RESPONSE;
	}

	private void saveUploadFileAttempt(String path, String fileName, String checkId, Long requestId) {
		AttemptUploadDocument attemptUploadDocument = new AttemptUploadDocument();
		attemptUploadDocument.setCheckid(checkId);
		attemptUploadDocument.setComponentDocumentid((long) 38);
		attemptUploadDocument.setCreateDate(new Date());
		attemptUploadDocument.setRequestId(requestId);
		attemptUploadDocument.setDocumentPath(path);
		attemptUploadDocument.setFileName(fileName);
		attemptUploadDocumentRepository.save(attemptUploadDocument);
	}

	private CheckVerificationPOJO getCheckVerification(CaseAndCheckDetailInterface caseAndCheckDetailInterface,
			String type, Boolean isManual, String verifierName) {
		CheckVerificationPOJO checkVerification = new CheckVerificationPOJO();

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy");

		checkVerification.setCountry("India");
		checkVerification.setFollowUpDateAndTimes("");
		checkVerification.setExpectedClosureDate("");

		if (Boolean.TRUE.equals(isManual)) {
			checkVerification.setExecutiveSummaryComments("");
			checkVerification.setInternalNotes("Verification received from vendor, check moved for manual tagging");
			checkVerification.setModeOfVerification("Email");
			checkVerification.setEndStatusOfTheVerification("Pending for Reply");
			checkVerification.setDateVerificationCompleted("");
		} else {
			checkVerification.setDateVerificationCompleted(simpleDateFormat.format(new Date()));
			if (StringUtils.equalsIgnoreCase(type, ADDRESS)) {
				checkVerification.setExecutiveSummaryComments("Verified (Physical confirmation)");
				checkVerification.setInternalNotes("Physical address check, written uploaded");
				checkVerification.setModeOfVerification("Letter");
				checkVerification.setEndStatusOfTheVerification("Verified-Clear-Written");

			} else if (StringUtils.equalsIgnoreCase(type, VERBAL)) {
				checkVerification.setExecutiveSummaryComments(VERIFIED);
				checkVerification
						.setInternalNotes("We have received verbal verification from the educational institution");
				checkVerification.setModeOfVerification(VERBAL);
				checkVerification.setEndStatusOfTheVerification("Verified-Clear-Verbal");

			} else if (StringUtils.equalsIgnoreCase(type, WRITTEN)) {
				checkVerification.setExecutiveSummaryComments(VERIFIED);
				checkVerification
						.setInternalNotes("We have received written verification from the educational institution");
				checkVerification.setModeOfVerification("Email");
				checkVerification.setEndStatusOfTheVerification("Verified-Clear-Written");

			} else {
				checkVerification.setExecutiveSummaryComments("");
				checkVerification.setInternalNotes("");
				checkVerification.setModeOfVerification("");
				checkVerification.setEndStatusOfTheVerification("");
			}
		}
		
		//================================NOTE: THIS IS A TEMPORARY CHANGE============================
		checkVerification.setModeOfVerification("Letter");
		checkVerification.setEndStatusOfTheVerification("Response Received");
		//============================================================================================

		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		checkVerification.setEmailId("");
		checkVerification.setVerifierName(verifierName);
		checkVerification.setGeneralRemarks("");
		checkVerification.setVerifiedDate("");
		checkVerification.setAction("verifyChecks");
		checkVerification.setSubAction("verifyChecks");
		checkVerification.setActionCode("");
		checkVerification.setComponentName(caseAndCheckDetailInterface.getComponentName());
		checkVerification.setProductName(caseAndCheckDetailInterface.getProduct());
		checkVerification.setAttempts("Internal");
		checkVerification.setIsThisAVerificationAttempt("Yes");
		checkVerification.setDepartmentName("");
		checkVerification.setKeyWords("");
		checkVerification.setCost("");
		checkVerification.setDisposition("");
		checkVerification.setVerifierDesignation("");
		checkVerification.setVerifierNumber("");
		checkVerification.setMiRemarks("");
		return checkVerification;
	}

	private List<QuestionnairePOJO> getQuetionnaireData(CaseAndCheckDetailInterface caseAndCheckDetailInterface,
			JsonNode recordNode, String type) {
		ObjectNode requestNode = mapper.createObjectNode();
		requestNode.put("component", caseAndCheckDetailInterface.getComponentName());
		requestNode.put("productName", caseAndCheckDetailInterface.getProduct());
		if (Arrays.asList(VERBAL, WRITTEN).contains(type)) {
			requestNode.put("type", StringUtils.lowerCase(type));
		} else {
			requestNode.put("type", "");
		}
		logger.info("Request string for questionnaire mapping : {} ", requestNode);
		String responseStr = apiService.sendDataToPost(vendorQuestionnaireUrl, requestNode.toString());
		List<L3QuestionnairePOJO> l3QuestionnairePOJOs = getL3QuestionnaireDetails(caseAndCheckDetailInterface);

		Map<String, String> questionIdList = getGlobalQuestionIds(caseAndCheckDetailInterface);

		try {
			if (responseStr != null && StringUtils.isNotEmpty(responseStr)) {
				List<VendorQuestioneireMappingPOJO> vendorQuestioneireMappingPOJOs = mapper.readValue(responseStr,
						new TypeReference<List<VendorQuestioneireMappingPOJO>>() {
						});
				List<QuestionnairePOJO> questionnairePOJOs = new ArrayList<>();
				List<AttemptQuestionnaire> attemptQuestionnaires = new ArrayList<>();
				isIndividual = false;
				for (VendorQuestioneireMappingPOJO vendorQuestioneireMappingPOJO : vendorQuestioneireMappingPOJOs) {
					String formlabel = questionIdList.get(vendorQuestioneireMappingPOJO.getGlobalQuestionId());
					if (formlabel != null) {

						checkAllVendorQuestionnaires(caseAndCheckDetailInterface, recordNode, type,
								l3QuestionnairePOJOs, questionnairePOJOs, attemptQuestionnaires,
								vendorQuestioneireMappingPOJO, formlabel);
					}
				}

				questionnairePOJOs.stream().forEach(data -> {
					if (StringUtils.equals(data.getCaseQuestionRefID(), "807062") && isIndividual) {
						data.setReportData("");
					}
					if (Arrays
							.asList("relation with subject", "relationship with individual",
									"relationship with the individual", "relationship with the subject",
									"verifiers relation with subject")
							.contains(StringUtils.lowerCase(data.getQuestion())) && isIndividual) {
						data.setReportData("-");
					}
					if (Arrays.asList("verified by", "verifier name", "respondents name")
							.contains(StringUtils.lowerCase(data.getQuestion())) && isIndividual) {
						data.setReportData("The Subject");
					}
				});

				if (CollectionUtils.isNotEmpty(attemptQuestionnaires)) {
					attemptQuestionnaireRepository.saveAll(attemptQuestionnaires);
				}

				return questionnairePOJOs;
			}
		} catch (JsonProcessingException e) {
			logger.error("Exception occurred while mapping questionnaire resposne : {}", e.getMessage());
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	private Map<String, String> getGlobalQuestionIds(CaseAndCheckDetailInterface caseAndCheckDetailInterface) {
		List<GlobalQuestionAndFormLabelPOJO> questionIdList = new ArrayList<>();
		try {
			ObjectNode requestNode = mapper.createObjectNode();
			requestNode.put("componentName", caseAndCheckDetailInterface.getComponentName());
			requestNode.put("packageName", caseAndCheckDetailInterface.getPackageName());
			requestNode.put("productName", caseAndCheckDetailInterface.getProduct());

			String response = apiService.sendDataToPost(questionIdListUrl, requestNode.toString());
			questionIdList = mapper.readValue(response, new TypeReference<List<GlobalQuestionAndFormLabelPOJO>>() {
			});

		} catch (Exception e) {
			logger.error("Exception occurred while fetching global question id list : {}", e.getMessage());
		}
		Map<String, String> questionMap = questionIdList.stream()
				.collect(Collectors.toMap(GlobalQuestionAndFormLabelPOJO::getGlobalQuestionId,
						GlobalQuestionAndFormLabelPOJO::getFormLabel, (questionId1, questionId2) -> {
							logger.info("Duplicate QuestionID Found:{}", questionId1);
							return questionId1;
						}));
		logger.info("questionIdList : {}", questionIdList);

		return questionMap;
	}

	private void checkAllVendorQuestionnaires(CaseAndCheckDetailInterface caseAndCheckDetailInterface,
			JsonNode recordNode, String type, List<L3QuestionnairePOJO> l3QuestionnairePOJOs,
			List<QuestionnairePOJO> questionnairePOJOs, List<AttemptQuestionnaire> attemptQuestionnaires,
			VendorQuestioneireMappingPOJO vendorQuestioneireMappingPOJO, String formlabel) {
		if (vendorQuestioneireMappingPOJO.getFieldMapping() != null
				&& StringUtils.isNotEmpty(vendorQuestioneireMappingPOJO.getFieldMapping())) {

			AttemptQuestionnaire attemptQuestionnaire = new AttemptQuestionnaire();
			List<AttemptQuestionnaire> existingAttemptQuestionnaires = getExistingAttemptQuestionnaires(
					caseAndCheckDetailInterface, type, vendorQuestioneireMappingPOJO);
			List<L3QuestionnairePOJO> newL3QuestionnairePOJOs = l3QuestionnairePOJOs.stream().filter(data -> StringUtils
					.equalsIgnoreCase(data.getGlobalQuestionId(), vendorQuestioneireMappingPOJO.getGlobalQuestionId()))
					.collect(Collectors.toList());

			L3QuestionnairePOJO l3QuestionnairePOJO = CollectionUtils.isNotEmpty(newL3QuestionnairePOJOs)
					? newL3QuestionnairePOJOs.get(0)
					: null;

			if (existingAttemptQuestionnaires != null && CollectionUtils.isNotEmpty(existingAttemptQuestionnaires)) {
				attemptQuestionnaire = existingAttemptQuestionnaires.get(0);
			}
			attemptQuestionnaire.setCheckId(caseAndCheckDetailInterface.getCheckId());
			attemptQuestionnaire.setComponentName(caseAndCheckDetailInterface.getComponentName());
			attemptQuestionnaire.setProductName(caseAndCheckDetailInterface.getProduct());

			setAttemptQuestionnaireValues(recordNode, type, questionnairePOJOs, attemptQuestionnaires,
					vendorQuestioneireMappingPOJO, attemptQuestionnaire, l3QuestionnairePOJO, formlabel);
		}
	}

	private void setAttemptQuestionnaireValues(JsonNode recordNode, String type,
			List<QuestionnairePOJO> questionnairePOJOs, List<AttemptQuestionnaire> attemptQuestionnaires,
			VendorQuestioneireMappingPOJO vendorQuestioneireMappingPOJO, AttemptQuestionnaire attemptQuestionnaire,
			L3QuestionnairePOJO l3QuestionnairePOJO, String formlabel) {

		if (Arrays.asList("806813", "811105").contains(vendorQuestioneireMappingPOJO.getGlobalQuestionId())) {
			logger.info("Question Id : {}", vendorQuestioneireMappingPOJO.getGlobalQuestionId());
		}
		QuestionnairePOJO questionnairePOJO = new QuestionnairePOJO();

		String reportData = "";
		if (StringUtils.equalsIgnoreCase(vendorQuestioneireMappingPOJO.getFieldMapping(), "-")) {
			reportData = vendorQuestioneireMappingPOJO.getFieldMapping();
		} else {
			reportData = recordNode.has(vendorQuestioneireMappingPOJO.getFieldMapping())
					? recordNode.get(vendorQuestioneireMappingPOJO.getFieldMapping()).asText()
					: "";
			reportData = StringUtils.isEmpty(reportData) ? reportData
					: Utility.formatDateQuestions(reportData, vendorQuestioneireMappingPOJO.getGlobalQuestion().trim(),
							vendorQuestioneireMappingPOJO.getGlobalQuestionId().trim());
		}

		String status = vendorQuestioneireMappingPOJO.getStatus() != null ? vendorQuestioneireMappingPOJO.getStatus()
				: "";
		String verifiedData = vendorQuestioneireMappingPOJO.getVerifiedData() != null
				? vendorQuestioneireMappingPOJO.getVerifiedData()
				: "";

		if ((StringUtils.equalsIgnoreCase(vendorQuestioneireMappingPOJO.getGlobalQuestionId().trim(), "806813")
				|| StringUtils.equalsIgnoreCase(vendorQuestioneireMappingPOJO.getGlobalQuestionId().trim(), "811105"))
				&& StringUtils.equalsIgnoreCase(reportData, "The subject")) {
			isIndividual = true;
		}
		if (StringUtils.equalsIgnoreCase(vendorQuestioneireMappingPOJO.getFieldMapping().trim(),
				"Copy of written confirmation recieved is furnished below")) {
			reportData = "Copy of written confirmation recieved is furnished below";
		}
		questionnairePOJO.setQuestion(formlabel);
		questionnairePOJO.setCaseQuestionRefID(vendorQuestioneireMappingPOJO.getGlobalQuestionId());
		questionnairePOJO.setAnswer("");
		questionnairePOJO.setStatus(status);
		questionnairePOJO.setVerifiedData(verifiedData);
		questionnairePOJO.setReportData(reportData);

		attemptQuestionnaire.setCreateDate(new Date());
		attemptQuestionnaire.setGlobalQuestionId(vendorQuestioneireMappingPOJO.getGlobalQuestionId());
		attemptQuestionnaire.setQuestionName(formlabel);
		attemptQuestionnaire.setReportComments(reportData);
		attemptQuestionnaire.setStatus(status);
		attemptQuestionnaire.setVerifiedData(verifiedData);
		attemptQuestionnaire.setType(type);

		validateStatusAndVerifiedData(attemptQuestionnaire, questionnairePOJO, l3QuestionnairePOJO, status,
				verifiedData);

		questionnairePOJOs.add(questionnairePOJO);
		attemptQuestionnaires.add(attemptQuestionnaire);
	}

	private void validateStatusAndVerifiedData(AttemptQuestionnaire attemptQuestionnaire,
			QuestionnairePOJO questionnairePOJO, L3QuestionnairePOJO l3QuestionnairePOJO, String status,
			String verifiedData) {
		String applicationData = "";
		if (l3QuestionnairePOJO != null) {
			applicationData = Utility
					.formatString(l3QuestionnairePOJO.getAnswere() != null ? l3QuestionnairePOJO.getAnswere() : "");
			attemptQuestionnaire.setApplicationData(applicationData);
			attemptQuestionnaire.setMandatory(l3QuestionnairePOJO.isMandatory());
		}

		if (StringUtils.containsIgnoreCase(questionnairePOJO.getVerifiedData(),
				"Auto-population from Application data")) {
			attemptQuestionnaire.setVerifiedData(applicationData);
			questionnairePOJO.setVerifiedData(applicationData);
		}
		if (StringUtils.equalsIgnoreCase(verifiedData, "Blank")
				|| StringUtils.equalsIgnoreCase(verifiedData, "Blanks")) {
			attemptQuestionnaire.setVerifiedData("");
			questionnairePOJO.setVerifiedData("");
		}

		if (StringUtils.equalsIgnoreCase(status, "Blank") || StringUtils.equalsIgnoreCase(status, "Blanks")) {
			attemptQuestionnaire.setStatus("");
			questionnairePOJO.setStatus("");
		}
	}

	private List<AttemptQuestionnaire> getExistingAttemptQuestionnaires(
			CaseAndCheckDetailInterface caseAndCheckDetailInterface, String type,
			VendorQuestioneireMappingPOJO vendorQuestioneireMappingPOJO) {
		List<AttemptQuestionnaire> existingAttemptQuestionnaires;
		String componentName = caseAndCheckDetailInterface.getComponentName();
		String productName = caseAndCheckDetailInterface.getProduct();
		String checkId = caseAndCheckDetailInterface.getCheckId();
		String globalQuestionId = vendorQuestioneireMappingPOJO.getGlobalQuestionId();

		if (type != null && Arrays.asList(VERBAL, WRITTEN).contains(type)) {
			existingAttemptQuestionnaires = attemptQuestionnaireRepository
					.findByCheckIdAndGlobalQuestionIdAndComponentNameAndProductNameAndType(checkId, globalQuestionId,
							componentName, productName, type);
		} else {
			existingAttemptQuestionnaires = attemptQuestionnaireRepository
					.findByCheckIdAndGlobalQuestionIdAndComponentNameAndProductName(checkId, globalQuestionId,
							componentName, productName);
		}
		return existingAttemptQuestionnaires;
	}

	private List<L3QuestionnairePOJO> getL3QuestionnaireDetails(
			CaseAndCheckDetailInterface caseAndCheckDetailInterface) {
		String l3QuestionnaireRes = apiService
				.sendDataToL3Get(questionaireListL3Url + caseAndCheckDetailInterface.getCheckId());
		try {
			JsonNode l3QuestionnaireNode = mapper.readTree(l3QuestionnaireRes);

			ArrayNode responseNodeArr = l3QuestionnaireNode.has("response")
					? (ArrayNode) l3QuestionnaireNode.get("response")
					: mapper.createArrayNode();
			return mapper.convertValue(responseNodeArr, new TypeReference<List<L3QuestionnairePOJO>>() {
			});
		} catch (JsonProcessingException | IllegalArgumentException e) {
			logger.error("Exception while mapping l3 questionnaire response : {}", e.getMessage());
			return new ArrayList<>();
		}
	}

	public L3ApiRequestHistoryPOJO saveInitiatedL3ApiRequest(String requestUrl, String caseNumber, String checkId,
			String requestStr, String engineStage, String requestType, Long eventId) {
		L3ApiRequestHistoryPOJO l3ApiRequestHistoryPOJO = new L3ApiRequestHistoryPOJO();
		l3ApiRequestHistoryPOJO.setCheckId(checkId);
		l3ApiRequestHistoryPOJO.setCaseNumber(caseNumber);
		l3ApiRequestHistoryPOJO.setCreatedDate(new Date());
		l3ApiRequestHistoryPOJO.setUpdatedDate(new Date());
		l3ApiRequestHistoryPOJO.setEngineStage(engineStage);
		l3ApiRequestHistoryPOJO.setEventStatusId(eventId);
		JsonNode requestBody = mapper.createObjectNode();
		try {
			requestBody = requestStr != null ? mapper.readTree(requestStr) : mapper.createObjectNode();

			l3ApiRequestHistoryPOJO.setL3Request(requestBody);
			l3ApiRequestHistoryPOJO.setRequestUrl(requestUrl);
			l3ApiRequestHistoryPOJO.setRequestType(requestType);

			String l3ApiRequestHistoryStr = mapper.writeValueAsString(l3ApiRequestHistoryPOJO);
			logger.info("l3ApiRequestHistoryStr : {} ", l3ApiRequestHistoryPOJO);

			String response = apiService.sendDataToPost(l3RequestHistoryUrl, l3ApiRequestHistoryStr);
			logger.info("l3ApiRequestHistory response : {} ", response);

			return mapper.readValue(response, L3ApiRequestHistoryPOJO.class);

		} catch (JsonProcessingException e) {
			logger.error("Exception occurred while mapping l3response : {}", e.getMessage());
		}
		return new L3ApiRequestHistoryPOJO();
	}

	public void savePostL3ApiRequest(L3ApiRequestHistoryPOJO l3ApiRequestHistory, ObjectMapper mapper,
			String l3ResponseStr) {
		L3ApiRequestHistoryPOJO l3ApiRequestHistoryPOJO = new L3ApiRequestHistoryPOJO();
		l3ApiRequestHistoryPOJO.setL3ApiRequestHistoryId(l3ApiRequestHistory.getL3ApiRequestHistoryId());
		l3ApiRequestHistoryPOJO.setCheckId(l3ApiRequestHistory.getCheckId());
		l3ApiRequestHistoryPOJO.setCaseNumber(l3ApiRequestHistory.getCaseNumber());
		l3ApiRequestHistoryPOJO.setCreatedDate(l3ApiRequestHistory.getCreatedDate());
		l3ApiRequestHistoryPOJO.setUpdatedDate(new Date());
		l3ApiRequestHistoryPOJO.setEngineStage(l3ApiRequestHistory.getEngineStage());
		l3ApiRequestHistoryPOJO.setEventStatusId(l3ApiRequestHistory.getEventStatusId());
		l3ApiRequestHistoryPOJO.setL3Request(l3ApiRequestHistory.getL3Request());
		l3ApiRequestHistoryPOJO.setRequestUrl(l3ApiRequestHistory.getRequestUrl());
		l3ApiRequestHistoryPOJO.setRequestType(l3ApiRequestHistory.getRequestType());

		JsonNode l3Response = mapper.createObjectNode();
		try {
			l3Response = l3ResponseStr != null ? mapper.readTree(l3ResponseStr) : mapper.createObjectNode();
			l3ApiRequestHistoryPOJO.setL3Response(l3Response);
			l3ApiRequestHistoryPOJO.setResponseFlag("");

			String l3ApiRequestHistoryStr = mapper.writeValueAsString(l3ApiRequestHistoryPOJO);
			logger.info("l3ApiRequestHistoryStr : {} ", l3ApiRequestHistoryPOJO);

			String response = apiService.sendDataToPost(l3RequestHistoryUrl, l3ApiRequestHistoryStr);
			logger.info("l3ApiRequestHistory response : {} ", response);

		} catch (JsonProcessingException e) {
			logger.error("Exception occurred while mapping l3response : {}", e.getMessage());
		}
	}
}
