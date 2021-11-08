package com.gic.fadv.verification.skill.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gic.fadv.verification.skill.model.SkillMapping;
import com.gic.fadv.verification.skill.pojo.SkillMapPOJO;

@Service
public interface SkillMappingService {

	List<SkillMapping> createSkillMapping(SkillMapPOJO skillMapPOJO);

}
