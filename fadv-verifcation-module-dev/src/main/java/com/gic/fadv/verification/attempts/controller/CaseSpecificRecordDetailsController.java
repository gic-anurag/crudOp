package com.gic.fadv.verification.attempts.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gic.fadv.verification.attempts.model.AttemptQuestionnaire;
import com.gic.fadv.verification.attempts.model.CaseSpecificRecordDetail;
import com.gic.fadv.verification.attempts.repository.CaseSpecificRecordDetailRepository;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class CaseSpecificRecordDetailsController {

	//	@Autowired
	//	private CaseSpecificInfoRepository caseSpecificInfoRepository;
	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;

	//	private static final Logger logger = LoggerFactory.getLogger(CaseSpecificRecordDetailsController.class);

	@GetMapping(path = "/case-specific-record", produces = "application/json")
	public List<CaseSpecificRecordDetail> getCaseSpecificInfo() {
		return caseSpecificRecordDetailRepository.findAll();
	}

	@PostMapping(path = "/case-specific-record", produces = "application/json")
	public CaseSpecificRecordDetail postCaseSpecificInfo(@Valid @RequestBody CaseSpecificRecordDetail requestBody) {

		CaseSpecificRecordDetail caseSpecificRecordDetailNew = new CaseSpecificRecordDetail();
		if (requestBody != null) {

			List<CaseSpecificRecordDetail> caseSpecificRecordDetailList = caseSpecificRecordDetailRepository
					.findByInstructionCheckId(requestBody.getInstructionCheckId());
			if (!caseSpecificRecordDetailList.isEmpty()) {
				caseSpecificRecordDetailNew
				.setCaseSpecificDetailId(caseSpecificRecordDetailList.get(0).getCaseSpecificDetailId());
				caseSpecificRecordDetailNew
				.setCheckCreatedDate(caseSpecificRecordDetailList.get(0).getCheckCreatedDate());
			} else {
				caseSpecificRecordDetailNew.setCheckCreatedDate(requestBody.getCheckCreatedDate());
			}

			caseSpecificRecordDetailNew.setCaseSpecificId(requestBody.getCaseSpecificId());
			caseSpecificRecordDetailNew.setComponentName(requestBody.getComponentName());
			caseSpecificRecordDetailNew.setProduct(requestBody.getProduct());
			caseSpecificRecordDetailNew.setComponentRecordField(requestBody.getComponentRecordField());
			caseSpecificRecordDetailNew.setInstructionCheckId(requestBody.getInstructionCheckId());
			caseSpecificRecordDetailNew.setCheckStatus(requestBody.getCheckStatus());
			caseSpecificRecordDetailNew.setCheckDueDate(requestBody.getCheckDueDate());
			caseSpecificRecordDetailNew.setCheckTat(requestBody.getCheckTat());
			caseSpecificRecordDetailNew.setUpdatedDate(new Date());
			caseSpecificRecordDetailNew.setCaseSpecificRecordStatus(requestBody.getCaseSpecificRecordStatus());
			caseSpecificRecordDetailRepository.save(caseSpecificRecordDetailNew);
		}

		return caseSpecificRecordDetailNew;
	}
	
	@GetMapping(path={"/case-specific-record/{checkId}"})
	public List<CaseSpecificRecordDetail> getCaseSpecificRecord(@PathVariable(name = "checkId", required = true) String checkId) {
		
		checkId = checkId != null ? checkId : "";
		List<CaseSpecificRecordDetail> caseSpecificRecordDetailByCheck =new ArrayList<>();
		if (StringUtils.isNotEmpty(checkId)) {
			caseSpecificRecordDetailByCheck= caseSpecificRecordDetailRepository.findByInstructionCheckId(checkId);
		}
		return caseSpecificRecordDetailByCheck;
	}
}
