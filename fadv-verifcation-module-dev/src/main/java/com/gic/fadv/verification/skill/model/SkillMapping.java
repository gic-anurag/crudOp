package com.gic.fadv.verification.skill.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class SkillMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long skillMappingId;
	
	private Long userId;
	
	private Long skillId;
	
	private String skillType;  //will either be primary or secondary
}
