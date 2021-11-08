package com.gic.fadv.verification.skill.pojo;

import java.util.List;

import lombok.Data;

@Data
public class SkillMapPOJO {
	private Long userId;
	private List<SkillPOJO> skills;
}
