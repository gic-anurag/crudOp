package com.gic.fadv.verification.spoc.pojo;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SPOCBulkPOJO {
	
		private long id;
		private String akaName;
		private String caseReference;
		private String caseNumber;
		private String checkId;
		private String candidateName;
		private String clientName;
		private String flag;
		private Date createdDate;
		private Date updatedDate;
}
