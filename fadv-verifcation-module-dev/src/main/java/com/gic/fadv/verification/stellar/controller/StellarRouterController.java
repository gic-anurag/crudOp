package com.gic.fadv.verification.stellar.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gic.fadv.verification.stellar.pojo.StellarReportPOJO;
import com.gic.fadv.verification.stellar.service.StellarRouterService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class StellarRouterController {

	@Autowired
	private StellarRouterService stellarRouterService;

	@PostMapping(path = "/save-stellar-report", consumes = "application/json")
	public ResponseEntity<String> createStellarLog(@Valid @RequestBody StellarReportPOJO stellarReportPOJO) {
		return stellarRouterService.createStellarMisLog(stellarReportPOJO);
	}

	@GetMapping("/get-stellar-report/{fromDateStr}/{toDateStr}")
	public ResponseEntity<String> getSpocLogReport(@PathVariable(value = "fromDateStr") String fromDateStr,
			@PathVariable(value = "toDateStr") String toDateStr, HttpServletResponse response) {
		return stellarRouterService.getStellarLogReport(fromDateStr, toDateStr, response);
	}
}
