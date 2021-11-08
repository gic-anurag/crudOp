package com.gic.fadv.verification.attempts.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.fadv.verification.pojo.CaseSpecificInfoPOJO;

@Service
public interface CaseSpecificRecordDetailService {

	List<CaseSpecificInfoPOJO> getCaseSpecificRecords(ObjectMapper mapper, JsonNode requestBody);

}
