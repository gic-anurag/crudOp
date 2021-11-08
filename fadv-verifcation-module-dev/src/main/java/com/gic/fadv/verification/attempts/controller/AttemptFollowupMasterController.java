package com.gic.fadv.verification.attempts.controller;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.gic.fadv.verification.attempts.model.AttemptFollowupMaster;
import com.gic.fadv.verification.attempts.repository.AttemptFollowupMasterRepository;
import com.gic.fadv.verification.pojo.AttemptFollowUpMasterPOJO;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class AttemptFollowupMasterController {
	
//	@Autowired
//	private APIService apiService;
	@Autowired
	private AttemptFollowupMasterRepository attemptFollowupMasterRepository;
	
	@Autowired
	private EntityManager entityManager;

//	private static final Logger logger = LoggerFactory.getLogger(AttemptFollowupMasterController.class);

	
	@GetMapping("/attempt-followup")
	public List<AttemptFollowupMaster> getAllStellars() {
		return attemptFollowupMasterRepository.findAll();
	}
	
	@PostMapping("/attempt-followup")
	public AttemptFollowupMaster createAttemptDeposition(@Valid @RequestBody AttemptFollowupMaster attemptFollowupMaster) {
		attemptFollowupMaster.setFollowupStatus(attemptFollowupMaster.getFollowupStatus());
		attemptFollowupMaster.setFollowupDescription(attemptFollowupMaster.getFollowupDescription());
		attemptFollowupMaster.setActionType(attemptFollowupMaster.getActionType());
		attemptFollowupMaster.setRelationToCspi(attemptFollowupMaster.getRelationToCspi());
		attemptFollowupMaster.setCheckFlow(attemptFollowupMaster.getCheckFlow());
		attemptFollowupMaster.setComments(attemptFollowupMaster.getComments());
		attemptFollowupMaster.setIsActive(1);
		return attemptFollowupMasterRepository.save(attemptFollowupMaster);
	}

	@ApiOperation(value = "This service is used to search followup master by filter on any field or multiple fields", response = List.class)
	@PostMapping("/attempt-followup/search")
	public ResponseEntity<List<AttemptFollowupMaster>> getFollowUpByFilter(
			@RequestBody AttemptFollowUpMasterPOJO attemptFollowUpMasterPOJO)  {
		boolean isFilter = false;
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<AttemptFollowupMaster> criteriaQuery = criteriaBuilder.createQuery(AttemptFollowupMaster.class);
		Root<AttemptFollowupMaster> itemRoot = criteriaQuery.from(AttemptFollowupMaster.class);

		List<Predicate> predicates = new ArrayList<>();
		List<AttemptFollowupMaster> attemptFollowupMasterList = new ArrayList<AttemptFollowupMaster>();

		if (attemptFollowUpMasterPOJO.getFollowupId() != null) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("followupId"), attemptFollowUpMasterPOJO.getFollowupId()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(attemptFollowUpMasterPOJO.getFollowupStatus())) {
			predicates.add(
					criteriaBuilder.equal(itemRoot.get("followupStatus"), attemptFollowUpMasterPOJO.getFollowupStatus()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(attemptFollowUpMasterPOJO.getUserid())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("userid"), attemptFollowUpMasterPOJO.getUserid()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(attemptFollowUpMasterPOJO.getComments())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("comments"), attemptFollowUpMasterPOJO.getComments()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(attemptFollowUpMasterPOJO.getFollowupDescription())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("followupDescription"), attemptFollowUpMasterPOJO.getFollowupDescription()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(attemptFollowUpMasterPOJO.getActionType())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("actionType"), attemptFollowUpMasterPOJO.getActionType()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(attemptFollowUpMasterPOJO.getRelationToCspi())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("relationToCspi"), attemptFollowUpMasterPOJO.getRelationToCspi()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(attemptFollowUpMasterPOJO.getCheckFlow())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("checkFlow"), attemptFollowUpMasterPOJO.getCheckFlow()));
			isFilter = true;
		}
		if (isFilter) {
			criteriaQuery.where(predicates.toArray(new Predicate[0]));
			attemptFollowupMasterList = entityManager.createQuery(criteriaQuery).getResultList();
		}

		return ResponseEntity.ok().body(attemptFollowupMasterList);
	}
}
