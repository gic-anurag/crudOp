package com.gic.fadv.online.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.online.service.OnlinePreProcessService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/india")
@Api(value = "Cross Reference")
public class OnlineWorkflowController {
	
	@Autowired
	OnlinePreProcessService onlineWorkflowService;

	private static final Logger logger = LoggerFactory.getLogger(OnlineWorkflowController.class);
	
	@ApiOperation(value = "This service is used to process Records at online router and return the result to workflow router ", response = ObjectNode.class)
	@PostMapping(path = "/onlineworkflow", consumes = "application/json", produces = "application/json")
	public ObjectNode doRecordProcess(@RequestBody JsonNode requestBody) {
		logger.info("Got Request :\n {}", requestBody);
		ObjectNode response = onlineWorkflowService.processRequestBody(requestBody);
		logger.info("Record request Processed");
		return response;
	}
}
