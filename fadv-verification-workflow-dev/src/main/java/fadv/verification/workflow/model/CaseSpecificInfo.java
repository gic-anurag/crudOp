package fadv.verification.workflow.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;

@Entity
@Data
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class CaseSpecificInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long caseSpecificId;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", name = "case_reference")
	private String caseReference;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", name = "case_more_info")
	private String caseMoreInfo;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", name = "case_details")
	private String caseDetails;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", name = "client_specific_fields")
	private String clientSpecificFields;

	private String caseNumber;

	private String caseRefNumber;

	private String clientCode;

	private String clientName;

	private String sbuName;

	private String packageName;

	private String candidateName;
	
	private String crnCreationDate;

	private Date createdDate = new Date();

	private Date updatedDate = new Date();

	private String status;
	
	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb", name = "data_entry_info")
	private String dataEntryInfo; 
}
