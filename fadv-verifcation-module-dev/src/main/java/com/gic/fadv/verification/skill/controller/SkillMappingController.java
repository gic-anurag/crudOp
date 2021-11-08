package com.gic.fadv.verification.skill.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gic.fadv.verification.skill.pojo.SkillMapPOJO;
import com.gic.fadv.verification.skill.model.SkillMapping;
import com.gic.fadv.verification.skill.service.SkillMappingService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class SkillMappingController {

	private static final Logger logger = LoggerFactory.getLogger(SkillMappingController.class);

	@Autowired
	private SkillMappingService skillMappingService;

	@PostMapping("/map-skills")
	public List<SkillMapping> getAllSkill(@Valid @RequestBody SkillMapPOJO skillMapPOJO) {
		logger.info("Skill Mapping request : {}", skillMapPOJO);
		
		return skillMappingService.createSkillMapping(skillMapPOJO);
	}
}
