package com.gic.fadv.verification.spoc.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.attempts.model.CaseSpecificInfo;
import com.gic.fadv.verification.attempts.model.CaseSpecificRecordDetail;
import com.gic.fadv.verification.attempts.repository.CaseSpecificInfoRepository;
import com.gic.fadv.verification.attempts.repository.CaseSpecificRecordDetailRepository;
import com.gic.fadv.verification.online.service.OnlineApiService;
import com.gic.fadv.verification.spoc.pojo.CutomSpocDetailsPOJO;
import com.gic.fadv.verification.spoc.pojo.SPOCEmailConfigPOJO;
import com.gic.fadv.verification.spoc.pojo.SPOCExcelTemplatePOJO;
import com.gic.fadv.verification.spoc.pojo.TemplateHeadersPOJO;
import com.gic.fadv.verification.spoc.repository.SPOCBulkRepository;
import com.gic.fadv.verification.spoc.utility.JExcelUtility;
import com.gic.fadv.verification.spoc.utility.ZipUtility;

import jxl.write.WriteException;

@Service
public class SPOCBulkServiceImpl implements SPOCBulkService {

	private static final Logger logger = LoggerFactory.getLogger(SPOCBulkServiceImpl.class);
	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;

	@Autowired
	private CaseSpecificInfoRepository caseSpecificInfoRepository;

	@Autowired
	private SPOCBulkRepository spocBulkRepository;

	@Autowired
	private OnlineApiService onlineApiService;

	@Value("${spocexceltemplate.rest.url}")
	private String spocExcelTemplateRestUrl;

	@Value("${associate.filepath.rest.url}")
	private String associateFilePathUrl;

	@Value("${local.file.zip.location}")
	private String localFileZiplocation;

	// Doc URL
	@Value("${doc.url}")
	private String docUrl;
	// Local File Download Location
	@Value("${local.file.download.location}")
	private String localFileLocation;

	@Value("${spocemail.rest.url}")
	private String spocEmailRestUrl;

	private static final String PERSONAL_DETAILS_BVF = "personaldetailsasperbvf";
	private static final String PLEASE_SPECIFY = "(Please specify)";

	@Override
	public String processSPOCBulk(List<String> checkIdList) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		logger.info("List of Checks:{}", checkIdList);
		List<CaseSpecificRecordDetail> caseSpecificRecordDetailPOJOList = caseSpecificRecordDetailRepository
				.getByCheckIdList(checkIdList);
		logger.info("Size of List:{}", caseSpecificRecordDetailPOJOList.size());
		String akaName = spocBulkRepository
				.getAkaNameByCheckId(caseSpecificRecordDetailPOJOList.get(0).getInstructionCheckId());
		List<List<String>> exceldataList = new ArrayList<>();
		List<TemplateHeadersPOJO> templateHeadersPOJOs = new ArrayList<>();
		String akaFileName = StringUtils.replace(akaName, " ", "_");
		String akaFileNameXls = akaFileName;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		akaFileName = akaFileName + timestamp.getTime();
		for (CaseSpecificRecordDetail caseSpecificRecordDetail : caseSpecificRecordDetailPOJOList) {
			logger.info("Value of Instruction Check" + caseSpecificRecordDetail.getInstructionCheckId());
			String componentName = caseSpecificRecordDetail.getComponentName();
			String spocExcelTemplateSearchString = "{\"contactCardName\":" + "\"" + akaName + "\""
					+ ",\"componentName\":" + "\"" + componentName + "\"" + "}";
			logger.info("Spoc template Search String : {}", spocExcelTemplateSearchString);

			String spocTemplateResponse = onlineApiService.sendDataToPost(spocExcelTemplateRestUrl,
					spocExcelTemplateSearchString);

			logger.info("SPOC Excel Template Response : {}", spocTemplateResponse);

			List<SPOCExcelTemplatePOJO> spocEmailTemplateList = mapper.readValue(spocTemplateResponse,
					new TypeReference<List<SPOCExcelTemplatePOJO>>() {
					});
			if (CollectionUtils.isNotEmpty(spocEmailTemplateList)) {
				logger.info("Send Information With Excel attachment : {}",
						spocEmailTemplateList.get(0).getTemplateHeaders());

				// object -> Map
				TemplateHeadersPOJO[] templateHeaders = mapper
						.convertValue(spocEmailTemplateList.get(0).getTemplateHeaders(), TemplateHeadersPOJO[].class);

				templateHeadersPOJOs = new ArrayList<>(Arrays.asList(templateHeaders));

				logger.info("{}", templateHeadersPOJOs.size());
				logger.info("{} {}", templateHeaders[0], templateHeadersPOJOs.get(0));
				JsonNode recordNode = mapper.readTree(caseSpecificRecordDetail.getComponentRecordField());
				exceldataList.add(generateExcelDataForCheckId(mapper, caseSpecificRecordDetail, templateHeadersPOJOs,
						recordNode, akaName, akaFileName));
			}
		}
		try {
			JExcelUtility.writeJExcelList(templateHeadersPOJOs, exceldataList, localFileLocation + akaFileName,
					akaFileNameXls);
		} catch (IOException | WriteException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}

