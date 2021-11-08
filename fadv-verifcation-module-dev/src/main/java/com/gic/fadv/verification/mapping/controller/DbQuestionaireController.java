package com.gic.fadv.verification.mapping.controller;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gic.fadv.verification.mapping.model.DbQuestionaireMapping;
import com.gic.fadv.verification.mapping.pojo.DbQuestionaireMappingRequest;
import com.gic.fadv.verification.mapping.service.DbQuestionaireMappingService;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india/")
public class DbQuestionaireController {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private DbQuestionaireMappingService dbQuestionaireMappingService;

	/**
	 * upload questioneire mapping csv file using this end point
	 * 
	 * @return
	 */
	@ApiOperation(value = "upload db questioneire mapping csv file using this end point")
	@PostMapping("/upload-dbquestioneire-csv")
	public ResponseEntity<String> saveDbQuestionnerMapping() {

		return new ResponseEntity<>(dbQuestionaireMappingService.saveDbQuestinneireCsv(), HttpStatus.CREATED);
	}

	/**
	 * 
	 * @param questionaireMappingRequest
	 * @return
	 */
	@ApiOperation(value = "This service is used to search QuestionaireMapping by filter on any field or multiple fields", response = List.class)
	@PostMapping("/get-dbquestionaire/search")
	public ResponseEntity<List<DbQuestionaireMapping>> getDbQuestionnerMappingByFilter(
			@RequestBody DbQuestionaireMappingRequest dbQuestionaireMappingRequest) {

		boolean isFilter = false;
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<DbQuestionaireMapping> criteriaQuery = criteriaBuilder.createQuery(DbQuestionaireMapping.class);
		Root<DbQuestionaireMapping> itemRoot = criteriaQuery.from(DbQuestionaireMapping.class);

		List<Predicate> predicates = new ArrayList<>();
		List<DbQuestionaireMapping> questioneireList = new ArrayList<>();

		if (dbQuestionaireMappingRequest.getId() != null && dbQuestionaireMappingRequest.getId() > 0) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("id"), dbQuestionaireMappingRequest.getId()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getComponentName())) {
			predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(itemRoot.get("componentName")),
					dbQuestionaireMappingRequest.getComponentName().trim().toLowerCase()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getProductName())) {
			predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(itemRoot.get("productName")),
					dbQuestionaireMappingRequest.getProductName().trim().toLowerCase()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getGlobalQuestionId())) {
			predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(itemRoot.get("globalQuestionId")),
					dbQuestionaireMappingRequest.getGlobalQuestionId().trim().toLowerCase()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getGlobalQuestion())) {
			predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(itemRoot.get("globalQuestion")),
					dbQuestionaireMappingRequest.getGlobalQuestion().trim().toLowerCase()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getQuestioneType())) {
			predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(itemRoot.get("questioneType")),
					dbQuestionaireMappingRequest.getQuestioneType().trim().toLowerCase()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getPackageQuestion())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("packageQuestion"),
					dbQuestionaireMappingRequest.getPackageQuestion().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getReportLabel())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("reportLabel"),
					dbQuestionaireMappingRequest.getReportLabel().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getProductQuestionId())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("productQuestionId"),
					dbQuestionaireMappingRequest.getProductQuestionId().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getProductId())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("productId"),
					dbQuestionaireMappingRequest.getProductId().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getComponentId())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("componentId"),
					dbQuestionaireMappingRequest.getComponentId().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getPqPrecedence())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("pqPrecedence"),
					dbQuestionaireMappingRequest.getPqPrecedence().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getIsRequired())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("isRequired"),
					dbQuestionaireMappingRequest.getIsRequired().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getIsReportRequired())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("isReportRequired"),
					dbQuestionaireMappingRequest.getIsReportRequired().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getEntityName())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("entityName"),
					dbQuestionaireMappingRequest.getEntityName().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getQuestioneScope())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("questioneScope"),
					dbQuestionaireMappingRequest.getQuestioneScope().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getPkgCompProdQstnId())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("pkgCompProdQstnId"),
					dbQuestionaireMappingRequest.getPkgCompProdQstnId().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getPackageCompProductId())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("packageCompProductId"),
					dbQuestionaireMappingRequest.getPackageCompProductId().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getTaggingType())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("taggingType"),
					dbQuestionaireMappingRequest.getTaggingType().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getRemarks())) {
			predicates.add(
					criteriaBuilder.equal(itemRoot.get("remarks"), dbQuestionaireMappingRequest.getRemarks().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getStatus())) {
			predicates.add(
					criteriaBuilder.equal(itemRoot.get("status"), dbQuestionaireMappingRequest.getStatus().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getVerifiedData())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("verifiedData"),
					dbQuestionaireMappingRequest.getVerifiedData().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(dbQuestionaireMappingRequest.getReportComments())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("reportComments"),
					dbQuestionaireMappingRequest.getReportComments().trim()));
			isFilter = true;
		}

		if (isFilter) {
			criteriaQuery.where(predicates.toArray(new Predicate[0]));
			questioneireList = entityManager.createQuery(criteriaQuery).getResultList();
		}

		return ResponseEntity.ok().body(questioneireList);
	}
	

	@GetMapping("/get-componenets")
	public ResponseEntity<List<String>>getcomponents() {
		List<String>cmpList=dbQuestionaireMappingService.getDistictComponentNames();
		 return new ResponseEntity<>(cmpList,HttpStatus.OK);
	}
	
	@GetMapping("/get-products")
	public ResponseEntity<List<String>>getproducts() {
		
		return new ResponseEntity<>(dbQuestionaireMappingService.getDistictproductNames(),HttpStatus.OK);
	}

	

}
