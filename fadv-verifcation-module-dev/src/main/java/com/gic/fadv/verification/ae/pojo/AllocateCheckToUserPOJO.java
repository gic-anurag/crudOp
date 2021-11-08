package com.gic.fadv.verification.ae.pojo;

import java.util.List;

import lombok.Data;

@Data
public class AllocateCheckToUserPOJO {
	 	private Long userId;
	 	private List<Long> caseSpecificRecordIds;
}
