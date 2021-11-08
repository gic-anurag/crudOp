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
public class RouterHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long routerHistoryId;

	private String checkId;
	private String engineName;
	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", name = "engine_request")
	private JsonNode engineRequest;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", name = "engine_response")
	private JsonNode engineResponse;

	private String caseNumber;
	// Should Connect Case Specific Record Details
	private Long caseSpecificRecordDetailId;
	// Should be processed, failed, initiated
	private String currentEngineStatus;
	
	private Date startTime;

	private Date endTime;
}
