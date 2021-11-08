package com.gic.fadv.verification.spoc.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public interface SPOCBulkService {

	String processSPOCBulk(List<String> checkIdList) throws JsonProcessingException;
	

}
