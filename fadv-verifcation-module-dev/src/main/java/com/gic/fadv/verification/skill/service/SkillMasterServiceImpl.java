package com.gic.fadv.verification.skill.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gic.fadv.verification.skill.model.SkillMaster;
import com.gic.fadv.verification.skill.repository.SkillMasterRepository;

@Service
public class SkillMasterServiceImpl implements SkillMasterService {

	@Autowired
	private SkillMasterRepository skillMasterRepository;

	public List<SkillMaster> getAllSkill() {
		return skillMasterRepository.findAll();
	}

	public SkillMaster save(SkillMaster skillMaster) {
		return skillMasterRepository.save(skillMaster);

	}

	public void deleteById(long id) {
		skillMasterRepository.deleteById(id);

	}

//	public List<SkillMaster> getSkillMasterByFilter(SkillMasterPOJO skillMasterPOJO) {
//
//		boolean isFilter = false;
//		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//		CriteriaQuery<SkillMaster> criteriaQuery = criteriaBuilder.createQuery(SkillMaster.class);
//		Root<SkillMaster> itemRoot = criteriaQuery.from(SkillMaster.class);
//
//		List<Predicate> predicates = new ArrayList<>();
//		List<SkillMaster> skillMasterList = new ArrayList<>();
//
//		if (skillMasterPOJO.getSkillMasterId() != null) {
//			predicates.add(criteriaBuilder.equal(itemRoot.get("id"), skillMasterPOJO.getSkillMasterId()));
//			isFilter = true;
//		}
//		if (!StringUtils.isEmpty(skillMasterPOJO.getSkillDescription())) {
//			predicates.add(
//					criteriaBuilder.equal(itemRoot.get("skillDescription"), skillMasterPOJO.getSkillDescription()));
//			isFilter = true;
//		}
//
//		if (isFilter) {
//
//			criteriaQuery.where(predicates.toArray(new Predicate[0]));
//			skillMasterList = entityManager.createQuery(criteriaQuery).getResultList();
//		}
//
//		return skillMasterList;
//	}
}
