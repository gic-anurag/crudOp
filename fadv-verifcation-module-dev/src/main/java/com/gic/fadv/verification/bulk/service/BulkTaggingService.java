package com.gic.fadv.verification.bulk.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.bulk.interfaces.TemplateDetailsInterface;
import com.gic.fadv.verification.bulk.pojo.BulkTemplateHeadersPOJO;
import com.gic.fadv.verification.bulk.pojo.ClientDetailsPOJO;
import com.gic.fadv.verification.bulk.pojo.ComponentDetailsPOJO;
import com.gic.fadv.verification.bulk.pojo.PackageDetailsPOJO;
import com.gic.fadv.verification.bulk.pojo.ProductNamePOJO;
import com.gic.fadv.verification.bulk.pojo.SbuDetailsPOJO;

@Service
public interface BulkTaggingService {

	List<ClientDetailsPOJO> getClientDetailsFromL3();

	List<SbuDetailsPOJO> getSbuNameFromL3(String clientName);

	List<PackageDetailsPOJO> getPackageNameFromL3(String clientName, String sbuName);

	List<ComponentDetailsPOJO> getComponentNameList();

	List<ProductNamePOJO> getProductNameList(String componentName);

	ResponseEntity<ObjectNode> createTemplateHeader(BulkTemplateHeadersPOJO bulkTemplateHeadersPOJO);

	List<TemplateDetailsInterface> getTemplateNameList();

	ArrayNode getTemplateByName(String templateName);

//	ResponseEntity<ObjectNode> saveBulkTaggingDataMap(BulkTemplateMappingPOJO bulkTemplateMappingPOJO);

	ResponseEntity<ObjectNode> saveBulkTaggingData(BulkTemplateHeadersPOJO bulkTemplateHeadersPOJO);

	String getMappedTemplate(BulkTemplateHeadersPOJO bulkTemplateHeadersPOJO);

	List<TemplateDetailsInterface> getTemplateByComponentName(String componentName);
}
