package com.gic.fadv.spoc.pojo;

import java.util.List;

import lombok.Data;

@Data
public class TaskSpecsPOJO
{
    private CaseReferencePOJO caseReference;

    private CheckVerificationPOJO checkVerification;
    
	List<QuestionnairePOJO> questionaire;

    private FileUploadPOJO fileUpload;

}