package com.gic.fadv.verification.stellar.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.fadv.verification.stellar.model.StellarMisReport;
import com.gic.fadv.verification.stellar.pojo.StellarReportPOJO;
import com.gic.fadv.verification.stellar.repository.StellarMisReportRepository;
import com.gic.fadv.verification.stellar.utility.ExcelUtility;

@Service
public class StellarRouterServiceImpl implements StellarRouterService {

	@Autowired
	private StellarMisReportRepository stellarMisReportRepository;

	ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Value("${excel.dir}")
	private String fileLocation;

	private static final Logger logger = LoggerFactory.getLogger(StellarRouterServiceImpl.class);

	@Override
	public ResponseEntity<String> createStellarMisLog(StellarReportPOJO stellarReportPOJO) {
		StellarMisReport stellarMisReport = mapper.convertValue(stellarReportPOJO, StellarMisReport.class);

		stellarMisReport.setCreatedDate(new Date());
		stellarMisReport.setUpdatedDate(new Date());

		stellarMisReportRepository.save(stellarMisReport);

		return new ResponseEntity<>("Record saved successfully", HttpStatus.OK);

	}

	@Override
	public ResponseEntity<String> getStellarLogReport(String fromDateStr, String toDateStr,
			HttpServletResponse response) {

		String message = "";
		if (StringUtils.isNotEmpty(fromDateStr) && StringUtils.isNotEmpty(toDateStr)) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			try {
				simpleDateFormat.parse(fromDateStr);
				simpleDateFormat.parse(toDateStr);

				List<StellarMisReport> stellarMisReports = stellarMisReportRepository
						.getStellarReportByDate(fromDateStr, toDateStr);
				logger.info("Size of stellarMisReports : {}", stellarMisReports.size());

				String filePath = fileLocation + "Stellar_Mis_Logs.xlsx";
				ExcelUtility.createStellarLogReport(filePath, stellarMisReports);
				return generateLogFileForResponse(response, filePath);

			} catch (ParseException e) {
				message = "Date should be in correct format : yyyy-MM-dd";
			}
		} else {
			message = "Both from date and to date is required";
		}
		if (StringUtils.isEmpty(message)) {
			message = "Unable to fetch logs";
		}
		return ResponseEntity.ok().body(message);
	}

	private ResponseEntity<String> generateLogFileForResponse(HttpServletResponse response, String filePath) {
		File fileNameObject = new File(filePath);
		logger.info(fileNameObject.getName());

		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=misReportLog.xlsx");

		try (BufferedOutputStream bfos = new BufferedOutputStream(response.getOutputStream());
				FileInputStream fs = new FileInputStream(fileNameObject)) {

			byte[] buffer = new byte[fs.available()];
			fs.read(buffer);

			bfos.write(buffer, 0, buffer.length);
			bfos.flush();

			Files.delete(fileNameObject.toPath());
			logger.error("File deleted successfully : {}", fileNameObject.getAbsolutePath());
		} catch (IOException e) {
			logger.error("Exception occurred in file operation : {}", e.getMessage());
		}

		return ResponseEntity.ok().body("Download excel created");
	}
}
