package com.gic.fadv.vendor.input.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.vendor.input.pojo.AddressInputHeaderPOJO;
import com.gic.fadv.vendor.input.pojo.CaseSpecificInfoPOJO;
import com.gic.fadv.vendor.input.pojo.CaseSpecificRecordDetailPOJO;
import com.gic.fadv.vendor.input.pojo.EducationInputHeaderPOJO;
import com.gic.fadv.vendor.model.AttemptHistory;
import com.gic.fadv.vendor.model.AttemptStatusData;
import com.gic.fadv.vendor.model.RouterHistory;
import com.gic.fadv.vendor.model.VendorInputComponentRecords;
import com.gic.fadv.vendor.model.VerificationEventStatus;
import com.gic.fadv.vendor.pojo.CheckVerificationPOJO;
import com.gic.fadv.vendor.pojo.FileUploadPOJO;
import com.gic.fadv.vendor.pojo.L3CaseReferencePOJO;
import com.gic.fadv.vendor.pojo.TaskSpecsPOJO;
import com.gic.fadv.vendor.repository.AttemptHistoryRepository;
import com.gic.fadv.vendor.repository.AttemptStatusDataRepository;
import com.gic.fadv.vendor.repository.RouterHistoryRepository;
import com.gic.fadv.vendor.repository.VendorInputComponentRecordsRepository;
import com.gic.fadv.vendor.repository.VerificationEventStatusRepository;
import com.gic.fadv.vendor.service.ApiService;
import com.gic.fadv.vendor.service.VendorSftpConnectionService;
import com.gic.fadv.vendor.utility.Utility;

@Service
public class VendorInputScheduledServiceImpl implements VendorInputScheduledService {

	private static final String CASE_SPECIFIC_RECORD_DETAIL = "caseSpecificRecordDetail";

	private static final String CASE_SPECIFIC_INFO = "caseSpecificInfo";

	private static final String UNIVERSITY_AKA_NAME = "University Aka Name";

	@Autowired
	private ApiService apiService;

	@Autowired
	private RouterHistoryRepository routerHistoryRepository;

	@Autowired
	private VerificationEventStatusRepository verificationEventStatusRepository;

	@Autowired
	private AttemptHistoryRepository attemptHistoryRepository;

	@Autowired
	private AttemptStatusDataRepository attemptStatusDataRepository;

	@Autowired
	private VendorInputComponentRecordsRepository vendorInputComponentRecordsRepository;

	@Value("${mrl.rule.description.url}")
	private String mrlRuleDescriptionUrl;

	@Value("${local.file.location}")
	private String localFileLocation;

	@Value("${associate.filepaths.rest.url}")
	private String associateFilePathUrl;

	@Value("${doc.url}")
	private String docUrl;

	@Value("${holiday.list.url}")
	private String holidayListUrl;

	@Value("${education.mrl.file.location}")
	private String educationFileLocation;

	@Value("${education.pdf.file.location}")
	private String educationPdfFileLocation;

	@Value("${verification.status.url.l3}")
	private String verificationStatusL3Url;

	@Autowired
	private VendorSftpConnectionService vendorSftpConnectionService;

	private static final Logger logger = LoggerFactory.getLogger(VendorInputScheduledServiceImpl.class);
	// ======================== Address File Headers ============================
	private static final String COMPONENT = "Component";
	private static final String CHECKID = "CheckID";
	private static final String ADDRESS = "Address";
	private static final String CASE_REF_NUMBER = "Case Reference Number";
	private static final String CLIENTNAME = "clientname";
	private static final String CANDIDATE_NAME = "CandidateName";
	private static final String FATHERS_NAME = "FathersName";
	private static final String DATE_FROM = "Date From";
	private static final String DATETO = "DateTo";
	private static final String CANDIDATE_CONTACT_NO = "Candidate's contact no.";

	private static final String ORG_NAME_BVF = "Org Name In BVF ";
	private static final String CHECKTAT = "CheckTAT";
	private static final String CHECK_INTERNAL_STATUS = "CheckInternalStatus";
	private static final String OPEN_FOR = "OpenFor";
	private static final String WORKABLE_WIP_CHECKS = "Workable WIP Checks";
	private static final String CLIENT_CODE = "Client Code";
	private static final String DUMMY = "Dummy";
	// ===========================================================================

	private static final String CLIENT_NAME = "Client Name";
	private static final String COMPLETE_ADDRESS = "Complete Address";
	private static final String FATHER_NAME = "Father’s Name";
	private static final String STATE = "State";
	private static final String STATUS_C = "Status";
	private static final String REMARKS = "Remarks";
	private static final String EDUCATION = "Education";
	// =============================================================================

	private static final String CHECK_ID = "checkId";
	private static final String DATE_FORMAT = "dd-MM-yyyy";
	private static final String FORWARD_SLASH = "/";
	private static final String ADDRESS_FILE = "Address_Input_Mapping.xlsx";
	private static final String EDUCATION_FILE = "Edu_Assignors_Log.xlsx";
	private static final String CHECK_ID2 = "Check ID";
	private static final String COUNTRY = "Country";
	private static final String STATUS = "Status";
	private static final String INITIATOR_NAME = "Initiator Name";
	private static final String QR_CODE = "QR Code";
	private static final String BATCH_SLOT = "Batch  / Slot";
	private static final String APPROVED_REJECTED = "Approved/Rejected";
	private static final String TOP_CLIENTS = "Top Clients";
	private static final String LH_VERIFICATION_COST = "LH Verification Cost";
	private static final String BT_DT_WT = "BT/DT/WT";
	private static final String NO_OF_CHECKS = "No of Checks";
	private static final String QUEUE_NAME = "Queue Name";
	private static final String CHECK_INTERNAL_STATUS2 = "Check Internal Status";
	private static final String DV_COMPLETION_TIME = "DV Completion Time";
	private static final String TL_NAME = "TL Name";
	private static final String VENDOR = "Vendor";
	private static final String DOCUMENT_AS_PER_MRL = "Document as per MRL";
	private static final String SUB_STATUS = "Sub Status";
	private static final String STELLAR_SENT = "Stellar Sent (Yes/No)";
	private static final String WRITTEN_MANDATE_CLIENT = "Written mandate Client";
	private static final String SPECIAL_NOTES = "Special Notes / Comments (re-verification / additional cost / any other special note)";
	private static final String CHECK_DUE_DATE2 = "Check Due date";
	private static final String CHECK_SUB_DATE = "Check Sub date";
	private static final String DOCUMENTS_SENT = "Document Sent";
	private static final String NUMBER_TYPE2 = "Number type2";
	private static final String NUMBER_TYPE1 = "Number type1";
	private static final String UNIVERSITY_NAME = "University Name";
	private static final String COLLEGE_NAME = "College Name";
	private static final String CANDIDATE_NAME2 = "Candidate Name";
	private static final String POSTAL_ZIP_CODE_PIN_CODE = "Postal Zip Code Pin Code";
	private static final String VENDOR_INPUT = "VendorInput";
	private static final String INITIATED = "Initiated";
	private static final String L3_ERROR_RESPONSE = "{\"Error\": \"Error as reponse from L3\"}";
	private static final String SUCCESS = "success";
	private static final String PROCESSED = "Processed";
	private static final String UNIQUE_NUMBER = "Unique Number";
	private static final String UNIQUE_NUMBER_TYPE = "Unique Number Type";
	private static final String YEAR_OF_PASSING = "Year of Passing";

	private static final List<String> candidateDataEntryKeys = Arrays.asList("educationmarksheetasperdocument",
			"admitcard", "certificate", "consolidatedmarksheet", "degreecertificate", "groupi", "groupii",
			"pccumconsolidatedmarksheetasperdocument", "passingcertificate", "provisionalcertificate",
			"registrationreceipt", "renewalslip", "statementofmarks", "transcript", "finalyearmarksheet",
			"allyearmarksheets", "1styearsemestermarksheet", "2ndyearsemestermarksheet", "3rdyearsemestermarksheet",
			"4thyearsemestermarksheet", "5thyearsemestermarksheet", "6thsemestermarksheet", "7thsemestermarksheet",
			"8thsemestermarksheet", "9thsemestermarksheet");

	private static final List<String> educationDataEntryKeys = Arrays.asList("educationdetailasperbvf",
			"educationmarksheetasperdocument", "admitcard", "certificate", "consolidatedmarksheet", "degreecertificate",
			"groupi", "groupii", "pccumconsolidatedmarksheetasperdocument", "passingcertificate",
			"provisionalcertificate", "registrationreceipt", "renewalslip", "statementofmarks", "transcript",
			"finalyearmarksheet", "allyearmarksheets", "1styearsemestermarksheet", "2ndyearsemestermarksheet",
			"3rdyearsemestermarksheet", "4thyearsemestermarksheet", "5thyearsemestermarksheet", "6thsemestermarksheet",
			"7thsemestermarksheet", "8thsemestermarksheet", "9thsemestermarksheet");

