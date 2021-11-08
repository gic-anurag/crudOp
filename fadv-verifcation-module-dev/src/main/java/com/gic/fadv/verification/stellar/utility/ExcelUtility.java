package com.gic.fadv.verification.stellar.utility;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gic.fadv.verification.stellar.model.StellarMisReport;

public class ExcelUtility {

	private static String[] stellarReportColumns = { "check id creation date", "Component", "Check ID", "Client Name",
			"CRN", "Candidate Name", "College Name", "University Name", "Qualification", "Major", "Number Type1",
			"Unique1", "Number Type2", "Unique2", "Month & Year", "Year of Graduation", "Class Obtained",
			"Document Sent", "Special Notes / Comments (re-verification / additional cost / any other special note)",
			"Stellar Availability Yes/No", "QR Code" };
	private static final Logger logger = LoggerFactory.getLogger(ExcelUtility.class);

	private ExcelUtility() {
		throw new IllegalStateException("ExcelUtility class");
	}

	public static void createStellarLogReport(String fileLocation, List<StellarMisReport> stellarMisReports) {

		try (Workbook workbook = new XSSFWorkbook()) {
			createStellarMisLogSheet(stellarMisReports, workbook, "Stellar Mis Log");

			// Write the output to a file
			FileOutputStream fileOut = new FileOutputStream(fileLocation);
			workbook.write(fileOut);
			fileOut.close();
		} catch (IOException e) {
			logger.error("Exception while creating excel file : {}", e.getMessage());
			e.printStackTrace();
		}
	}

	private static void createStellarMisLogSheet(List<StellarMisReport> stellarMisReports, Workbook workbook,
			String sheetName) {
		Sheet sheet = workbook.createSheet(sheetName);

		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 11);
		headerFont.setColor(IndexedColors.BLACK.getIndex());

		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);

		// Create a Row
		Row headerRow = sheet.createRow(0);

		for (int i = 0; i < stellarReportColumns.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(stellarReportColumns[i]);
			cell.setCellStyle(headerCellStyle);
		}

		// Create Other rows and cells with contacts data
		int rowNum = 1;

		for (StellarMisReport stellarMisReport : stellarMisReports) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(stellarMisReport.getCheckIdCreationDate());
			row.createCell(1).setCellValue(stellarMisReport.getComponentName());
			row.createCell(2).setCellValue(stellarMisReport.getCheckId());
			row.createCell(3).setCellValue(stellarMisReport.getClientName());
			row.createCell(4).setCellValue(stellarMisReport.getCrnNo());
			row.createCell(5).setCellValue(stellarMisReport.getCandidateName());
			row.createCell(6).setCellValue(stellarMisReport.getCollegeName());
			row.createCell(7).setCellValue(stellarMisReport.getUniversityName());
			row.createCell(8).setCellValue(stellarMisReport.getQualification());
			row.createCell(9).setCellValue(stellarMisReport.getMajor());
			row.createCell(10).setCellValue(stellarMisReport.getNumberType1());
			row.createCell(11).setCellValue(stellarMisReport.getUnique1());
			row.createCell(12).setCellValue(stellarMisReport.getNumberType2());
			row.createCell(13).setCellValue(stellarMisReport.getUnique2());
			row.createCell(14).setCellValue(stellarMisReport.getMonthYear());
			row.createCell(15).setCellValue(stellarMisReport.getYearOfGrad());
			row.createCell(16).setCellValue(stellarMisReport.getClassObtained());
			row.createCell(17).setCellValue(stellarMisReport.getDocumentSent());
			row.createCell(18).setCellValue(stellarMisReport.getSpecialNotes());
			row.createCell(19).setCellValue(stellarMisReport.getStellarAvailability());
			row.createCell(20).setCellValue(stellarMisReport.getQrCode());
		}
	}
}
