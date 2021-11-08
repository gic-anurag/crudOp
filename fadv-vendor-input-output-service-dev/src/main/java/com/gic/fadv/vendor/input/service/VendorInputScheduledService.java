package com.gic.fadv.vendor.input.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public interface VendorInputScheduledService {

	void getVendorRequests();

	List<String> getMrlDocumentNames(ObjectMapper mapper, String componentName, String akaName, String checkId,
			String caseNumber);

}
