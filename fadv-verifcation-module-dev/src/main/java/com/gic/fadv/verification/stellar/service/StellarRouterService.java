package com.gic.fadv.verification.stellar.service;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.gic.fadv.verification.stellar.pojo.StellarReportPOJO;

@Service
public interface StellarRouterService {

	ResponseEntity<String> createStellarMisLog(StellarReportPOJO stellarReportPOJO);

	ResponseEntity<String> getStellarLogReport(String fromDateStr, String toDateStr, HttpServletResponse response);

}
