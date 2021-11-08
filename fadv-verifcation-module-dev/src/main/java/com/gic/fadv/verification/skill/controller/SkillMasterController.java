package com.gic.fadv.verification.skill.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gic.fadv.verification.skill.pojo.SkillMasterPOJO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gic.fadv.verification.online.service.OnlineApiService;
import com.gic.fadv.verification.skill.model.SkillMaster;
import com.gic.fadv.verification.skill.service.SkillMasterService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class SkillMasterController {

	private static final Logger logger = LoggerFactory.getLogger(SkillMasterController.class);

	@Autowired
	private SkillMasterService skillMasterService;
	@Autowired
	private OnlineApiService onlineApiService;

	@Value("${component.list.l3}")
	private String componentListL3Url;

	@GetMapping("/get-skills")
	public List<SkillMaster> getAllSkill() {
		return skillMasterService.getAllSkill();
	}

	@PostMapping("/create-skill")
	public SkillMaster createSkillMaster(@Valid @RequestBody SkillMasterPOJO skillMasterPOJO) {
		logger.info("Skill to create : {}", skillMasterPOJO);
		SkillMaster skillMaster = new SkillMaster();
		Long skillId = skillMasterPOJO.getSkillId() != null ? skillMasterPOJO.getSkillId() : 0;
		if (skillId != 0) {
			skillMaster.setSkillId(skillId);
		} else {
			skillMaster.setCreatedUserId(skillMasterPOJO.getUserId());
			skillMaster.setCreatedDate(new Date());
		}
		skillMaster.setUpdatedDate(new Date());
		skillMaster.setUpdatedUserId(skillMasterPOJO.getUserId());
		skillMaster.setSkillName(skillMasterPOJO.getSkillName());
		skillMaster.setSkillSubState(skillMasterPOJO.getSkillSubState());
		skillMaster.setSkillTarget(skillMasterPOJO.getSkillTarget());
		skillMaster.setSkillType(skillMasterPOJO.getSkillType());
		skillMaster.setSkillUnit(skillMasterPOJO.getSkillUnit());

		return skillMasterService.save(skillMaster);
	}

	@DeleteMapping("/remove-skill/{skillId}")
	public ResponseEntity<String> deleteById(@PathVariable(name = "skillId", required = true) Long skillId) {
		try {
			skillMasterService.deleteById(skillId);
			return ResponseEntity.ok().body("DELETED");
		} catch (Exception e) {
			logger.error("Exception occurred while removing skill : {}", e.getMessage());
			return ResponseEntity.status(500).body("INTERNAL_SERVER_ERROR");
		}
	}

	@GetMapping("/get-component-List")
	public List<String> getComponentList() {
		String componentResponse = onlineApiService.sendDataToL3Get(componentListL3Url);
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode componentResponseNode = mapper.readValue(componentResponse, JsonNode.class);
			ArrayNode responseNode = componentResponseNode.has("response")
					? (ArrayNode) componentResponseNode.get("response")
					: mapper.createArrayNode();
			if (responseNode != null && !responseNode.isEmpty()) {
				List<String> componentList = new ArrayList<>();
				for (JsonNode response : responseNode) {
					String componentName = response.has("componentName")
							? response.get("componentName").asText()
							: "";
					if (componentName != null && StringUtils.isNotEmpty(componentName)) {
						componentList.add(componentName);
					}
				}

				return componentList;
			}
		} catch (JsonProcessingException e) {
			logger.error("Exception occurred while Component Name Mapping : {}", e.getMessage());
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
}