	private static final List<String> ADDRESS_DE_KEY = Arrays.asList("addressdetailscurrentasperbvf",
			"addressdetailspermanentasperbvf", "addresspreviousasperbvf", "personaldetailsasperbvf",
			"currentandpermanentaddressasperbvf", "addressdetailsasperbvf");

	@Override
	public void getVendorRequests() {
		List<RouterHistory> routerHistories = routerHistoryRepository
				.findByEngineNameAndCurrentEngineStatus(VENDOR_INPUT, INITIATED);

		logger.info("------------Scheduler -> Vendor Input : Router History Size {}------------",
				routerHistories.size());

		List<AddressInputHeaderPOJO> addressInputHeaders = new ArrayList<>();
		List<EducationInputHeaderPOJO> educationInputHeaders = new ArrayList<>();

		addressInputHeaders.add(generateAddressInputHeader());
		educationInputHeaders.add(generateEducationInputHeader());

		for (RouterHistory routerHistory : routerHistories) {
			try {
				processRequestBody(routerHistory.getEngineRequest(), addressInputHeaders, educationInputHeaders);
			} catch (JsonProcessingException e) {
				logger.error("Exception occured while mapping router request for check Id : {}",
						routerHistory.getCheckId());
			}
		}
		logger.info("-------------Scheduler -> Vendor Input : Address File Size {}-------------",
				addressInputHeaders.size());
		logger.info("------------Scheduler -> Vendor Input : Education File Size {}------------",
				educationInputHeaders.size());

		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy_HHmm");
		String uploadTime = dateFormat.format(new Date());

		String fileExtension = ".xlsx";
		String educationFileName = "Edu_Assignors_Log" + "_" + uploadTime + fileExtension;
		String addressFileName = "Address_Input_Mapping" + "_" + uploadTime + fileExtension;
		logger.info("-------------Scheduler -> Vendor Input : Address FileName {}-------------", addressFileName);
		logger.info("------------Scheduler -> Vendor Input : Education Filename {}------------", educationFileName);

		if (addressInputHeaders.size() > 1) {
			String fileLocation = localFileLocation + ADDRESS_FILE;
			logger.info("Address Input File Location : {}", fileLocation);
			try {
				Utility.writeAddressExcel(fileLocation, addressInputHeaders);
				uploadFileToSftp(ADDRESS, addressFileName, fileLocation);
			} catch (IOException e) {
				logger.error("Exception occured while creating Address excel : {}", e.getMessage());
			}
		}
		if (educationInputHeaders.size() > 1) {
			String fileLocation = localFileLocation + EDUCATION_FILE;
			logger.info("Education Input File Location : {}", fileLocation);
			try {
				Utility.writeEducationExcel(fileLocation, educationInputHeaders);
				uploadFileToSftp(EDUCATION, educationFileName, fileLocation);
			} catch (IOException e) {
				logger.error("Exception occured while creating Education excel : {}", e.getMessage());
			}
		}
		if (addressInputHeaders.size() > 1 || educationInputHeaders.size() > 1) {
			List<RouterHistory> newRouterHistories = getFilteredRouterHistory(addressInputHeaders,
					educationInputHeaders, routerHistories);
			logger.info("------------Scheduler -> Vendor Input : newRouterHistories Size {}------------",
					newRouterHistories.size());
			postProcessing(newRouterHistories, addressInputHeaders, educationInputHeaders);
		}
	}

	private List<RouterHistory> getFilteredRouterHistory(List<AddressInputHeaderPOJO> addressInputHeaders,
			List<EducationInputHeaderPOJO> educationInputHeaders, List<RouterHistory> routerHistories) {
		List<String> educationCheckIdList = educationInputHeaders.stream().map(EducationInputHeaderPOJO::getCheckID)
				.collect(Collectors.toList());
		List<String> addressCheckIdList = addressInputHeaders.stream().map(AddressInputHeaderPOJO::getCheckID)
				.collect(Collectors.toList());
		educationCheckIdList.remove(CHECK_ID2);
		addressCheckIdList.remove(CHECKID);

		educationCheckIdList.addAll(addressCheckIdList);
		List<String> checkIdList = new ArrayList<>(new HashSet<>(educationCheckIdList));
		logger.info("------------Scheduler -> Vendor Input : List of checkIds processed {}------------", checkIdList);

		return routerHistories.stream().filter(data -> checkIdList.contains(data.getCheckId()))
				.collect(Collectors.toList());
	}

