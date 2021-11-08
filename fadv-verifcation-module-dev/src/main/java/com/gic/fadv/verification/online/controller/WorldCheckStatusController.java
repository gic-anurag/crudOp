package com.gic.fadv.verification.online.controller;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gic.fadv.verification.online.service.WorldCheckService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class WorldCheckStatusController {

	@Autowired
	private WorldCheckService worldCheckService;

	@PostMapping("/update-worldcheck-status")
	public Map<String, String> saveWorldCheckStatus(@Valid @RequestBody ArrayNode requestArrayNode) {
		return worldCheckService.updateWorldCheckStatus(requestArrayNode);
	}
}
