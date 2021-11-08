package com.gic.fadv.verification.skill.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gic.fadv.verification.skill.model.SkillMaster;

@Service
public interface SkillMasterService {

	public List<SkillMaster> getAllSkill();

	public SkillMaster save(SkillMaster skillMaster);

	public void deleteById(long id);

}
