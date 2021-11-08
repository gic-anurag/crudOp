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

import com.gic.fadv.verification.attempts.model.AttemptVerificationModes;
import com.gic.fadv.verification.attempts.repository.AttemptVerificationModesRepository;
import com.gic.fadv.verification.exception.ResourceNotFoundException;
import com.gic.fadv.verification.pojo.AttemptVerificationModesPOJO;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class AttemptVerificationModesController {
	
	@Autowired
	private AttemptVerificationModesRepository attemptVerificationModesRepository;
	
	@Autowired
	private EntityManager entityManager;

//	private static final Logger logger = LoggerFactory.getLogger(AttemptVerificationModesController.class);

	
	@GetMapping("/attempt-verification-modes")
	public List<AttemptVerificationModes> getAllStellars() {
		return attemptVerificationModesRepository.findAll();
	}
	
	@PostMapping("/attempt-verification-modes")
	public AttemptVerificationModes createAttemptDeposition(@Valid @RequestBody AttemptVerificationModes attemptVerificationModes) {
		attemptVerificationModes.setVerificationMode(attemptVerificationModes.getVerificationMode());
		attemptVerificationModes.setCatergoryMode(attemptVerificationModes.getCatergoryMode());
		attemptVerificationModes.setUserid(attemptVerificationModes.getUserid());
		attemptVerificationModes.setStatus("A");
		return attemptVerificationModesRepository.save(attemptVerificationModes);
	}
	
	@ApiOperation(value = "This service is used to search verification modes by filter on any field or multiple fields", response = List.class)
	@PostMapping("/attempt-verification-modes/search")
	public ResponseEntity<List<AttemptVerificationModes>> getVerificationModesByFilter(
			@RequestBody AttemptVerificationModesPOJO attemptVerificationModesPOJO) throws ResourceNotFoundException {
		boolean isFilter = false;
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<AttemptVerificationModes> criteriaQuery = criteriaBuilder.createQuery(AttemptVerificationModes.class);
		Root<AttemptVerificationModes> itemRoot = criteriaQuery.from(AttemptVerificationModes.class);

		List<Predicate> predicates = new ArrayList<>();
		List<AttemptVerificationModes> attemptVerificationModesList = new ArrayList<AttemptVerificationModes>();

		if (attemptVerificationModesPOJO.getId() != 0) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("id"), attemptVerificationModesPOJO.getId()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(attemptVerificationModesPOJO.getCatergoryMode())) {
			predicates.add(
					criteriaBuilder.equal(itemRoot.get("catergoryMode"), attemptVerificationModesPOJO.getCatergoryMode()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(attemptVerificationModesPOJO.getVerificationMode())) {
			predicates.add(
					criteriaBuilder.equal(itemRoot.get("verificationMode"), attemptVerificationModesPOJO.getVerificationMode()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(attemptVerificationModesPOJO.getUserid())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("userid"), attemptVerificationModesPOJO.getUserid()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(attemptVerificationModesPOJO.getStatus())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("status"), attemptVerificationModesPOJO.getStatus()));
			isFilter = true;
		}

		if (isFilter) {
			criteriaQuery.where(predicates.toArray(new Predicate[0]));
			attemptVerificationModesList = entityManager.createQuery(criteriaQuery).getResultList();
		}

		return ResponseEntity.ok().body(attemptVerificationModesList);
	}
}
