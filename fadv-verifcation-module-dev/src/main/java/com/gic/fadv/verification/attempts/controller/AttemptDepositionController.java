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

import com.gic.fadv.verification.attempts.model.AttemptDeposition;
import com.gic.fadv.verification.attempts.repository.AttemptDepositionRepository;
import com.gic.fadv.verification.exception.ResourceNotFoundException;
import com.gic.fadv.verification.pojo.AttemptDespositionPOJO;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class AttemptDepositionController {
	
	@Autowired
	private AttemptDepositionRepository attemptDepositionRepository;
	
	@Autowired
	private EntityManager entityManager;

//	private static final Logger logger = LoggerFactory.getLogger(AttemptDepositionController.class);

	
	@GetMapping("/attempt-desposition")
	public List<AttemptDeposition> getAllStellars() {
		return attemptDepositionRepository.findAll();
	}
	
	@PostMapping("/attempt-desposition")
	public AttemptDeposition createAttemptDeposition(@Valid @RequestBody AttemptDeposition attemptDeposition) {
		attemptDeposition.setDepositionName(attemptDeposition.getDepositionName());
		attemptDeposition.setStatus("A");
		return attemptDepositionRepository.save(attemptDeposition);
	}
	
	@ApiOperation(value = "This service is used to search deposition by filter on any field or multiple fields", response = List.class)
	@PostMapping("/attempt-desposition/search")
	public ResponseEntity<List<AttemptDeposition>> getDespositionsByFilter(
			@RequestBody AttemptDespositionPOJO attemptDespositionPOJO) throws ResourceNotFoundException {
		boolean isFilter = false;
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<AttemptDeposition> criteriaQuery = criteriaBuilder.createQuery(AttemptDeposition.class);
		Root<AttemptDeposition> itemRoot = criteriaQuery.from(AttemptDeposition.class);

		List<Predicate> predicates = new ArrayList<>();
		List<AttemptDeposition> attemptDepositionList = new ArrayList<>();

		if (attemptDespositionPOJO.getId() != 0) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("id"), attemptDespositionPOJO.getId()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(attemptDespositionPOJO.getDepositionName())) {
			predicates.add(
					criteriaBuilder.equal(itemRoot.get("depositionName"), attemptDespositionPOJO.getDepositionName()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(attemptDespositionPOJO.getUserid())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("userid"), attemptDespositionPOJO.getUserid()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(attemptDespositionPOJO.getStatus())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("status"), attemptDespositionPOJO.getStatus()));
			isFilter = true;
		}

		if (isFilter) {
			criteriaQuery.where(predicates.toArray(new Predicate[0]));
			attemptDepositionList = entityManager.createQuery(criteriaQuery).getResultList();
		}

		return ResponseEntity.ok().body(attemptDepositionList);
	}
}
