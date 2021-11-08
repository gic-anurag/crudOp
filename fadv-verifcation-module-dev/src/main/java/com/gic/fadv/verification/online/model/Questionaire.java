package com.gic.fadv.verification.online.model;

import lombok.Data;

@Data
public class Questionaire {
	private String verifiedData;
	private String question;
	private String answer;
	private String caseQuestionRefID;
	private String reportData;
	private String status;
}