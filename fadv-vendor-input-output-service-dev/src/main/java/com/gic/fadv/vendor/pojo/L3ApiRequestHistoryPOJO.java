package com.gic.fadv.vendor.pojo;

import java.util.Date;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class L3ApiRequestHistoryPOJO {
	private long l3ApiRequestHistoryId;
	private String caseNumber;
	private String checkId;
	private String requestUrl;
	private JsonNode l3Response;
	private Date createdDate;
	private Date updatedDate;
	private JsonNode l3Request;
	private String requestType;// DE,ComponetCheckData,Verification,PassportVerification etc.
	private String engineStage;// SPOC, Vendor,CBVUTV etc., Event_status_id
	private String responseFlag;// Data valid,Data Invalid, Response Empty,API Not responding,etc.
	private Long eventStatusId; // Take from Event status table
}
