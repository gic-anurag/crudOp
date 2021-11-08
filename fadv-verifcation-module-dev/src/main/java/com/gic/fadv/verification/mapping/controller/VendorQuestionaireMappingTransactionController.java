package com.gic.fadv.verification.mapping.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gic.fadv.verification.mapping.interfaces.MappedQuestionsInterface;
import com.gic.fadv.verification.mapping.interfaces.VendorQuestionnaireInterface;
import com.gic.fadv.verification.mapping.model.VendorComponentMaster;
import com.gic.fadv.verification.mapping.model.VendorProductMaster;
import com.gic.fadv.verification.mapping.model.VendorQuestionaireTransaction;
import com.gic.fadv.verification.mapping.pojo.QuestionaireRequestPojo;
import com.gic.fadv.verification.mapping.pojo.QuestioneireMappingRes;
import com.gic.fadv.verification.mapping.pojo.VendorQuestionaireMappingRequest;
import com.gic.fadv.verification.mapping.pojo.VendorQuestionaireTransactionPojo;
import com.gic.fadv.verification.mapping.pojo.VendorQuestionnairePOJO;
import com.gic.fadv.verification.mapping.repository.VendorComponentMasterRepository;
import com.gic.fadv.verification.mapping.repository.VendorProductMasterRepository;
import com.gic.fadv.verification.mapping.repository.VendorQuestionaireTransactionRepository;
import com.gic.fadv.verification.mapping.service.VendorQuestionnaireService;
import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india/")

public class VendorQuestionaireMappingTransactionController {

	private static final Logger logger = LoggerFactory.getLogger(VendorQuestionaireMappingTransactionController.class);

	@Autowired
	private VendorQuestionaireTransactionRepository vendorQuestionaireTransactionRepository;

	@Autowired
	private VendorComponentMasterRepository vendorComponentMasterRepository;

	@Autowired
	private VendorProductMasterRepository vendorProductMasterRepository;

	@Autowired
	private VendorQuestionnaireService vendorQuestionnaireService;

	/**
	 * Get Specific ComponentNames for Vendor QuestionneirMapping
	 * 
	 * @return
	 */

	@GetMapping("/get-component-list")
	public ResponseEntity<List<VendorComponentMaster>> getComponentMasters() {

		return new ResponseEntity<>(vendorComponentMasterRepository.findAll(), HttpStatus.OK);

	}

	/**
	 * Get ProductNames for Vendor QuestionneirMapping
	 * 
	 * @return
	 */
	@GetMapping("/get-product-list/{componentId}")
	public ResponseEntity<List<VendorProductMaster>> getProductMasters(@PathVariable Long componentId) {

		return new ResponseEntity<>(vendorProductMasterRepository.getAllProductsByComponentId(componentId),
				HttpStatus.OK);
	}

	/**
	 * add new verification questioneire mapping transaction
	 * 
	 * @return
	 */
	@ApiOperation(value = "add new QuestioneireTransaction")
	@PostMapping("/save-vendor-questioneire")
	public ResponseEntity<List<VendorQuestionaireTransaction>> saveQuestionnereTransaction(
			@RequestBody VendorQuestionaireMappingRequest vendorQuestionaireMappingRequest) {

		logger.info("Start of save-vendor-questioneire : {} ", vendorQuestionaireMappingRequest);
		List<VendorQuestionaireTransaction> qtListres = new ArrayList<>();
		VendorQuestionaireTransaction qt = null;

		String conmponentName = vendorQuestionaireMappingRequest.getComponentName();
		String type = vendorQuestionaireMappingRequest.getType();
		for (VendorQuestionaireTransactionPojo qtPojo : vendorQuestionaireMappingRequest.getDataArray()) {

			qt = vendorQuestionaireTransactionRepository
					.getQuestionaireTransactionByQuestioneireMapping(qtPojo.getId());
			if (qt != null) {
				qt.setQuestionaireMappingId(qtPojo.getId());
				qt.setFieldMapping(qtPojo.getUpdatedtext());
				qt.setStatus(qtPojo.getStatus());
				qt.setVerifiedData(qtPojo.getVerifiedData());
				if(conmponentName.equalsIgnoreCase("Education")) {
					qt.setType(type);
				}
				VendorQuestionaireTransaction qtRes = vendorQuestionaireTransactionRepository.save(qt);
				qtListres.add(qtRes);
			} else {
				qt = new VendorQuestionaireTransaction();
				qt.setQuestionaireMappingId(qtPojo.getId());
				qt.setFieldMapping(qtPojo.getUpdatedtext());
				qt.setStatus(qtPojo.getStatus());
				qt.setVerifiedData(qtPojo.getVerifiedData());
				qt.setComponentName(conmponentName);
				if(conmponentName.equalsIgnoreCase("Education")) {
					qt.setType(type);
				}
				VendorQuestionaireTransaction qtRes = vendorQuestionaireTransactionRepository.save(qt);
				qtListres.add(qtRes);
			}
		}
		if (!qtListres.isEmpty())
			return new ResponseEntity<>(qtListres, HttpStatus.CREATED);

		return new ResponseEntity<>(qtListres, HttpStatus.EXPECTATION_FAILED);
	}

	/**
	 * get questioneire transaction details with questionere maping by component and
	 * productName
	 * 
	 * @param questionaireRequestPojo
	 * @return
	 */
	@ApiOperation(value = "get questioneire transaction  details with questionere maping by component and productName")
	@PostMapping("/get-vendor-questionere-details")
	public ResponseEntity<List<QuestioneireMappingRes>> getQuestionerTansactionByComponentAndProductName(
			@RequestBody QuestionaireRequestPojo questionaireRequestPojo) {
		return new ResponseEntity<>(
				vendorQuestionaireTransactionRepository.getQuestionaireTransactionByComponentAndProductName(
						questionaireRequestPojo.getComponent(), questionaireRequestPojo.getProductName()),
				HttpStatus.OK);
	}

	@PostMapping("/get-mapped-questionnaires")
	public List<MappedQuestionsInterface> getMappedQuestionnaires(
			@RequestBody QuestionaireRequestPojo questionaireRequestPojo) {
		return vendorQuestionnaireService.getQuetionnaireMapping(questionaireRequestPojo);
	}

	@PostMapping("/fetch-questionnaire-mapping")
	public List<VendorQuestionnaireInterface> fetchQuetionnaireMapping(
			@RequestBody VendorQuestionnairePOJO vendorQuestionnairePOJO) {

		if (vendorQuestionnairePOJO != null) {
			return vendorQuestionnaireService.fetchQuetionnaireMapping(vendorQuestionnairePOJO);
		}

		return new ArrayList<>();
	}

	@PostMapping("/save-questionnaire-mapping")
	public List<VendorQuestionnaireInterface> saveQuetionnaireMapping(
			@RequestBody List<VendorQuestionnairePOJO> vendorQuestionnairePOJOs) {

		if (CollectionUtils.isNotEmpty(vendorQuestionnairePOJOs)) {
			return vendorQuestionnaireService.saveQuestionnaireMapping(vendorQuestionnairePOJOs);
		}

		return new ArrayList<>();
	}
}
