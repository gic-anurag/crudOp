package com.gic.fadv.vendor.input.controller;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.vendor.input.service.VendorInputScheduledService;
import com.gic.fadv.vendor.input.service.VendorInputService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/india")
@Api(value = "Cross Reference")
public class VendorInputController {

	@Autowired
	private VendorInputScheduledService vendorInputScheduledService;

	@Autowired
	private VendorInputService vendorInputServiceNew;

	private static final Logger logger = LoggerFactory.getLogger(VendorInputController.class);

	@GetMapping("/vendor-input-schedule")
	public void inputScheduler() {
		vendorInputScheduledService.getVendorRequests();
	}

	@ApiOperation(value = "This service is used to process Records at vendor input router and return the result to workflow router ", response = List.class)
	@PostMapping(path = "/vendorinputrouter", consumes = "application/json", produces = "application/json")
	public ObjectNode doRecordProcess(@RequestBody JsonNode requestBody) {
		logger.info("Got Request :\n {}", requestBody);
		ObjectNode response = vendorInputServiceNew.processRequestBody(requestBody);
		logger.info("Record request Processed");
		return response;
	}

	@PostMapping("/vendor-mrl-check")
	public List<String> getVendorMrlDoc(@RequestBody JsonNode requestBody) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String componentName = requestBody.has("componentName") ? requestBody.get("componentName").asText() : "";
		String akaName = requestBody.has("akaName") ? requestBody.get("akaName").asText() : "";
		String checkId = requestBody.has("checkId") ? requestBody.get("checkId").asText() : "";
		String caseNumber = requestBody.has("caseNumber") ? requestBody.get("caseNumber").asText() : "";

		return vendorInputScheduledService.getMrlDocumentNames(mapper, componentName, akaName, checkId, caseNumber);
	}

}
