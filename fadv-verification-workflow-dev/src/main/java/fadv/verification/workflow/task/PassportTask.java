package fadv.verification.workflow.task;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fadv.verification.workflow.model.CaseSpecificInfo;
import fadv.verification.workflow.model.CaseSpecificRecordDetail;
import fadv.verification.workflow.service.PassportService;

public class PassportTask implements Callable<String> {

	private static final String NATIONALITY = "Nationality";
	private final ObjectMapper mapper;
	private final List<CaseSpecificRecordDetail> caseSpecificRecordDetails;
	private final CaseSpecificInfo caseSpecificInfo;
	private final PassportService passportService;
	private static final Logger logger = LoggerFactory.getLogger(PassportTask.class);
	private final CountDownLatch countDownLatch;

	public PassportTask(ObjectMapper mapper, List<CaseSpecificRecordDetail> caseSpecificRecordDetails,
			CaseSpecificInfo caseSpecificInfo, PassportService passportService, CountDownLatch countDownLatch) {
		this.mapper = mapper;
		this.caseSpecificRecordDetails = caseSpecificRecordDetails;
		this.caseSpecificInfo = caseSpecificInfo;
		this.passportService = passportService;
		this.countDownLatch = countDownLatch;
	}

	@Override
	public String call() {
		logger.info("Passport Service started at : {}", new Date());
		try {
			for (CaseSpecificRecordDetail caseSpecificRecordDetail : caseSpecificRecordDetails) {
				String recordString = caseSpecificRecordDetail.getComponentRecordField() != null
						? caseSpecificRecordDetail.getComponentRecordField()
						: "{}";
				try {
					JsonNode componentRecordFieldNode = mapper.readTree(recordString);

					String dob = componentRecordFieldNode.has("DOB") ? componentRecordFieldNode.get("DOB").asText()
							: "";
					String givenName = componentRecordFieldNode.has("Given name as per passport")
							? componentRecordFieldNode.get("Given name as per passport").asText()
							: "";
					String lastName = componentRecordFieldNode.has("Surname as per passport")
							? componentRecordFieldNode.get("Surname as per passport").asText()
							: "";
					String gender = componentRecordFieldNode.has("Gender")
							? componentRecordFieldNode.get("Gender").asText()
							: "";
					String issuingState = componentRecordFieldNode.has(NATIONALITY)
							? componentRecordFieldNode.get(NATIONALITY).asText()
							: "";
					String nationality = componentRecordFieldNode.has(NATIONALITY)
							? componentRecordFieldNode.get(NATIONALITY).asText()
							: "";
					String passportIdNumber = componentRecordFieldNode.has("Doc Identification No")
							? componentRecordFieldNode.get("Doc Identification No").asText()
							: "";
					String dateOfExpiry = componentRecordFieldNode.has("Date of Expiry")
							? componentRecordFieldNode.get("Date of Expiry").asText()
							: "";

					ObjectNode requestNode = mapper.createObjectNode();
					String caseReference = caseSpecificInfo.getCaseReference() != null
							? caseSpecificInfo.getCaseReference()
							: "{}";
					// As per Suggestion

					ObjectNode caseReferenceNode = (ObjectNode) mapper.readTree(caseReference);
					logger.info("casereference : {}", caseReferenceNode);
					caseReferenceNode.put("checkId", caseSpecificRecordDetail.getInstructionCheckId());
					logger.info("casereference checkId : {}", caseSpecificRecordDetail.getInstructionCheckId());
					requestNode.set("caseReference", caseReferenceNode);

					ObjectNode passportVerificationNode = mapper.createObjectNode();
					passportVerificationNode.put("sharedPath", "C:\\Shared Folder\\");
					passportVerificationNode.put("checkID", caseSpecificRecordDetail.getInstructionCheckId());
					passportVerificationNode.put("dateOfBirth", dob);
					passportVerificationNode.put("givenName", givenName);
					passportVerificationNode.put("lastName", lastName);
					passportVerificationNode.put("gender", gender);
					passportVerificationNode.put("issuingState", issuingState + " ");
					passportVerificationNode.put("nationality", nationality + " ");
					passportVerificationNode.put("passportIdNumber", passportIdNumber);
					passportVerificationNode.put("dateOfExpiry", dateOfExpiry);
					requestNode.set("passportverification", passportVerificationNode);
					String requestStr = mapper.writeValueAsString(requestNode);

					logger.info("passport request : {}", requestNode);
					processRouters(mapper, caseSpecificRecordDetail, caseSpecificInfo, requestStr, requestNode);
				} catch (JsonProcessingException e) {
					logger.error("Exception while converting request node to string : {}", e.getMessage());
				}
			}
		} catch (Exception e) {
			logger.error("Passport Task Exception:{}", e.getMessage());
		} finally {
			countDownLatch.countDown();
		}
		logger.info("Passport Service ended at : {}", new Date());
		return "Passport Service Completed";
	}

	private void processRouters(ObjectMapper mapper, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo, String requestStr, JsonNode requestNode) throws JsonProcessingException {
		if (StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getComponentName(), "Passport Investigation")
				&& StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getCheckStatus(), "Open")
				&& !StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getPassportStatus(), "Processed")) {
			try {
				passportService.processPassport(mapper, caseSpecificRecordDetail, caseSpecificInfo, requestStr,
						requestNode);
			} catch (JsonProcessingException e) {
				logger.error("Exception while processing Wellknown Router : {}", e.getMessage());
				e.printStackTrace();
			}
		}
	}
}