	private void postProcessing(List<RouterHistory> routerHistories, List<AddressInputHeaderPOJO> addressInputHeaders,
			List<EducationInputHeaderPOJO> educationInputHeaders) {

		for (RouterHistory routerHistory : routerHistories) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JsonNode requestNode = routerHistory.getEngineRequest();

			if (!requestNode.isEmpty()) {
				JsonNode caseSpecificInfoNode = requestNode.has(CASE_SPECIFIC_INFO)
						? requestNode.get(CASE_SPECIFIC_INFO)
						: mapper.createObjectNode();
				JsonNode caseSpecificRecordDetailNode = requestNode.has(CASE_SPECIFIC_RECORD_DETAIL)
						? requestNode.get(CASE_SPECIFIC_RECORD_DETAIL)
						: mapper.createObjectNode();

				CaseSpecificInfoPOJO caseSpecificInfoPOJO = mapper.convertValue(caseSpecificInfoNode,
						CaseSpecificInfoPOJO.class);
				CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO = mapper
						.convertValue(caseSpecificRecordDetailNode, CaseSpecificRecordDetailPOJO.class);

				JsonNode inputFileNode = mapper.createObjectNode();
				if (StringUtils.equalsIgnoreCase(caseSpecificRecordDetailPOJO.getComponentName(), ADDRESS)) {
					inputFileNode = getAddressInputJsonNode(mapper, addressInputHeaders,
							caseSpecificRecordDetailPOJO.getInstructionCheckId());
				} else if (StringUtils.equalsIgnoreCase(caseSpecificRecordDetailPOJO.getComponentName(), EDUCATION)) {
					inputFileNode = getEducationInputJsonNode(mapper, educationInputHeaders,
							caseSpecificRecordDetailPOJO.getInstructionCheckId());
				}

				finalAtemptsAndCspiTagging(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO, routerHistory,
						inputFileNode);
			}
		}

	}

	private ObjectNode getAddressInputJsonNode(ObjectMapper mapper, List<AddressInputHeaderPOJO> addressInputHeaders,
			String checkId) {
		AddressInputHeaderPOJO addressInputHeader = addressInputHeaders.get(0);
		List<AddressInputHeaderPOJO> addressInputHeaderChecks = addressInputHeaders.stream()
				.filter(data -> StringUtils.equals(data.getCheckID(), checkId)).collect(Collectors.toList());
		ObjectNode addressNode = mapper.createObjectNode();
		if (CollectionUtils.isNotEmpty(addressInputHeaderChecks)) {
			AddressInputHeaderPOJO newAddressInputHeader = addressInputHeaderChecks.get(0);
			addressNode.put(addressInputHeader.getComponent(), newAddressInputHeader.getComponent());
			addressNode.put(addressInputHeader.getCheckID(), newAddressInputHeader.getCheckID());
			addressNode.put(addressInputHeader.getCaseReferenceNumber(),
					newAddressInputHeader.getCaseReferenceNumber());
			addressNode.put(addressInputHeader.getClientname(), newAddressInputHeader.getClientname());
			addressNode.put(addressInputHeader.getCandidateName(), newAddressInputHeader.getCandidateName());
			addressNode.put(addressInputHeader.getAddress(), newAddressInputHeader.getAddress());
			addressNode.put(addressInputHeader.getFathersName(), newAddressInputHeader.getFathersName());
			addressNode.put(addressInputHeader.getDateFrom(), newAddressInputHeader.getDateFrom());
			addressNode.put(addressInputHeader.getDateTo(), newAddressInputHeader.getDateTo());
			addressNode.put(addressInputHeader.getCandidatesContactNos(),
					newAddressInputHeader.getCandidatesContactNos());
			addressNode.put(addressInputHeader.getOrgNameInBVF(), newAddressInputHeader.getOrgNameInBVF());
			addressNode.put(addressInputHeader.getCheckTAT(), newAddressInputHeader.getCheckTAT());
			addressNode.put(addressInputHeader.getCheckInternalStatus(),
					newAddressInputHeader.getCheckInternalStatus());
			addressNode.put(addressInputHeader.getOpenFor(), newAddressInputHeader.getOpenFor());
			addressNode.put(addressInputHeader.getWorkableWIPChecks(), newAddressInputHeader.getWorkableWIPChecks());
			addressNode.put(addressInputHeader.getClientCode(), newAddressInputHeader.getClientCode());
			addressNode.put(addressInputHeader.getDummy(), newAddressInputHeader.getDummy());
		}
		return addressNode;
	}

	private ObjectNode getEducationInputJsonNode(ObjectMapper mapper,
			List<EducationInputHeaderPOJO> educationInputHeaders, String checkId) {
		EducationInputHeaderPOJO educationInputHeader = educationInputHeaders.get(0);
		List<EducationInputHeaderPOJO> educationInputHeaderChecks = educationInputHeaders.stream()
				.filter(data -> StringUtils.equals(data.getCheckID(), checkId)).collect(Collectors.toList());
		ObjectNode educationNode = mapper.createObjectNode();
		if (CollectionUtils.isNotEmpty(educationInputHeaderChecks)) {
			EducationInputHeaderPOJO newEducationInputHeader = educationInputHeaderChecks.get(0);
			educationNode.put(educationInputHeader.getDateOfInitiation(),
					newEducationInputHeader.getDateOfInitiation());
			educationNode.put(educationInputHeader.getComponent(), newEducationInputHeader.getComponent());
			educationNode.put(educationInputHeader.getCheckID(), newEducationInputHeader.getCheckID());
			educationNode.put(educationInputHeader.getClientName(), newEducationInputHeader.getClientName());
			educationNode.put(educationInputHeader.getCaseNumber(), newEducationInputHeader.getCaseNumber());
			educationNode.put(educationInputHeader.getCandidateName(), newEducationInputHeader.getCandidateName());
			educationNode.put(educationInputHeader.getCollegeName(), newEducationInputHeader.getCollegeName());
			educationNode.put(educationInputHeader.getUniversityName(), newEducationInputHeader.getUniversityName());
			educationNode.put(educationInputHeader.getQualification(), newEducationInputHeader.getQualification());
			educationNode.put(educationInputHeader.getMajor(), newEducationInputHeader.getMajor());
			educationNode.put(educationInputHeader.getNumbertype1(), newEducationInputHeader.getNumbertype1());
			educationNode.put(educationInputHeader.getUniqueno1(), newEducationInputHeader.getUniqueno1());
			educationNode.put(educationInputHeader.getNumbertype2(), newEducationInputHeader.getNumbertype2());
			educationNode.put(educationInputHeader.getUniqueno2(), newEducationInputHeader.getUniqueno2());
			educationNode.put(educationInputHeader.getYearofPassing(), newEducationInputHeader.getYearofPassing());
			educationNode.put(educationInputHeader.getYearofGraduation(),
					newEducationInputHeader.getYearofGraduation());
			educationNode.put(educationInputHeader.getClassObtained(), newEducationInputHeader.getClassObtained());
			educationNode.put(educationInputHeader.getDocumentsSent(), newEducationInputHeader.getDocumentsSent());
			educationNode.put(educationInputHeader.getCheckSubdate(), newEducationInputHeader.getCheckSubdate());
			educationNode.put(educationInputHeader.getCheckDuedate(), newEducationInputHeader.getCheckDuedate());
			educationNode.put(educationInputHeader.getInitiatiorsName(), newEducationInputHeader.getInitiatiorsName());
			educationNode.put(educationInputHeader.getSpecialNotesComments(),
					newEducationInputHeader.getSpecialNotesComments());
			educationNode.put(educationInputHeader.getWrittenmandateClient(),
					newEducationInputHeader.getWrittenmandateClient());
			educationNode.put(educationInputHeader.getStellarSent(), newEducationInputHeader.getStellarSent());
			educationNode.put(educationInputHeader.getStatus(), newEducationInputHeader.getStatus());
			educationNode.put(educationInputHeader.getSubStatus(), newEducationInputHeader.getSubStatus());
			educationNode.put(educationInputHeader.getDocumentAsPerMRL(),
					newEducationInputHeader.getDocumentAsPerMRL());
			educationNode.put(educationInputHeader.getLocation(), newEducationInputHeader.getLocation());
			educationNode.put(educationInputHeader.getVendor(), newEducationInputHeader.getVendor());
			educationNode.put(educationInputHeader.getTLName(), newEducationInputHeader.getTLName());
			educationNode.put(educationInputHeader.getPOC(), newEducationInputHeader.getPOC());
			educationNode.put(educationInputHeader.getDVCompletionTime(),
					newEducationInputHeader.getDVCompletionTime());
			educationNode.put(educationInputHeader.getLDD(), newEducationInputHeader.getLDD());
			educationNode.put(educationInputHeader.getCheckInternalStatus(),
					newEducationInputHeader.getCheckInternalStatus());
			educationNode.put(educationInputHeader.getQueueName(), newEducationInputHeader.getQueueName());
			educationNode.put(educationInputHeader.getNoOfChecks(), newEducationInputHeader.getNoOfChecks());
			educationNode.put(educationInputHeader.getBTDTWT(), newEducationInputHeader.getBTDTWT());
			educationNode.put(educationInputHeader.getLHVerificationCost(),
					newEducationInputHeader.getLHVerificationCost());
			educationNode.put(educationInputHeader.getTopClients(), newEducationInputHeader.getTopClients());
			educationNode.put(educationInputHeader.getApprovedRejected(),
					newEducationInputHeader.getApprovedRejected());
			educationNode.put(educationInputHeader.getRemarks(), newEducationInputHeader.getRemarks());
			educationNode.put(educationInputHeader.getBatchSlot(), newEducationInputHeader.getBatchSlot());
			educationNode.put(educationInputHeader.getQRCode(), newEducationInputHeader.getQRCode());
			logger.info("educationNode : {}", educationNode);
		}
		return educationNode;
	}

	private void finalAtemptsAndCspiTagging(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, RouterHistory routerHistory,
			JsonNode inputFileNode) {

		String response = apiService.sendDataToGet(holidayListUrl);
		response = response.replace("[", "").replace("]", "").replace("\"", "");

		String[] split = response.split(",");
		List<String> holidayList = Arrays.asList(split);
		holidayList.replaceAll(String::trim);

		logger.info("holidayList : {}", holidayList);

		String componentName = caseSpecificRecordDetailPOJO.getComponentName() != null
				? caseSpecificRecordDetailPOJO.getComponentName()
				: "";
		createVendorInputPostVerificationEvent(componentName, caseSpecificRecordDetailPOJO, caseSpecificInfoPOJO);
		String l3response = "";
		try {
			l3response = sendVerifyDataToL3(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO, holidayList);
		} catch (JsonProcessingException e) {
			logger.error("Exception while sending verifycheck to l3 for check Id : {} : {}", routerHistory.getCheckId(),
					e.getMessage());
			l3response = L3_ERROR_RESPONSE;
		}
		createVendorAttempt(caseSpecificRecordDetailPOJO, l3response, holidayList);
		createVendorInputComponent(caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO, inputFileNode);
		routerHistory.setCurrentEngineStatus(PROCESSED);
		ObjectNode engineResponse = mapper.createObjectNode();

		engineResponse.put("status", 200);
		engineResponse.put("engine", "vendorInput");
		engineResponse.put("result", "Input file created");
		engineResponse.put("l3Status", SUCCESS);
		engineResponse.put("l3Response", l3response);
		engineResponse.put(SUCCESS, true);
		routerHistory.setEngineResponse(engineResponse);
		routerHistory.setEndTime(new Date());
		routerHistoryRepository.save(routerHistory);
	}

	private void createVendorInputComponent(CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, JsonNode inputFileNode) {
		VendorInputComponentRecords vendorInputComponentRecords = new VendorInputComponentRecords();
		vendorInputComponentRecords.setCaseNumber(caseSpecificInfoPOJO.getCaseNumber());
		vendorInputComponentRecords.setCheckId(caseSpecificRecordDetailPOJO.getInstructionCheckId());
		vendorInputComponentRecords.setComponentName(caseSpecificRecordDetailPOJO.getComponentName());
		vendorInputComponentRecords.setSubComponentName(caseSpecificRecordDetailPOJO.getProduct());
		vendorInputComponentRecords.setCheckRecord(inputFileNode);
		vendorInputComponentRecords.setCreatedDate(new Date());
		List<VendorInputComponentRecords> vendorInputComponentRecordsList = vendorInputComponentRecordsRepository
				.findByCheckId(caseSpecificRecordDetailPOJO.getInstructionCheckId());
		if (CollectionUtils.isNotEmpty(vendorInputComponentRecordsList)) {
			vendorInputComponentRecords.setId(vendorInputComponentRecordsList.get(0).getId());
		}
		vendorInputComponentRecordsRepository.save(vendorInputComponentRecords);
	}

	private void processRequestBody(JsonNode requestNode, List<AddressInputHeaderPOJO> addressInputHeaders,
			List<EducationInputHeaderPOJO> educationInputHeaders) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		if (!requestNode.isEmpty()) {
			JsonNode caseSpecificInfoNode = requestNode.has(CASE_SPECIFIC_INFO) ? requestNode.get(CASE_SPECIFIC_INFO)
					: mapper.createObjectNode();
			JsonNode caseSpecificRecordDetailNode = requestNode.has(CASE_SPECIFIC_RECORD_DETAIL)
					? requestNode.get(CASE_SPECIFIC_RECORD_DETAIL)
					: mapper.createObjectNode();

			JsonNode otherDetails = requestNode.has("otherDetails") ? requestNode.get("otherDetails")
					: mapper.createObjectNode();

			CaseSpecificInfoPOJO caseSpecificInfoPOJO = mapper.convertValue(caseSpecificInfoNode,
					CaseSpecificInfoPOJO.class);
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO = mapper
					.convertValue(caseSpecificRecordDetailNode, CaseSpecificRecordDetailPOJO.class);

			String recordStr = caseSpecificRecordDetailPOJO.getComponentRecordField() != null
					? caseSpecificRecordDetailPOJO.getComponentRecordField()
					: "{}";
			JsonNode recordNode = mapper.readValue(recordStr, JsonNode.class);
			String componentName = caseSpecificRecordDetailPOJO.getComponentName() != null
					? caseSpecificRecordDetailPOJO.getComponentName()
					: "";

			if (StringUtils.equalsIgnoreCase(componentName, EDUCATION)) {
				generateEducationInputFile(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO, recordNode,
						otherDetails, educationInputHeaders);
			} else if (StringUtils.equalsIgnoreCase(componentName, ADDRESS)) {
				generateAddressInputFile(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO, recordNode,
						addressInputHeaders);
			}
		}
	}

	private void generateAddressInputFile(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, JsonNode recordNode,
			List<AddressInputHeaderPOJO> addressInputHeaders) throws JsonMappingException, JsonProcessingException {
		// Take Out Data Entry Value
		String dataEntryStr = caseSpecificInfoPOJO.getDataEntryInfo() != null ? caseSpecificInfoPOJO.getDataEntryInfo()
				: "{}";
		JsonNode dataEntryNode = mapper.createObjectNode();
		try {
			dataEntryNode = mapper.readTree(dataEntryStr);
		} catch (JsonProcessingException e1) {
			logger.error("Exception occurred while mapping data entry to json node : {}", e1.getMessage());
		}

		String contactNumber = recordNode.has("Mobile Number") ? recordNode.get("Mobile Number").asText() : "";
		contactNumber = getDataFromDataEntryKeyVal(dataEntryNode, ADDRESS_DE_KEY, "mobilenumber", contactNumber);

		if (StringUtils.isEmpty(contactNumber)) {
			//personalmobileno
			contactNumber = recordNode.has("Personal Mobile Number") ? recordNode.get("Personal Mobile Number").asText()
					: "";
			contactNumber = getDataFromDataEntryKeyVal(dataEntryNode, ADDRESS_DE_KEY, "personalmobileno",
					contactNumber);
		}

		if (StringUtils.isEmpty(contactNumber)) {
			//homephone
			contactNumber = recordNode.has("Home Phone") ? recordNode.get("Home Phone").asText() : "";
			contactNumber = getDataFromDataEntryKeyVal(dataEntryNode, ADDRESS_DE_KEY, "homephone", contactNumber);
		}
		String fathersName = recordNode.has(FATHER_NAME) ? recordNode.get(FATHER_NAME).asText() : "";
		if (StringUtils.isEmpty(fathersName)) {
			fathersName = recordNode.has("Fathers Name") ? recordNode.get("Fathers Name").asText() : "";
		}
		fathersName = getDataFromDataEntryKeyVal(dataEntryNode, ADDRESS_DE_KEY, "fathersname", fathersName);

		String city = recordNode.has("City") ? recordNode.get("City").asText() : "";
		String dateFrom = recordNode.has("Period of Stay - From")
				? Utility.formatDateUtil(recordNode.get("Period of Stay - From").asText())
				: "";
		String dateTo = recordNode.has("Period of Stay - To")
				? Utility.formatDateUtil(recordNode.get("Period of Stay - To").asText())
				: "";

		dateFrom = StringUtils.isEmpty(dateFrom) ? "NA/NA/NA" : dateFrom;
		dateTo = StringUtils.isEmpty(dateTo) ? "NA/NA/NA" : dateTo;

		String checkTat = recordNode.has(CHECKTAT) ? recordNode.get(CHECKTAT).asText() : "";
		String dummy = recordNode.has(DUMMY) ? recordNode.get(DUMMY).asText() : "";

		AddressInputHeaderPOJO addressInputHeader = new AddressInputHeaderPOJO();
		addressInputHeader.setComponent(ADDRESS);
		addressInputHeader.setCheckID(caseSpecificRecordDetailPOJO.getInstructionCheckId());
		addressInputHeader.setCaseReferenceNumber(caseSpecificInfoPOJO.getCaseRefNumber());
		addressInputHeader.setClientname(caseSpecificInfoPOJO.getClientName());
		addressInputHeader.setClientCode(caseSpecificInfoPOJO.getClientCode());
		addressInputHeader.setCandidateName(caseSpecificInfoPOJO.getCandidateName());
		addressInputHeader.setFathersName(fathersName);
		addressInputHeader.setCandidatesContactNos(contactNumber);
		addressInputHeader.setCheckInternalStatus("Ready To Send Vendor");
		addressInputHeader.setOpenFor("Open For FR");
		addressInputHeader.setWorkableWIPChecks("1");
		addressInputHeader.setOrgNameInBVF(city);
		addressInputHeader.setCheckTAT(checkTat);
		addressInputHeader.setDateFrom(dateFrom);
		addressInputHeader.setDateTo(dateTo);
		addressInputHeader.setDummy(dummy);
		if (checkTat == null || StringUtils.isEmpty(checkTat)) {
			addressInputHeader.setCheckTAT("0");
		}
		addressInputHeader.setAddress(getAddressDetails(recordNode, city));

		addressInputHeaders.add(addressInputHeader);
	}

	private String getAddressDetails(JsonNode recordNode, String city) {

		String completeAddress = recordNode.has(COMPLETE_ADDRESS) ? recordNode.get(COMPLETE_ADDRESS).asText() : "";
		String houseFlatNumber = recordNode.has("House / Flat Number") ? recordNode.get("House / Flat Number").asText()
				: "";
		String buildingNumberName = recordNode.has("Building Number & Name")
				? recordNode.get("Building Number & Name").asText()
				: "";
		String streetLaneName = recordNode.has("Street Name / Lane Name")
				? recordNode.get("Street Name / Lane Name").asText()
				: "";
		String landmark = recordNode.has("Landmark") ? recordNode.get("Landmark").asText() : "";
		String pin = recordNode.has(POSTAL_ZIP_CODE_PIN_CODE) ? recordNode.get(POSTAL_ZIP_CODE_PIN_CODE).asText() : "";
		String state = recordNode.has(STATE) ? recordNode.get(STATE).asText() : "";
		String country = recordNode.has(COUNTRY) ? recordNode.get(COUNTRY).asText() : "";
		String district = recordNode.has("District") ? recordNode.get("District").asText() : "";

		List<String> addressList = new ArrayList<>();
		if (completeAddress != null && StringUtils.isNotEmpty(completeAddress)) {
			addressList.add(completeAddress);
			checkAddressComponents1(city, pin, state, country, district, addressList);
		} else {
			checkAddressComponents2(houseFlatNumber, buildingNumberName, streetLaneName, landmark, addressList);
			checkAddressComponents1(city, pin, state, country, district, addressList);
		}
		String addressChange = String.join(", ", addressList);
		addressChange = StringUtils.replace(addressChange, "#dot#", ".");
		return addressChange;
	}

	private void checkAddressComponents2(String houseFlatNumber, String buildingNumberName, String streetLaneName,
			String landmark, List<String> addressList) {
		if (houseFlatNumber != null && StringUtils.isNotEmpty(houseFlatNumber)) {
			addressList.add(houseFlatNumber);
		}
		if (buildingNumberName != null && StringUtils.isNotEmpty(buildingNumberName)) {
			addressList.add(buildingNumberName);
		}
		if (streetLaneName != null && StringUtils.isNotEmpty(streetLaneName)) {
			addressList.add(streetLaneName);
		}
		if (landmark != null && StringUtils.isNotEmpty(landmark)) {
			addressList.add(landmark);
		}
	}

	private void checkAddressComponents1(String city, String pin, String state, String country, String district,
			List<String> addressList) {
		if (city != null && StringUtils.isNotEmpty(city)) {
			addressList.add(city);
		}
		if (district != null && StringUtils.isNotEmpty(district)) {
			addressList.add(district);
		}
		if (pin != null && StringUtils.isNotEmpty(pin)) {
			addressList.add(pin);
		}
		if (state != null && StringUtils.isNotEmpty(state)) {
			addressList.add(state);
		}
		if (country != null && StringUtils.isNotEmpty(country)) {
			addressList.add(country);
		}
	}

	private void generateEducationInputFile(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, JsonNode recordNode, JsonNode otherDetails,
			List<EducationInputHeaderPOJO> educationInputHeaders) {
		String universityAkaName = recordNode.has(UNIVERSITY_AKA_NAME) ? recordNode.get(UNIVERSITY_AKA_NAME).asText()
				: "";
		logger.info("Value of University AKA Name : {}", universityAkaName);

		String dataEntryStr = caseSpecificInfoPOJO.getDataEntryInfo() != null ? caseSpecificInfoPOJO.getDataEntryInfo()
				: "{}";
		JsonNode dataEntryNode = mapper.createObjectNode();
		try {
			dataEntryNode = mapper.readTree(dataEntryStr);
		} catch (JsonProcessingException e1) {
			logger.error("Exception occurred while mapping data entry to json node : {}", e1.getMessage());
		}

		String documentsName = "";
		List<String> documentList = getMrlDocumentNames(mapper, EDUCATION, universityAkaName,
				caseSpecificRecordDetailPOJO.getInstructionCheckId(), caseSpecificInfoPOJO.getCaseNumber());
		if (CollectionUtils.isNotEmpty(documentList)) {
			documentsName = String.join(", ", documentList);
		}
		EducationInputHeaderPOJO educationInputHeader = new EducationInputHeaderPOJO();
		getEducationDetails1(caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO, otherDetails, documentsName,
				educationInputHeader);

		getEducationDetails2(recordNode, educationInputHeader, dataEntryNode);
		getEducationDetails3(recordNode, educationInputHeader, dataEntryNode);

		String qrCode = recordNode.has(QR_CODE) ? recordNode.get(QR_CODE).asText() : "";
		educationInputHeader
				.setQRCode(getDataFromDataEntryKeyVal(dataEntryNode, educationDataEntryKeys, "qrcode", qrCode));
		educationInputHeader.setFilePath(recordNode.has("FilePath") ? recordNode.get("FilePath").asText() : "");

		setEducationDefault1(educationInputHeader);
		getEducationCollegeDetails(recordNode, educationInputHeader, dataEntryNode);

		try {
			uploadEducationPdfFile(caseSpecificRecordDetailPOJO.getInstructionCheckId(), EDUCATION,
					caseSpecificInfoPOJO.getCaseNumber(), recordNode);
		} catch (JsonProcessingException e) {
			logger.error("Exception while uploading check education pdf : {}",
					caseSpecificRecordDetailPOJO.getInstructionCheckId());
			e.printStackTrace();
		}

		educationInputHeaders.add(educationInputHeader);
	}

	private void getEducationDetails3(JsonNode recordNode, EducationInputHeaderPOJO educationInputHeader,
			JsonNode dataEntryNode) {
		educationInputHeader.setStellarSent(recordNode.has(STELLAR_SENT) ? recordNode.get(STELLAR_SENT).asText() : "");
		educationInputHeader.setSubStatus(recordNode.has(SUB_STATUS) ? recordNode.get(SUB_STATUS).asText() : "");
		educationInputHeader.setDocumentAsPerMRL(
				recordNode.has(DOCUMENT_AS_PER_MRL) ? recordNode.get(DOCUMENT_AS_PER_MRL).asText() : "");

		String universityLocaltionState = recordNode.has("University Location (State)")
				? recordNode.get("University Location (State)").asText()
				: "";
		educationInputHeader.setLocation(getDataFromDataEntryKeyVal(dataEntryNode, educationDataEntryKeys,
				"universitylocationstate", universityLocaltionState));
		educationInputHeader.setVendor(recordNode.has(VENDOR) ? recordNode.get(VENDOR).asText() : "");
		educationInputHeader.setTLName(recordNode.has(TL_NAME) ? recordNode.get(TL_NAME).asText() : "");
		educationInputHeader.setPOC(recordNode.has("POC") ? recordNode.get("POC").asText() : "");
		educationInputHeader.setDVCompletionTime(
				recordNode.has(DV_COMPLETION_TIME) ? recordNode.get(DV_COMPLETION_TIME).asText() : "");
		educationInputHeader.setCheckInternalStatus(
				recordNode.has(CHECK_INTERNAL_STATUS2) ? recordNode.get(CHECK_INTERNAL_STATUS2).asText() : "");
		educationInputHeader.setQueueName(recordNode.has(QUEUE_NAME) ? recordNode.get(QUEUE_NAME).asText() : "");
		educationInputHeader.setBTDTWT(recordNode.has(BT_DT_WT) ? recordNode.get(BT_DT_WT).asText() : "");
		educationInputHeader.setLHVerificationCost(
				recordNode.has(LH_VERIFICATION_COST) ? recordNode.get(LH_VERIFICATION_COST).asText() : "");
		educationInputHeader.setTopClients(recordNode.has(TOP_CLIENTS) ? recordNode.get(TOP_CLIENTS).asText() : "");
		educationInputHeader.setApprovedRejected(
				recordNode.has(APPROVED_REJECTED) ? recordNode.get(APPROVED_REJECTED).asText() : "");
		educationInputHeader.setBatchSlot(recordNode.has(BATCH_SLOT) ? recordNode.get(BATCH_SLOT).asText() : "");
	}

	private void getEducationDetails2(JsonNode recordNode, EducationInputHeaderPOJO educationInputHeader,
			JsonNode dataEntryNode) {

		String candidateName = recordNode.has("Name of the candidate while attending the below qualification")
				? recordNode.get("Name of the candidate while attending the below qualification").asText()
				: "";
		educationInputHeader.setCandidateName(getDataFromDataEntryKeyVal(dataEntryNode, candidateDataEntryKeys,
				"nameofthecandidatewhileattendingthebelowqualification", candidateName));

		String qualification = recordNode.has("Qualification/course name")
				? recordNode.get("Qualification/course name").asText()
				: "";
		qualification = getDataFromDataEntryKeyVal(dataEntryNode, educationDataEntryKeys, "qualificationcoursename",
				qualification);

		String specialization = recordNode.has("Specialization") ? recordNode.get("Specialization").asText() : "";
		specialization = getDataFromDataEntryKeyVal(dataEntryNode, educationDataEntryKeys, "specialization",
				specialization);

		if (StringUtils.isNotEmpty(specialization)) {
			qualification += " (" + specialization + ")";
		}

		educationInputHeader.setQualification(qualification);

		educationInputHeader.setMajor(specialization);
		String uniqueNumberType = recordNode.has(UNIQUE_NUMBER_TYPE) ? recordNode.get(UNIQUE_NUMBER_TYPE).asText() : "";
		String uniqueNumber = recordNode.has(UNIQUE_NUMBER) ? recordNode.get(UNIQUE_NUMBER).asText() : "";
		String gradeClass = recordNode.has("Grade/Class") ? recordNode.get("Grade/Class").asText() : "";
		educationInputHeader.setNumbertype1(getDataFromDataEntryKeyVal(dataEntryNode, educationDataEntryKeys,
				"uniquenumbertype", uniqueNumberType));
		educationInputHeader.setNumbertype2("");
		educationInputHeader.setUniqueno1(
				getDataFromDataEntryKeyVal(dataEntryNode, educationDataEntryKeys, "uniquenumber", uniqueNumber));
		educationInputHeader.setUniqueno2("");
		educationInputHeader.setLDD(recordNode.has("LDD") ? recordNode.get("LDD").asText() : "");
		educationInputHeader.setClassObtained(
				getDataFromDataEntryKeyVal(dataEntryNode, educationDataEntryKeys, "gradeclass", gradeClass));
		educationInputHeader.setCheckDuedate(
				recordNode.has(CHECK_DUE_DATE2) ? Utility.formatDateUtil2(recordNode.get(CHECK_DUE_DATE2).asText())
						: "");
		educationInputHeader
				.setInitiatiorsName(recordNode.has(INITIATOR_NAME) ? recordNode.get(INITIATOR_NAME).asText() : "");
		educationInputHeader
				.setSpecialNotesComments(recordNode.has(SPECIAL_NOTES) ? recordNode.get(SPECIAL_NOTES).asText() : "");
		educationInputHeader.setStatus(recordNode.has(STATUS) ? recordNode.get(STATUS).asText() : "");
		educationInputHeader.setRemarks(recordNode.has(REMARKS) ? recordNode.get(REMARKS).asText() : "");
		educationInputHeader.setWrittenmandateClient(
				recordNode.has(WRITTEN_MANDATE_CLIENT) ? recordNode.get(WRITTEN_MANDATE_CLIENT).asText() : "");
	}

	private String getDataFromDataEntryKeyVal(JsonNode dataEntryNode, List<String> keyList, String keyToSearch,
			String keyVal) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		if (StringUtils.isEmpty(keyVal)) {
			for (String key : keyList) {
				if (dataEntryNode.has(key)) {
					JsonNode keyNode;
					if (dataEntryNode.get(key).isArray() && !dataEntryNode.get(key).isEmpty()) {
						keyNode = dataEntryNode.get(key).get(0);
					} else {
						keyNode = dataEntryNode.get(key);
					}
					keyVal = keyNode.get(keyToSearch) != null ? keyNode.get(keyToSearch).asText() : "";
					if (StringUtils.isNotEmpty(keyVal)) {
						return keyVal;
					}
				}
			}
		}
		return keyVal;
	}

	private void getEducationDetails1(CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, JsonNode otherDetails, String documentsName,
			EducationInputHeaderPOJO educationInputHeader) {
		educationInputHeader.setComponent(EDUCATION);
		educationInputHeader.setCheckID(caseSpecificRecordDetailPOJO.getInstructionCheckId());
		educationInputHeader.setClientName(caseSpecificInfoPOJO.getClientName());
		educationInputHeader.setCaseNumber(caseSpecificInfoPOJO.getCaseRefNumber());
		educationInputHeader.setDocumentsSent(documentsName);

		String endDate = otherDetails.has("End Time") ? otherDetails.get("End Time").asText() : "-";
		if (StringUtils.isNotEmpty(endDate) && !StringUtils.equalsIgnoreCase(endDate, "-")) {
			endDate = Utility.formatDateMilliSec(endDate);
		}
		String noOfChecks = otherDetails.has("No. of Checks") ? otherDetails.get("No. of Checks").asText() : "-";
		educationInputHeader.setCheckSubdate(endDate);
		educationInputHeader.setNoOfChecks(noOfChecks);

		SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy");
		String intiationDate = formatter1.format(new Date());
		educationInputHeader.setDateOfInitiation(intiationDate);
	}

	private void getEducationCollegeDetails(JsonNode recordNode, EducationInputHeaderPOJO educationInputHeader,
			JsonNode dataEntryNode) {
		String collegeName = recordNode.has(COLLEGE_NAME) ? recordNode.get(COLLEGE_NAME).asText() : "";
		String universityName = recordNode.has(UNIVERSITY_NAME) ? recordNode.get(UNIVERSITY_NAME).asText() : "";
		String collegeCity = recordNode.has("College City") ? recordNode.get("College City").asText() : "";
		String universityCity = recordNode.has("University City") ? recordNode.get("University City").asText() : "";
		String passingYear = recordNode.has(YEAR_OF_PASSING) ? recordNode.get(YEAR_OF_PASSING).asText() : "";

		collegeName = getDataFromDataEntryKeyVal(dataEntryNode, educationDataEntryKeys, "collegename", collegeName);
		universityName = getDataFromDataEntryKeyVal(dataEntryNode, educationDataEntryKeys, "universityname",
				universityName);
		collegeCity = getDataFromDataEntryKeyVal(dataEntryNode, educationDataEntryKeys, "collegecity", collegeCity);
		universityCity = getDataFromDataEntryKeyVal(dataEntryNode, educationDataEntryKeys, "universitycity",
				universityCity);
		passingYear = getDataFromDataEntryKeyVal(dataEntryNode, educationDataEntryKeys, "yearofpassing", passingYear);

		educationInputHeader.setYearofPassing(passingYear);
		educationInputHeader.setYearofGraduation(
				recordNode.has("Graduated date") ? Utility.formatDateUtil2(recordNode.get("Graduated date").asText())
						: "");
		educationInputHeader.setUniversityName(String.join(", ", Arrays.asList(universityName, universityCity)));
		educationInputHeader.setCollegeName(String.join(", ", Arrays.asList(collegeName, collegeCity)));
	}

	private void setEducationDefault1(EducationInputHeaderPOJO educationInputHeader) {
		if (StringUtils.isEmpty(educationInputHeader.getInitiatiorsName())) {
			educationInputHeader.setInitiatiorsName("NA");
		}
		if (StringUtils.isEmpty(educationInputHeader.getStellarSent())) {
			educationInputHeader.setStellarSent("No");
		}
		if (StringUtils.isEmpty(educationInputHeader.getStatus())) {
			educationInputHeader.setStatus("WIP");
		}
		if (StringUtils.isEmpty(educationInputHeader.getSubStatus())) {
			educationInputHeader.setSubStatus(VENDOR);
		}
		if (StringUtils.isEmpty(educationInputHeader.getDocumentAsPerMRL())) {
			educationInputHeader.setDocumentAsPerMRL("Yes");
		}
		if (StringUtils.isEmpty(educationInputHeader.getVendor())) {
			educationInputHeader.setVendor("NA");
		}
		if (StringUtils.isEmpty(educationInputHeader.getTLName())) {
			educationInputHeader.setTLName("NA");
		}
		if (StringUtils.isEmpty(educationInputHeader.getPOC())) {
			educationInputHeader.setPOC("NA");
		}
		if (StringUtils.isEmpty(educationInputHeader.getDVCompletionTime())) {
			educationInputHeader.setDVCompletionTime("NA");
		}
		if (StringUtils.isEmpty(educationInputHeader.getLDD())) {
			educationInputHeader.setDVCompletionTime("NA");
		}
		setEducationDefault2(educationInputHeader);
	}

	private void setEducationDefault2(EducationInputHeaderPOJO educationInputHeader) {
		if (StringUtils.isEmpty(educationInputHeader.getCheckInternalStatus())) {
			educationInputHeader.setCheckInternalStatus("Vendor Input File Sent. Awaiting Response");
		}
		if (StringUtils.isEmpty(educationInputHeader.getQueueName())) {
			educationInputHeader.setQueueName("NA");
		}
		if (StringUtils.isEmpty(educationInputHeader.getBTDTWT())) {
			educationInputHeader.setBTDTWT("NA");
		}
		if (StringUtils.isEmpty(educationInputHeader.getLHVerificationCost())) {
			educationInputHeader.setLHVerificationCost("NA");
		}
		if (StringUtils.isEmpty(educationInputHeader.getTopClients())) {
			educationInputHeader.setTopClients("NA");
		}
		if (StringUtils.isEmpty(educationInputHeader.getApprovedRejected())) {
			educationInputHeader.setApprovedRejected("NA");
		}
		if (StringUtils.isEmpty(educationInputHeader.getRemarks())) {
			educationInputHeader.setRemarks("NA");
		}
		if (StringUtils.isEmpty(educationInputHeader.getBatchSlot())) {
			educationInputHeader.setBatchSlot("NA");
		}
	}

	@Override
	public List<String> getMrlDocumentNames(ObjectMapper mapper, String componentName, String akaName, String checkId,
			String caseNumber) {

		if (StringUtils.isNotEmpty(akaName) && StringUtils.isNotEmpty(componentName)
				&& StringUtils.isNotEmpty(caseNumber) && StringUtils.isNotEmpty(checkId)) {

			List<String> checkIdList = new ArrayList<>();
			checkIdList.add(checkId);

			ObjectNode mrlRuleDescriptionDocNode = mapper.createObjectNode();
			mrlRuleDescriptionDocNode.put("componentName", componentName);
			mrlRuleDescriptionDocNode.put("akaName", akaName);
			mrlRuleDescriptionDocNode.put("caseNumber", caseNumber);
			mrlRuleDescriptionDocNode.put("checkIdList", checkIdList.toString());

			try {
				String mrlRuleDescriptionDocStr = mapper.writeValueAsString(mrlRuleDescriptionDocNode);

				String filePathMapStr = apiService.sendDataToPost(associateFilePathUrl, mrlRuleDescriptionDocStr);

				logger.info("associate file response : {}", filePathMapStr);
				filePathMapStr = filePathMapStr == null ? "[]" : filePathMapStr;
				List<JsonNode> responseArrayNode = mapper.readValue(filePathMapStr,
						new TypeReference<List<JsonNode>>() {
						});

				List<String> docNames = responseArrayNode.stream()
						.map(data -> data.has("documentName") ? data.get("documentName").asText() : "")
						.collect(Collectors.toList());
				return new ArrayList<>(new HashSet<>(docNames));

			} catch (JsonProcessingException e) {
				logger.error("Exception while mapping mrl document response : {}", e.getMessage());
				e.printStackTrace();
			}
		}

		return new ArrayList<>();
	}

	private void uploadFileToSftp(String componentName, String fileName, String fileLocation) {
		logger.info("Uploading {} excel file", componentName);
		File myObj = new File(fileLocation);
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		String strDate = formatter.format(date);
		String subfolder = strDate + FORWARD_SLASH;
		vendorSftpConnectionService.uploadFileTransferToSFTPServer(myObj, fileName, componentName, subfolder);
		try {
			Files.delete(myObj.toPath());
		} catch (Exception e) {
			logger.error("Exception while deleting {} excel file : {}", componentName, e.getMessage());
		}
	}

	private void uploadEducationPdfFile(String checkId, String componentName, String caseNumber, JsonNode recordNode)
			throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		String strDate = formatter.format(date);
		String subFolder = strDate + FORWARD_SLASH;

		String universityAkaName = recordNode.has(UNIVERSITY_AKA_NAME) ? recordNode.get(UNIVERSITY_AKA_NAME).asText()
				: "";

		if (StringUtils.isNotEmpty(checkId) && StringUtils.isNotEmpty(caseNumber)
				&& StringUtils.isNotEmpty(universityAkaName)) {
			List<String> checkIdList = new ArrayList<>();
			checkIdList.add(checkId);

			ObjectNode requestNode = mapper.createObjectNode();
			requestNode.put("caseNumber", caseNumber);
			requestNode.put("checkIdList", checkIdList.toString());
			requestNode.put("componentName", componentName);
			requestNode.put("akaName", universityAkaName);

			String requestNodeStr = mapper.writeValueAsString(requestNode);

			String filePathMapStr = apiService.sendDataToPost(associateFilePathUrl, requestNodeStr);

			logger.info("associate file response : {}", filePathMapStr);
			filePathMapStr = filePathMapStr == null ? "[]" : filePathMapStr;
			ArrayNode responseArrayNode = mapper.readValue(filePathMapStr, ArrayNode.class);

			uploadEducationPdfChild(componentName, subFolder, responseArrayNode);
		}
	}

	private void uploadEducationPdfChild(String componentName, String subFolder, ArrayNode responseArrayNode) {
		List<File> fileList = new ArrayList<>();
		String docCheckId = "";

		for (JsonNode responseNode : responseArrayNode) {

			docCheckId = responseNode.has(CHECK_ID) ? responseNode.get(CHECK_ID).asText() : "";
			String filePath = responseNode.has("filePath") ? responseNode.get("filePath").asText() : "";

			if (StringUtils.isNotEmpty(docCheckId) && StringUtils.isNotEmpty(filePath)) {
				File localFileName = new File(educationFileLocation + filePath);

				try {
					URL fileRemoteUrl = new URL(docUrl + filePath);
					logger.info("Filename : {}, \n {} ", localFileName, fileRemoteUrl);

					FileUtils.copyURLToFile(fileRemoteUrl, localFileName);
					logger.info("{}.pdf", docCheckId);
					fileList.add(localFileName);

				} catch (Exception e) {
					logger.error("Exception while downloaing education files : {}", e.getMessage());
					e.printStackTrace();
				}
			}
		}
		if (CollectionUtils.isNotEmpty(fileList)) {
			// Merge FIle Logic
			try {
				uploadPdftoSftp(componentName, subFolder, fileList, docCheckId);
			} catch (IOException e) {
				logger.error("Exception while merging education pdf to sftp : {}", e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private void uploadPdftoSftp(String componentName, String subFolder, List<File> fileList, String docCheckId)
			throws IOException {

		File uplodPDF = mergeFile(fileList, docCheckId);
		vendorSftpConnectionService.uploadFileTransferToSFTPServer(uplodPDF, docCheckId + ".pdf", componentName,
				subFolder);
		try {
			Files.delete(uplodPDF.toPath());
		} catch (Exception e) {
			logger.error("Exception while deleting file : {}", e.getMessage());
		}
	}

	// PDf Merge Logic
	private File mergeFile(List<File> fileList, String desiredFileName) throws IOException {
		List<PDDocument> pdDocuments = new ArrayList<>();

		// Instantiating PDFMergerUtility class
		PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
		// Setting the destination file
		pdfMergerUtility.setDestinationFileName(educationPdfFileLocation + "/" + desiredFileName + ".pdf");

		// Loading an existing PDF document
		for (File file : fileList) {
			pdDocuments.add(PDDocument.load(file));
			pdfMergerUtility.addSource(file);
		}
		// Merging the two documents
		pdfMergerUtility.mergeDocuments(null);
		for (PDDocument pdDocument : pdDocuments) {
			pdDocument.close();
		}

		for (File file : fileList) {
			try {
				Files.delete(file.toPath());
			} catch (Exception e) {
				logger.error("Exception while deleting file : {}", e.getMessage());
			}
		}
		return new File(educationPdfFileLocation, desiredFileName + ".pdf");
	}

	private AddressInputHeaderPOJO generateAddressInputHeader() {
		AddressInputHeaderPOJO addressInputHeader = new AddressInputHeaderPOJO();
		addressInputHeader.setComponent(COMPONENT);
		addressInputHeader.setCheckID(CHECKID);
		addressInputHeader.setCaseReferenceNumber(CASE_REF_NUMBER);
		addressInputHeader.setClientname(CLIENTNAME);
		addressInputHeader.setCandidateName(CANDIDATE_NAME);
		addressInputHeader.setAddress(ADDRESS);
		addressInputHeader.setFathersName(FATHERS_NAME);
		addressInputHeader.setDateFrom(DATE_FROM);
		addressInputHeader.setDateTo(DATETO);
		addressInputHeader.setCandidatesContactNos(CANDIDATE_CONTACT_NO);
		addressInputHeader.setOrgNameInBVF(ORG_NAME_BVF);
		addressInputHeader.setCheckTAT(CHECKTAT);
		addressInputHeader.setCheckInternalStatus(CHECK_INTERNAL_STATUS);
		addressInputHeader.setOpenFor(OPEN_FOR);
		addressInputHeader.setWorkableWIPChecks(WORKABLE_WIP_CHECKS);
		addressInputHeader.setClientCode(CLIENT_CODE);
		addressInputHeader.setDummy(DUMMY);
		return addressInputHeader;
	}

	private EducationInputHeaderPOJO generateEducationInputHeader() {
		EducationInputHeaderPOJO educationInputHeader = new EducationInputHeaderPOJO();
		educationInputHeader.setDateOfInitiation("Date of initiation");
		educationInputHeader.setComponent(COMPONENT);
		educationInputHeader.setCheckID(CHECK_ID2);
		educationInputHeader.setClientName(CLIENT_NAME);
		educationInputHeader.setCaseNumber("Case Number");// Client Code
		educationInputHeader.setCandidateName(CANDIDATE_NAME2);
		educationInputHeader.setCollegeName(COLLEGE_NAME);
		educationInputHeader.setUniversityName(UNIVERSITY_NAME);
		educationInputHeader.setQualification("Qualification");
		educationInputHeader.setMajor("Major");
		educationInputHeader.setNumbertype1(NUMBER_TYPE1);
		educationInputHeader.setUniqueno1("Unique1");
		educationInputHeader.setNumbertype2(NUMBER_TYPE2);
		educationInputHeader.setUniqueno2("Unique2");
		// educationInputHeader.setYearofPassing("Year of PassingE.g.Month-Year
		// (July-2005)");
		educationInputHeader.setYearofPassing("Year of Passing\nE.g.Month-Year (July-2005)");
		educationInputHeader.setYearofGraduation("Year of Graduation");
		educationInputHeader.setClassObtained("Class Obtained");
		educationInputHeader.setDocumentsSent(DOCUMENTS_SENT);
		educationInputHeader.setCheckSubdate(CHECK_SUB_DATE);
		educationInputHeader.setCheckDuedate(CHECK_DUE_DATE2);
		educationInputHeader.setInitiatiorsName("Initiatiors Name");
		educationInputHeader.setSpecialNotesComments(SPECIAL_NOTES);
		educationInputHeader.setWrittenmandateClient(WRITTEN_MANDATE_CLIENT);
		educationInputHeader.setStellarSent(STELLAR_SENT);
		educationInputHeader.setStatus(STATUS_C);
		educationInputHeader.setSubStatus(SUB_STATUS);
		educationInputHeader.setDocumentAsPerMRL(DOCUMENT_AS_PER_MRL);
		educationInputHeader.setLocation("Location");
		educationInputHeader.setVendor(VENDOR);
		educationInputHeader.setTLName(TL_NAME);
		educationInputHeader.setPOC("POC");
		educationInputHeader.setDVCompletionTime(DV_COMPLETION_TIME);
		educationInputHeader.setLDD("LDD");
		educationInputHeader.setCheckInternalStatus(CHECK_INTERNAL_STATUS2);
		educationInputHeader.setQueueName(QUEUE_NAME);
		educationInputHeader.setNoOfChecks(NO_OF_CHECKS);
		educationInputHeader.setBTDTWT(BT_DT_WT);
		educationInputHeader.setLHVerificationCost(LH_VERIFICATION_COST);
		educationInputHeader.setTopClients(TOP_CLIENTS);
		educationInputHeader.setApprovedRejected(APPROVED_REJECTED);
		educationInputHeader.setRemarks(REMARKS);
		educationInputHeader.setBatchSlot(BATCH_SLOT);
		educationInputHeader.setQRCode(QR_CODE);
		return educationInputHeader;
	}

	private void createVendorInputPostVerificationEvent(String routerResult,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, CaseSpecificInfoPOJO caseSpecificInfoPOJO) {
		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();
		if (StringUtils.equalsIgnoreCase(routerResult, "File Created")) {
			verificationEventStatus.setStatus("F2");
		} else {
			verificationEventStatus.setStatus("Day 0");
		}

		verificationEventStatus.setEvent("Input file generated");
		verificationEventStatus.setCheckId(caseSpecificRecordDetailPOJO.getInstructionCheckId());
		verificationEventStatus.setStage("Vendor-Input");
		verificationEventStatus.setEventType("auto");
		verificationEventStatus.setCaseNo(caseSpecificInfoPOJO.getCaseNumber());
		verificationEventStatus.setUserId(null);
		verificationEventStatus.setRequestId(caseSpecificRecordDetailPOJO.getCaseSpecificDetailId());
		verificationEventStatusRepository.save(verificationEventStatus);
	}

	private String sendVerifyDataToL3(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, List<String> holidayList)
			throws JsonProcessingException {
		String checkId = caseSpecificRecordDetailPOJO.getInstructionCheckId();
		TaskSpecsPOJO taskSpecs = new TaskSpecsPOJO();

		taskSpecs.setCaseReference(
				getCaseReference(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO, checkId));
		taskSpecs.setCheckVerification(getCheckVerification(caseSpecificRecordDetailPOJO, holidayList));

		taskSpecs.setQuestionaire(new ArrayList<>());

		FileUploadPOJO fileUpload = new FileUploadPOJO();
		fileUpload.setVerificationReplyDocument(new ArrayList<>());
		taskSpecs.setFileUpload(fileUpload);

		String taskSpecsStr = mapper.writeValueAsString(taskSpecs);
		logger.info("l3 verification json : {} ", taskSpecsStr);

		String l3VerifyResponse = apiService.sendDataToL3Post(verificationStatusL3Url, taskSpecsStr, null);
		// TODO save l3 api event

		if (l3VerifyResponse == null) {
			return L3_ERROR_RESPONSE;
		}
		return l3VerifyResponse;
	}

	private L3CaseReferencePOJO getCaseReference(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, String checkId) throws JsonProcessingException {
		L3CaseReferencePOJO caseReference = mapper.readValue(caseSpecificInfoPOJO.getCaseReference(),
				L3CaseReferencePOJO.class);

		caseReference.setCheckId(checkId);

		caseReference.setNgStatus("F2");
		caseReference.setNgStatusDescription("Followup 2");
		caseReference.setSbuName(caseSpecificInfoPOJO.getSbuName());
		caseReference.setProductName(caseSpecificRecordDetailPOJO.getProduct());
		caseReference.setPackageName(caseSpecificInfoPOJO.getPackageName());
		caseReference.setComponentName(caseSpecificRecordDetailPOJO.getComponentName());

		return caseReference;
	}

	private CheckVerificationPOJO getCheckVerification(CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO,
			List<String> holidayList) {
		CheckVerificationPOJO checkVerification = new CheckVerificationPOJO();

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");

		checkVerification.setCountry("India");
		if (StringUtils.equalsIgnoreCase(caseSpecificRecordDetailPOJO.getComponentName(), ADDRESS)) {
			checkVerification.setInternalNotes(
					"In order to verify the address details through physical confirmation, the point of contact has been provided with the detailed address.");
			// 04 Working Days  (Excluding weekend and holidays)
			checkVerification.setFollowUpDateAndTimes(
					formatter.format(Utility.addDaysSkippingWeekends(new Date(), 4, holidayList)));
			// 05 Working Days  (Excluding weekend and holidays)
			checkVerification.setExpectedClosureDate(
					formatter.format(Utility.addDaysSkippingWeekends(new Date(), 5, holidayList)));
		} else if (StringUtils.equalsIgnoreCase(caseSpecificRecordDetailPOJO.getComponentName(), EDUCATION)) {
			checkVerification
					.setInternalNotes("We have sent an email to the point of contact at educational institutions.");
			// 02 Working Days (Excluding weekend and holidays)
			checkVerification.setFollowUpDateAndTimes(
					formatter.format(Utility.addDaysSkippingWeekends(new Date(), 2, holidayList)));
			// 07 Working Days (Excluding weekend and holidays)
			checkVerification.setExpectedClosureDate(
					formatter.format(Utility.addDaysSkippingWeekends(new Date(), 7, holidayList)));
		}
		checkVerification.setExecutiveSummaryComments("");
		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		checkVerification.setEmailId("");
		checkVerification.setVerifierName("");
		checkVerification.setGeneralRemarks("");
		checkVerification.setVerifiedDate("");
		checkVerification.setAction("verifyChecks");
		checkVerification.setSubAction("verifyChecks");
		checkVerification.setActionCode("");
		checkVerification.setComponentName(caseSpecificRecordDetailPOJO.getComponentName());
		checkVerification.setProductName(caseSpecificRecordDetailPOJO.getProduct());
		checkVerification.setEndStatusOfTheVerification("Pending for Reply");
		checkVerification.setModeOfVerification("Email");
		checkVerification.setAttempts("Internal");
		checkVerification.setIsThisAVerificationAttempt("Yes");
		checkVerification.setDepartmentName("");
		checkVerification.setKeyWords("");
		checkVerification.setCost("");
		checkVerification.setDateVerificationCompleted("");
		checkVerification.setDisposition("");
		checkVerification.setVerifierDesignation("Official");
		checkVerification.setVerifierNumber("");
		checkVerification.setMiRemarks("");
		return checkVerification;
	}

	private void createVendorAttempt(CaseSpecificRecordDetailPOJO caseSpecificRecordDetail, String l3Response,
			List<String> holidayList) {
		AttemptHistory attemptHistory = new AttemptHistory();

		String l3Status = SUCCESS;
		if (StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getComponentName(), EDUCATION)) {
			attemptHistory.setAttemptDescription(
					"We have sent an email to the point of contact at educational institutions.");
			attemptHistory.setAttemptStatusid((long) 64);// Input file generated
			attemptHistory.setName(VENDOR);
			attemptHistory.setJobTitle("NA");
			attemptHistory.setCheckid(caseSpecificRecordDetail.getInstructionCheckId());
			attemptHistory.setFollowupId((long) 2);
			attemptHistory.setRequestid(caseSpecificRecordDetail.getCaseSpecificDetailId());
			attemptHistory.setContactDate(new Date().toString());
			attemptHistory.setL3Response(l3Response);
			attemptHistory.setL3Status(l3Status);
			int followUpDays = 2;
			attemptHistory.setFollowupDate(
					(Utility.addDaysSkippingWeekends(new Date(), followUpDays, holidayList)).toString());
			int expectedClosureDate = 7;
			attemptHistory.setClosureExpectedDate(
					(Utility.addDaysSkippingWeekends(new Date(), expectedClosureDate, holidayList)).toString());
			Date contactDate = new Date();
			attemptHistory.setContactDate(contactDate.toString());
			AttemptHistory newAttemptHistory = attemptHistoryRepository.save(attemptHistory);
			createAttemptStatusData(newAttemptHistory);
		} else if (StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getComponentName(), ADDRESS)) {
			attemptHistory.setAttemptDescription(
					"In order to verify the address details through physical confirmation, the point of contact has been provided with the detailed address.");
			// Email-Sent Input file generated
			attemptHistory.setAttemptStatusid((long) 64);
			attemptHistory.setName(VENDOR);
			attemptHistory.setJobTitle("NA");
			attemptHistory.setCheckid(caseSpecificRecordDetail.getInstructionCheckId());
			attemptHistory.setFollowupId((long) 2);
			attemptHistory.setRequestid(caseSpecificRecordDetail.getCaseSpecificDetailId());
			attemptHistory.setContactDate(new Date().toString());
			attemptHistory.setL3Response(l3Response);
			attemptHistory.setL3Status(l3Status);
			int followUpDays = 4;
			attemptHistory.setFollowupDate(
					(Utility.addDaysSkippingWeekends(new Date(), followUpDays, holidayList)).toString());
			int expectedClosureDate = 5;
			attemptHistory.setClosureExpectedDate(
					(Utility.addDaysSkippingWeekends(new Date(), expectedClosureDate, holidayList)).toString());
			Date contactDate = new Date();
			attemptHistory.setContactDate(contactDate.toString());
			AttemptHistory newAttemptHistory = attemptHistoryRepository.save(attemptHistory);
			createAttemptStatusData(newAttemptHistory);
		}
	}

	private void createAttemptStatusData(AttemptHistory attemptHistory) {
		Long attemptId = attemptHistory.getAttemptid() != null ? attemptHistory.getAttemptid() : 0;
		if (attemptId != 0) {
			AttemptStatusData attemptStatusData = new AttemptStatusData();
			attemptStatusData.setAttemptId(attemptId);
			attemptStatusData.setDepositionId((long) 13);
			attemptStatusData.setEndstatusId((long) 2);
			attemptStatusData.setModeId((long) 14);
			attemptStatusDataRepository.save(attemptStatusData);
		}
	}

}
