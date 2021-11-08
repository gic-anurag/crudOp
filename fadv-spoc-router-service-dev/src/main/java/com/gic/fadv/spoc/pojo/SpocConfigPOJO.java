package com.gic.fadv.spoc.pojo;

import java.util.List;

import lombok.Data;

@Data
public class SpocConfigPOJO {

	private Long templateNo;
	private List<String> toEmailAddress;
	private List<String> ccEmailAddress;
}
