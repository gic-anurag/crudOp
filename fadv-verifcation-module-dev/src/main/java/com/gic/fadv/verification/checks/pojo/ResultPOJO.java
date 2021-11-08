package com.gic.fadv.verification.checks.pojo;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ResultPOJO {
	public CaseReferencePOJO caseReference;
	public List<CheckPOJO> checks;
}
