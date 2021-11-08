package com.gic.fadv.verification.online.model;

import java.util.List;

import com.gic.fadv.verification.pojo.QuestionnairePOJO;

import lombok.Data;

@Data
public class TaskSpecs {
	private CaseReference caseReference;
	private CheckVerification checkVerification;
	private List<QuestionnairePOJO> questionaire;
	private FileUpload fileUpload;

}