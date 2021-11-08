package fadv.verification.workflow.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import lombok.Data;

@Entity
@Data
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class L3ApiRequestHistory {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long l3ApiRequestHistoryId;
	
	private String caseNumber;
	
	private String checkId;
	
	private String requestUrl;
	
	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", name = "l3_response")
	private JsonNode l3Response;
	
	private Date createdDate;
	
	private Date updatedDate;
	
	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", name = "l3_request")
	private JsonNode l3Rquest;
	
	private String requestType;//DE,ComponetCheckData,Verification,PassportVerification etc.
	private String engineStage;//SPOC, Vendor,CBVUTV etc., Event_status_id
	private String responseFlag;//Data valid,Data Invalid, Response Empty,API Not responding,etc.
	private Long eventStatusId; //Take from Event status table
}
