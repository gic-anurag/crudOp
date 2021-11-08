package com.gic.fadv.spoc.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class SPOCBulkPOJO {
	
		private String akaName;
		private String caseReference;
		private String caseNumber;
		private String checkId;
		private String candidateName;
		private String clientName;
		private String filePath;
		private String flag;
		private Date createdDate;
		private Date updatedDate;
}
