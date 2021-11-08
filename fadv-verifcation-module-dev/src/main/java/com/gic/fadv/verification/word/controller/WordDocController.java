package com.gic.fadv.verification.word.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.fadv.verification.attempts.repository.CaseSpecificRecordDetailRepository;
import com.gic.fadv.verification.utility.Utility;
import com.gic.fadv.verification.word.pojo.WordFileDetailsPOJO;
import com.gic.fadv.verification.word.pojo.WordFileInputPOJO;
import com.gic.fadv.verification.word.service.WordDocumentService;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class WordDocController {

	@Autowired
	WordDocumentService woDocumentService;

	private static final Logger logger = LoggerFactory.getLogger(WordDocController.class);
	
	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;

	@PostMapping("/create-letter")
	public ResponseEntity<String> createDoc(@RequestBody JsonNode requestStr, HttpServletResponse response) {
		// Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					
		String checkId = requestStr.has("checkId") ? requestStr.get("checkId").asText() : "";
		Long requestNo = requestStr.has("requestNo") ? requestStr.get("requestNo").asLong() : 0;
		
		String institutionName = "NA";
		String state = "NA";
		String vendorBillingAddress = "NA";
		String gstNumber = "NA";
		String vendorName = "NA";
		String code = "NA";
		String date = "NA";
		String attn = "NA";
		String candidateName = "NA";
		String qualification = "NA";
		String majors = "NA";
		String yearOfPassing = "NA";
		String yearOfGraduation = "NA";
		String rollNo = "NA";	
		

		if (StringUtils.isNotEmpty(checkId) && requestNo != 0) {
			try {
				WordFileInputPOJO wordFileInputPOJO = new WordFileInputPOJO();
				WordFileDetailsPOJO wordFileDetailsPOJO = caseSpecificRecordDetailRepository.getWordFileDetails(checkId,
						requestNo);
				if (wordFileDetailsPOJO != null) {
					JsonNode componentRecordField = mapper.readTree(wordFileDetailsPOJO.getComponentRecordField());
					institutionName = componentRecordField.has("Aka Name") ? componentRecordField.get("Aka Name").asText() : "NA";
					state = componentRecordField.has("University Location (State)") ? componentRecordField.get("University Location (State)").asText() : "NA";
					candidateName = wordFileDetailsPOJO.getCandidateName();
					yearOfPassing = componentRecordField.has("Year of Passing") ? componentRecordField.get("Year of Passing").asText() : "NA";
					
				}
				wordFileInputPOJO.setCheckId(checkId);
				wordFileInputPOJO.setInstitutionName(institutionName);
				wordFileInputPOJO.setState(state);
				wordFileInputPOJO.setVendorBillingAddress(vendorBillingAddress);
				wordFileInputPOJO.setGstNumber(gstNumber);
				wordFileInputPOJO.setVendorName(vendorName);
				wordFileInputPOJO.setCode(code);
				wordFileInputPOJO.setDate(date);
				wordFileInputPOJO.setAttn(attn);
				wordFileInputPOJO.setCandidateName(candidateName);
				wordFileInputPOJO.setQualification(qualification);
				wordFileInputPOJO.setMajors(majors);
				wordFileInputPOJO.setYearOfPassing(yearOfPassing);
				wordFileInputPOJO.setYearOfGraduation(yearOfGraduation);
				wordFileInputPOJO.setRollNo(rollNo);
				
				String filePath = woDocumentService.handleSimpleDoc(wordFileInputPOJO);
				
				File fileNameObject = new File(filePath);
	            logger.info(fileNameObject.getName());
	            try ( InputStream inputStream = new FileInputStream(fileNameObject)){
	                response.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
	                IOUtils.copy(inputStream, response.getOutputStream());
	                response.getOutputStream().flush();
	                Utility.removeAttachmentFile(fileNameObject);
	            } catch (IOException e) {
	                logger.error(e.getMessage(), e);
	            }
				
				return ResponseEntity.ok().body("Letter Created");
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Some error occurred");
			}
		} else {
			return ResponseEntity.badRequest().body("Please provide checkId and requestNo");
		}

	}
}
