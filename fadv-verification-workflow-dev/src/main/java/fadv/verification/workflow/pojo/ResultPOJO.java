package fadv.verification.workflow.pojo;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ResultPOJO {
	private CaseReferencePOJO caseReference;
	private List<CheckPOJO> checks;
}
