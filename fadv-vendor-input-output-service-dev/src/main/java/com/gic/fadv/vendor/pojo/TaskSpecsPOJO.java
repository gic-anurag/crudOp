package com.gic.fadv.vendor.pojo;

import java.util.List;

import lombok.Data;

@Data
public class TaskSpecsPOJO
{
    private L3CaseReferencePOJO caseReference;

    private CheckVerificationPOJO checkVerification;
    
	List<QuestionnairePOJO> questionaire;

    private FileUploadPOJO fileUpload;

}