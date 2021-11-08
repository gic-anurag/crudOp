package com.gic.fadv.verification.skill.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gic.fadv.verification.skill.model.SkillMapping;
import com.gic.fadv.verification.skill.pojo.SkillMapPOJO;
import com.gic.fadv.verification.skill.pojo.SkillPOJO;
import com.gic.fadv.verification.skill.repository.SkillMappingRepository;

@Service
public class SkillMappingServiceImpl implements SkillMappingService {

	@Autowired
	private SkillMappingRepository skillMappingRepository;

	@Override
	public List<SkillMapping> createSkillMapping(SkillMapPOJO skillMapPOJO) {
		List<SkillMapping> skillMappings = new ArrayList<>();
		List<SkillPOJO> skillPOJOs = skillMapPOJO.getSkills() != null ? skillMapPOJO.getSkills() : new ArrayList<>();
		
		for (SkillPOJO skillPOJO : skillPOJOs) {
			SkillMapping skillMapping = new SkillMapping();
			skillMapping.setUserId(skillMapPOJO.getUserId());
			skillMapping.setSkillId(skillPOJO.getSkillId());
			skillMapping.setSkillType(skillPOJO.getSkillType());
			skillMappings.add(skillMapping);
		}
		if (CollectionUtils.isNotEmpty(skillMappings)) {
			return skillMappingRepository.saveAll(skillMappings);
		}
		return new ArrayList<>();
	}
}
