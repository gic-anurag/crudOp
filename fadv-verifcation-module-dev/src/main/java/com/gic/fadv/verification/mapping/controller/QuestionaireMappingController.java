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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gic.fadv.verification.mapping.model.QuestionaireMapping;
import com.gic.fadv.verification.mapping.pojo.QuestionaireMappingRequest;
import com.gic.fadv.verification.mapping.service.QuestionnerMappingService;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india/")
public class QuestionaireMappingController {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private QuestionnerMappingService questionnerMappingService;

	/**
	 * upload questioneire mapping csv file using this end point
	 * 
	 * @return
	 */
	@ApiOperation(value = "upload questioneire mapping csv file using this end point")
	@PostMapping("/upload-questioneire-csv")
	public ResponseEntity<String> saveQuestionnerMapping() {

		return new ResponseEntity<>(questionnerMappingService.saveQuestinnerCsv(), HttpStatus.CREATED);
	}

	/**
	 * This method is used to searchQuestionaireMapping by using component and
	 * productName fields
	 * 
	 * @param questionaireMappingRequest
	 * @return
	 */
	@ApiOperation(value = "This method is used to searchQuestionaireMapping by using component and productName  fields", response = List.class)
	@PostMapping("/getQuestionaire")
	public ResponseEntity<List<QuestionaireMapping>> getQuestionnerMapping(
			@RequestBody QuestionaireMappingRequest questionaireMappingRequest) {

		return new ResponseEntity<>(
				questionnerMappingService.getQuestionaireByComponentAndProductName(questionaireMappingRequest),
				HttpStatus.OK);
	}
	

	/**
	 * This method is used to searchQuestionaireMapping by filter on any field or
	 * multiple fields
	 * 
	 * @param questionaireMappingRequest
	 * @return
	 */
	@ApiOperation(value = "This service is used to search QuestionaireMapping by filter on any field or multiple fields", response = List.class)
	@PostMapping("/get-questionaire/search")
	public ResponseEntity<List<QuestionaireMapping>> getQuestionnerMappingByFilter(
			@RequestBody QuestionaireMappingRequest questionaireMappingRequest) {

		boolean isFilter = false;
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<QuestionaireMapping> criteriaQuery = criteriaBuilder.createQuery(QuestionaireMapping.class);
		Root<QuestionaireMapping> itemRoot = criteriaQuery.from(QuestionaireMapping.class);

		List<Predicate> predicates = new ArrayList<>();
		List<QuestionaireMapping> questioneireList = new ArrayList<>();

		if (questionaireMappingRequest.getId() != null && questionaireMappingRequest.getId() > 0) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("id"), questionaireMappingRequest.getId()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(questionaireMappingRequest.getComponent())) {
			predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(itemRoot.get("component")),
					questionaireMappingRequest.getComponent().trim().toLowerCase()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(questionaireMappingRequest.getProductName())) {
			predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(itemRoot.get("productName")),
					questionaireMappingRequest.getProductName().trim().toLowerCase()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(questionaireMappingRequest.getGlobalQuestionId())) {
			predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(itemRoot.get("globalQuestionId")),
					questionaireMappingRequest.getGlobalQuestionId().trim().toLowerCase()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(questionaireMappingRequest.getGlobalQuestion())) {
			predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(itemRoot.get("globalQuestion")),
					questionaireMappingRequest.getGlobalQuestion().trim().toLowerCase()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(questionaireMappingRequest.getQuestioneType())) {
			predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(itemRoot.get("questioneType")),
					questionaireMappingRequest.getQuestioneType().trim().toLowerCase()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(questionaireMappingRequest.getFormLabel())) {
			predicates.add(
					criteriaBuilder.equal(itemRoot.get("formLabel"),
							questionaireMappingRequest.getFormLabel().trim()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(questionaireMappingRequest.getReportLabel())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("reportLabel"),
					questionaireMappingRequest.getReportLabel().trim()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(questionaireMappingRequest.getMandatory())) {
			predicates.add(
					criteriaBuilder.equal(itemRoot.get("mandatory"), questionaireMappingRequest.getMandatory().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(questionaireMappingRequest.getInputType())) {
			predicates.add(
					criteriaBuilder.equal(itemRoot.get("inputType"), 
							questionaireMappingRequest.getInputType().trim()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(questionaireMappingRequest.getEntityName())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("entityName"),
					questionaireMappingRequest.getEntityName().trim()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(questionaireMappingRequest.getQuestioneScope())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("questioneScope"),
					questionaireMappingRequest.getQuestioneScope().trim()));
			isFilter = true;
		}

		if (isFilter) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("status"), "A"));
			criteriaQuery.where(predicates.toArray(new Predicate[0]));
			questioneireList = entityManager.createQuery(criteriaQuery).getResultList();
		}

		return ResponseEntity.ok().body(questioneireList);
	}

}
