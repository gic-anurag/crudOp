package fadv.verification.workflow.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SPOCEmailConfigPOJO {
	
		@JsonProperty("ORGID")
	    private String oRGID;

	    private String contactCardName;
	    @JsonProperty("mRLDocumentAttachmentFileName")
	    private String mRLDocumentAttachmentFileName;

	    private String toEmailID;

	    private String subjectLine;
	    @JsonProperty("CCEmailId")
	    private String cCEmailId;

	    private String verificationDetailsInMailBodyOrAttachment;

	    private String followUpDate1;
	    private String followUpDate2;
	    private String followUpDate3;
	    private String followUpDate4;
	    private String followUpDate5;

	    private String singleInitiationBulkInitiation;

	    private String initiation;

	    private String expectedClosureDate;

	    private String nameOfTheHRPOC;
	    @JsonProperty("hRDesignation")
	    private String hRDesignation;

	    private String followUpToEmailId2;

	    private String followUpToEmailId1;

	    private String fromEmailID;

	    private String freshORAddingToPreviousFollowUpMail;
	    
	    private String sourceName;
	    
	    public SPOCEmailConfigPOJO() {
			super();
		}
}
