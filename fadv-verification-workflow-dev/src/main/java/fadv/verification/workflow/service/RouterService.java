package fadv.verification.workflow.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;

import fadv.verification.workflow.model.CaseSpecificInfo;
import fadv.verification.workflow.model.CaseSpecificRecordDetail;

@Service
public interface RouterService {

	void processCaseRecordDetails(List<CaseSpecificRecordDetail> caseSpecificRecordDetails,
			CaseSpecificInfo caseSpecificInfo, ObjectNode otherDetails);

}
