
package com.gic.fadv.verification.skill.pojo;

import lombok.Data;

@Data
public class SkillMasterPOJO {

	private Long skillId;
	private String skillType;
	private String skillName;
	private String skillSubState;
	private int skillTarget;
	private String skillUnit;
	private boolean enableAutoAllocation;
	private Long userId;
}