		try {
			ZipUtility.zipDirectory(localFileLocation + akaFileName + "/", localFileZiplocation, akaFileName);
			File file = new File(localFileLocation + akaFileName);
			deleteDirectory(file);

		} catch (IOException e) {
			logger.error("Exception while making final zip file : {}", e.getMessage());
		}
		return localFileZiplocation + akaFileName + ".zip";
	}

	private List<String> generateExcelDataForCheckId(ObjectMapper mapper,
			CaseSpecificRecordDetail caseSpecificRecordDetail, List<TemplateHeadersPOJO> templateHeadersPOJOs,
			JsonNode recordNode, String akaName, String akaFileName) throws JsonProcessingException {

		CutomSpocDetailsPOJO cutomSpocDetailsPOJO = getFirstNameLastNameClientName(mapper, caseSpecificRecordDetail);
		String clientName = PLEASE_SPECIFY;
		String firstName = cutomSpocDetailsPOJO.getFirstName();
		String lastName = cutomSpocDetailsPOJO.getLastName();
		clientName = cutomSpocDetailsPOJO.getClientName();
		String checkId = caseSpecificRecordDetail.getInstructionCheckId();
		if(StringUtils.isEmpty(firstName) && StringUtils.isEmpty(lastName)) {
			firstName=PLEASE_SPECIFY;
		}
		
		logger.info("Added CheckId:{}", checkId);
		// Take Excel value to fill
		List<String> excelDataList = new ArrayList<>();
		if (recordNode != null) {
			for (TemplateHeadersPOJO templateHeadersPOJO : templateHeadersPOJOs) {
				String excelData = "";
				String convertedDate = "";
				if (StringUtils.equalsIgnoreCase("Client Name", templateHeadersPOJO.getDocumentName())) {
					excelData = clientName;
				} else if (StringUtils.equalsIgnoreCase("Name of the Candidate",
						templateHeadersPOJO.getDocumentName())) {
					excelData = firstName + " " + lastName;
				} else if (StringUtils.equalsIgnoreCase("CheckID", templateHeadersPOJO.getDocumentName())) {
					excelData = checkId;
				} else if (StringUtils.equalsIgnoreCase("DOB", templateHeadersPOJO.getDocumentName())
						|| StringUtils.equalsIgnoreCase("Date of Exit", templateHeadersPOJO.getDocumentName())
						|| StringUtils.equalsIgnoreCase("Date of Expiry", templateHeadersPOJO.getDocumentName())
						|| StringUtils.equalsIgnoreCase("Date of LOA", templateHeadersPOJO.getDocumentName())
						|| StringUtils.equalsIgnoreCase("Date of joining", templateHeadersPOJO.getDocumentName())
						|| StringUtils.equalsIgnoreCase("Dates of employment", templateHeadersPOJO.getDocumentName())) {
					excelData = recordNode.get(templateHeadersPOJO.getDocumentName()) != null
							&& StringUtils.isNotEmpty(recordNode.get(templateHeadersPOJO.getDocumentName()).asText())
									? recordNode.get(templateHeadersPOJO.getDocumentName()).asText()
									: PLEASE_SPECIFY;
					if (StringUtils.isNotEmpty(excelData)) {
						convertedDate = getDateFormat(excelData);
						if (convertedDate != null) {
							excelData = convertedDate;
						}
					}
				} else {
					excelData = recordNode.get(templateHeadersPOJO.getDocumentName()) != null
							&& StringUtils.isNotEmpty(recordNode.get(templateHeadersPOJO.getDocumentName()).asText())
									? recordNode.get(templateHeadersPOJO.getDocumentName()).asText()
									: PLEASE_SPECIFY;
				}
				excelDataList.add(excelData);
			}
		}
		generateMrlFiles(mapper, akaName, cutomSpocDetailsPOJO, caseSpecificRecordDetail, firstName, lastName,
				akaFileName);
		return excelDataList;
	}

	private String generateMrlFiles(ObjectMapper mapper, String akaName, CutomSpocDetailsPOJO cutomSpocDetailsPOJO,
			CaseSpecificRecordDetail caseSpecificRecordDetail, String firstName, String lastName, String akaFileName)
			throws JsonProcessingException {
		String attachmentName = getAttachmentName(caseSpecificRecordDetail, mapper, akaName, firstName, lastName);
		String caseNumber = cutomSpocDetailsPOJO.getCaseSpecificInfo() != null
				? cutomSpocDetailsPOJO.getCaseSpecificInfo().getCaseNumber()
				: "";

		List<File> fileList = getFileList(mapper, caseNumber, caseSpecificRecordDetail.getInstructionCheckId(),
				attachmentName, akaName, caseSpecificRecordDetail.getComponentName(), akaFileName);

		try {
			File file = mergeFile(fileList, attachmentName, akaFileName);
		} catch (IOException e) {
			logger.error("File Merge Error:{}", e.getMessage());
		}
		return localFileZiplocation + akaFileName + ".zip";
	}

	private List<File> getFileList(ObjectMapper mapper, String caseNumber, String checkId, String attachmentName,
			String akaName, String componentName, String akaFileName) throws JsonProcessingException {

		List<String> checkIdList = new ArrayList<>();
		checkIdList.add(checkId);
		ObjectNode requestNode = mapper.createObjectNode();
		requestNode.put("caseNumber", caseNumber);
		requestNode.put("checkIdList", checkIdList.toString());
		requestNode.put("componentName", componentName);
		requestNode.put("akaName", akaName);

		String requestNodeStr = mapper.writeValueAsString(requestNode);
		logger.info("Request Node Value : {}", requestNodeStr);

		String filePathMapStr = onlineApiService.sendDataToPost(associateFilePathUrl, requestNodeStr);

		logger.info("associate file response : {}", filePathMapStr);

		ArrayNode responseArrayNode = mapper.readValue(filePathMapStr, ArrayNode.class);

		List<File> attachmentList = new ArrayList<>();
		int index = 1;
		for (JsonNode responseNode : responseArrayNode) {
			String filePath = responseNode.has("filePath") ? responseNode.get("filePath").asText() : "";
			File filename = new File(localFileLocation + akaFileName + "/" + attachmentName + "-" + index + ".pdf");
			try {
				URL newURL = new URL(docUrl + filePath);
				logger.info("Filename : {} \nnewUrl : {}", filename, newURL);
				FileUtils.copyURLToFile(newURL, filename);
			} catch (Exception e) {
				e.printStackTrace();
			}
			index++;
			attachmentList.add(filename);
		}
		return attachmentList;
	}

	// PDf Merge Logic
	private File mergeFile(List<File> fileList, String desiredFileName, String akaFileName) throws IOException {
		List<PDDocument> pdDocuments = new ArrayList<>();

		// Instantiating PDFMergerUtility class
		PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
		// Setting the destination file
		// pdfMergerUtility.setDestinationFileName(localFileLocation + desiredFileName +
		// ".pdf");
		pdfMergerUtility.setDestinationFileName(localFileLocation + akaFileName + "/" + desiredFileName + ".pdf");
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
		return new File(localFileLocation + akaFileName + "/" + desiredFileName + ".pdf");
	}

	private String getAttachmentName(CaseSpecificRecordDetail caseSpecificRecordDetail, ObjectMapper mapper,
			String akaName, String firstName, String lastName) {
		String attachmentName = "";
		String spocEmailSearchString = "{\"contactCardName\":" + "\"" + akaName + "\"" + "}";
		logger.info("spoc Email Search String: {}", spocEmailSearchString);

		List<SPOCEmailConfigPOJO> spocEmailConfigList = new ArrayList<>();
		try {
			String spocEmailResponse = onlineApiService.sendDataToPost(spocEmailRestUrl, spocEmailSearchString);
			logger.info("SPOC Email Response : {}", spocEmailResponse);
			spocEmailConfigList = mapper.readValue(spocEmailResponse, new TypeReference<List<SPOCEmailConfigPOJO>>() {
			});

		} catch (Exception e) {
			logger.info("Excpetion in calling sendDataToEmailConfig : {}", e.getMessage());
			e.printStackTrace();
		}
		if (CollectionUtils.isNotEmpty(spocEmailConfigList)) {
			SPOCEmailConfigPOJO spocEmailConfigPOJO = spocEmailConfigList.get(0);
			attachmentName = spocEmailConfigPOJO.getMRLDocumentAttachmentFileName();
			attachmentName = attachmentName.replace("<<First Name>>", firstName);
			attachmentName = attachmentName.replace("<<Last Name>>", lastName);
			attachmentName = attachmentName.replace("<<Check ID>>", caseSpecificRecordDetail.getInstructionCheckId());
		}
		return attachmentName;
	}

	private CutomSpocDetailsPOJO getFirstNameLastNameClientName(ObjectMapper mapper,
			CaseSpecificRecordDetail caseSpecificRecordDetail) throws JsonProcessingException {
		CutomSpocDetailsPOJO cutomSpocDetailsPOJO = new CutomSpocDetailsPOJO();
		String firstName = "";
		String lastName = "";
		String clientName = "";
		CaseSpecificInfo caseSpecificInfo = new CaseSpecificInfo();

		Optional<CaseSpecificInfo> caseSpecificInfoOpt = caseSpecificInfoRepository
				.findById(caseSpecificRecordDetail.getCaseSpecificId());
		if (caseSpecificInfoOpt.isPresent()) {
			caseSpecificInfo = caseSpecificInfoOpt.get();

			JsonNode caseDetails = mapper.readTree(caseSpecificInfo.getCaseDetails());

			String dataEntryStr = caseSpecificInfo.getDataEntryInfo() != null
					|| StringUtils.isNotEmpty(caseSpecificInfo.getDataEntryInfo()) ? caseSpecificInfo.getDataEntryInfo()
							: "{}";

			JsonNode dataEntryNode = mapper.readTree(dataEntryStr);

			JsonNode personalDetailsNode = dataEntryNode.has(PERSONAL_DETAILS_BVF)
					? dataEntryNode.get(PERSONAL_DETAILS_BVF)
					: mapper.createObjectNode();
			firstName = personalDetailsNode.has("firstname")
					&& StringUtils.isNotEmpty(personalDetailsNode.get("firstname").asText())
							? personalDetailsNode.get("firstname").asText()
							: "";
			lastName = personalDetailsNode.has("lastname")
					&& StringUtils.isNotEmpty(personalDetailsNode.get("lastname").asText())
							? personalDetailsNode.get("lastname").asText()
							: "";
			if(StringUtils.isEmpty(firstName) && StringUtils.isEmpty(lastName)) {
				firstName="(Please Specify)";
			}
			if (caseDetails != null) {
				clientName = caseDetails.has("Client Name(Full Business name)")
						&& StringUtils.isNotEmpty(caseDetails.get("Client Name(Full Business name)").asText())
								? caseDetails.get("Client Name(Full Business name)").asText()
								: "";
			}

			logger.info("firstName : {}, lastName : {}, clientName : {}", firstName, lastName, clientName);
		}
		cutomSpocDetailsPOJO.setFirstName(firstName);
		cutomSpocDetailsPOJO.setLastName(lastName);
		cutomSpocDetailsPOJO.setClientName(clientName);
		cutomSpocDetailsPOJO.setCaseSpecificInfo(caseSpecificInfo);

		return cutomSpocDetailsPOJO;
	}

	public static void deleteDirectory(File folder) {
		File[] fileList = folder.listFiles();
		if (fileList != null) {
			for (File file : fileList) {
				try {
					Files.delete(file.toPath());
				} catch (Exception e) {
					logger.error("Exception while deleting file : {}", e.getMessage());
				}
			}
		}
		try {
			Files.delete(folder.toPath());
		} catch (Exception e) {
			logger.error("Exception while deleting file : {}", e.getMessage());
		}
	}

	private String getDateFormat(String dateAsString) {
		DateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd");
		String returnDate = "";
		try {
			Date date = sourceFormat.parse(dateAsString);
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
			returnDate = formatter.format(date);
			logger.info("Value of date : {}, New date : {}", date, returnDate);
		} catch (ParseException e) {
			logger.error("Exception occurred while parsing date : {}", e.getMessage());
			returnDate = dateAsString;
			// e.printStackTrace();
		}
		return returnDate;
	}
}
