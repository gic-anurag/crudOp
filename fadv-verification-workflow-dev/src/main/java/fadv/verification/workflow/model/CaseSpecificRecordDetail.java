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
public class CaseSpecificRecordDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long caseSpecificDetailId;

	private String instructionCheckId;

	private String componentName;

	private String product;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", name = "component_record_field")
	private String componentRecordField;

	private Date checkCreatedDate;

	private String checkStatus;

	private long caseSpecificId;

	private Date checkDueDate;
	
	private String functionalEntityName;
	
	private String entityLocation;
	
	private String checkTat;
	
	private Date updatedDate = new Date();

	private String caseSpecificRecordStatus;
	
	private String cbvUtvStatus;
	
	private String spocStatus;
	
	private String onlineStatus;
	
	private String wellknownStatus;
	
	private String suspectStatus;
	
	private String vendorStatus;
	
	private String stellarStatus;
	
	private Boolean isCheckManual;
	
	@Column(columnDefinition = "varchar(50) default 'NONE'")
    private String checkAllocationStatus;
	
	private Long userId; 
	
	private String passportStatus;
	
	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", name = "cost_approval_record")
	private JsonNode costApprovalRecord;
}
