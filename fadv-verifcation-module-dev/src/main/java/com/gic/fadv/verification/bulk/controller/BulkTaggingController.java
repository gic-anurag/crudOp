package com.gic.fadv.verification.bulk.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.bulk.interfaces.TemplateDetailsInterface;
import com.gic.fadv.verification.bulk.pojo.BulkTemplateHeadersPOJO;
import com.gic.fadv.verification.bulk.pojo.ClientDetailsPOJO;
import com.gic.fadv.verification.bulk.pojo.ComponentDetailsPOJO;
import com.gic.fadv.verification.bulk.pojo.PackageDetailsPOJO;
import com.gic.fadv.verification.bulk.pojo.ProductNamePOJO;
import com.gic.fadv.verification.bulk.pojo.SbuDetailsPOJO;
import com.gic.fadv.verification.bulk.service.BulkTaggingService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class BulkTaggingController {

	private static final Logger logger = LoggerFactory.getLogger(BulkTaggingController.class);

	@Autowired
	private BulkTaggingService bulkTaggingService;

	@GetMapping("/l3-get-client-names-list")
	public List<ClientDetailsPOJO> getClientNameList() {
		return bulkTaggingService.getClientDetailsFromL3();
	}

	@GetMapping("/l3-get-sbu-name/{clientName}")
	public List<SbuDetailsPOJO> getSbuNameList(@PathVariable(value = "clientName") String clientName) {
		return bulkTaggingService.getSbuNameFromL3(clientName);
	}

	@GetMapping("/l3-get-package-name/{clientName}/{sbuName}")
	public List<PackageDetailsPOJO> getPackageNameList(@PathVariable(value = "clientName") String clientName,
			@PathVariable(value = "sbuName") String sbuName) {
		return bulkTaggingService.getPackageNameFromL3(clientName, sbuName);
	}

	@GetMapping("/l3-get-component-name")
	public List<ComponentDetailsPOJO> getComponentNameList() {
		return bulkTaggingService.getComponentNameList();
	}

	@GetMapping("/l3-get-product-name/{componentName}")
	public List<ProductNamePOJO> getProductNameList(@PathVariable(value = "componentName") String componentName) {
		return bulkTaggingService.getProductNameList(componentName);
	}

	@PostMapping(path = "/create-template-header", produces = "application/json")
	public ResponseEntity<ObjectNode> createTemplateHeader(
			@Valid @RequestBody BulkTemplateHeadersPOJO bulkTemplateHeadersPOJO) {
		return bulkTaggingService.createTemplateHeader(bulkTemplateHeadersPOJO);
	}

	@GetMapping("/get-template-name-list")
	public List<TemplateDetailsInterface> getTemplateNameList() {
		return bulkTaggingService.getTemplateNameList();
	}

	@GetMapping("/get-template-name-list/{componentName}")
	public List<TemplateDetailsInterface> getTemplateByComponentName(
			@PathVariable(value = "componentName") String componentName) {
		return bulkTaggingService.getTemplateByComponentName(componentName);
	}

	@PostMapping(path = "/download-template", produces = "application/json")
	public String downloadMappedTemplate(@Valid @RequestBody BulkTemplateHeadersPOJO bulkTemplateHeadersPOJO) {
		return bulkTaggingService.getMappedTemplate(bulkTemplateHeadersPOJO);
	}

	@PostMapping(path = "/upload-template", produces = "application/json")
	public ResponseEntity<ObjectNode> uploadMappedTemplate(
			@Valid @RequestBody BulkTemplateHeadersPOJO bulkTemplateHeadersPOJO) {
		logger.info("Data Received Successfully:{}", bulkTemplateHeadersPOJO);
		return bulkTaggingService.saveBulkTaggingData(bulkTemplateHeadersPOJO);
	}
	
//	@GetMapping("/get-template-name-list/{componentName}")
//	public ArrayNode getTemplateByName(@PathVariable(value = "componentName") String componentName) {
//		return bulkTaggingService.getTemplateByComponentName(componentName);
//	}

//	@PostMapping("/map-template-data")
//	public ResponseEntity<ObjectNode> processBulkTaggingData(
//			@Valid @RequestBody BulkTemplateMappingPOJO bulkTemplateMappingPOJO) {
//		return bulkTaggingService.saveBulkTaggingDataMap(bulkTemplateMappingPOJO);
//	}
}
