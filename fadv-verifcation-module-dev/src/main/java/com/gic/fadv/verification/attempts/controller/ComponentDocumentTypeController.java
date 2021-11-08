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
import com.gic.fadv.verification.attempts.model.ComponentDocumentType;
import com.gic.fadv.verification.attempts.repository.ComponentDocumentTypeRepository;
import com.gic.fadv.verification.pojo.ComponentDocumentTypePOJO;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class ComponentDocumentTypeController {

	@Autowired
	private ComponentDocumentTypeRepository componentDocumentTypeRepository;

	@Autowired
	private EntityManager entityManager;

	//	private static final Logger logger = LoggerFactory.getLogger(ComponentDocumentTypeController.class);

	@GetMapping("/component-document")
	public List<ComponentDocumentType> getAllComponentDocumentType() {
		return componentDocumentTypeRepository.findAll();
	}

	@PostMapping("/component-document")
	public ComponentDocumentType createComponentDocumentType(
			@Valid @RequestBody ComponentDocumentType componentDocumentType) {
		componentDocumentType.setComponentName(componentDocumentType.getComponentName());
		componentDocumentType.setDocumentType(componentDocumentType.getDocumentType());
		componentDocumentType.setStatus("A");
		return componentDocumentTypeRepository.save(componentDocumentType);
	}

	@ApiOperation(value = "This service is used to search deposition by filter on any field or multiple fields", response = List.class)
	@PostMapping("/component-document/search")
	public ResponseEntity<List<ComponentDocumentType>> getDocumentsByFilter(
			@RequestBody ComponentDocumentTypePOJO componentDocumentTypePOJO) {
		boolean isFilter = false;
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ComponentDocumentType> criteriaQuery = criteriaBuilder.createQuery(ComponentDocumentType.class);
		Root<ComponentDocumentType> itemRoot = criteriaQuery.from(ComponentDocumentType.class);

		List<Predicate> predicates = new ArrayList<>();
		List<ComponentDocumentType> componentDocumentList = new ArrayList<>();

		if (componentDocumentTypePOJO.getId() != 0) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("id"), componentDocumentTypePOJO.getId()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(componentDocumentTypePOJO.getComponentName())) {
			predicates.add(
					criteriaBuilder.equal(itemRoot.get("componentName"), componentDocumentTypePOJO.getComponentName()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(componentDocumentTypePOJO.getDocumentType())) {
			predicates.add(
					criteriaBuilder.equal(itemRoot.get("documentType"), componentDocumentTypePOJO.getDocumentType()));
			isFilter = true;
		}

		if (!StringUtils.isEmpty(componentDocumentTypePOJO.getUserid())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("userid"), componentDocumentTypePOJO.getUserid()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(componentDocumentTypePOJO.getStatus())) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("status"), componentDocumentTypePOJO.getStatus()));
			isFilter = true;
		}

		if (isFilter) {
			criteriaQuery.where(predicates.toArray(new Predicate[0]));
			componentDocumentList = entityManager.createQuery(criteriaQuery).getResultList();
		}

		return ResponseEntity.ok().body(componentDocumentList);
	}
}
