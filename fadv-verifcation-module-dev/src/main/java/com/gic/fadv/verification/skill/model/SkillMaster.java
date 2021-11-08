
package com.gic.fadv.verification.skill.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class SkillMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long skillId;
	private String skillType;
	private String skillName;
	private String skillSubState;
	private int skillTarget;
	private String skillUnit;
	private boolean enableAutoAllocation;
	private Long createdUserId;
	private Long updatedUserId;
	private Date createdDate;
	private Date updatedDate;
}
