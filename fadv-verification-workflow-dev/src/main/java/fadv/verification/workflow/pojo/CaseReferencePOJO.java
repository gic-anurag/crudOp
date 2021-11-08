package fadv.verification.workflow.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CaseReferencePOJO {
	private String caseUUID;
	private String clientId;
	private String sbuId;
	private String packageId;
	private String crnNo;
	private String crnCreatedDate;
	private String caseNo;
	private String caseType;
	private String scrnCreatedDate;
}
