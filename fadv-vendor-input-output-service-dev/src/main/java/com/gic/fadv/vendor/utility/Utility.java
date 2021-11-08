package com.gic.fadv.vendor.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.vendor.input.pojo.AddressInputHeaderPOJO;
import com.gic.fadv.vendor.input.pojo.BankStatementInputHeaderPOJO;
import com.gic.fadv.vendor.input.pojo.CriminalInputHeaderPOJO;
import com.gic.fadv.vendor.input.pojo.EducationInputHeaderPOJO;
import com.gic.fadv.vendor.input.pojo.Form16InputHeaderPOJO;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Utility {

	public static Date addDaysSkippingWeekends(Date dateToConvert, int days, List<String> holidaysString) {

		LocalDate localDate = dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		List<LocalDate> holidays = new ArrayList<>();
		try {
			holidays = convertDate(holidaysString);
		} catch (Exception e) {
			 holidays = new ArrayList<>();
		}
		
		int addedDays = 0;
		while (addedDays < days) {
			localDate = localDate.plusDays(1);
			if (!(localDate.getDayOfWeek() == DayOfWeek.SATURDAY || localDate.getDayOfWeek() == DayOfWeek.SUNDAY
					|| holidays.contains(localDate))) {
				++addedDays;
			}
		}
		return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}
	
	public static List<LocalDate> convertDate(List<String> dateInput) {
		List<LocalDate> dateOutput = new ArrayList<>();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		dateInput.forEach(dateStr -> {

			dateTimeFormatter.parse(dateStr);

			Date date;
			try {
				date = simpleDateFormat.parse(dateStr);
				dateOutput.add(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
			} catch (DateTimeParseException | ParseException e) {
				e.printStackTrace();
			}

		});
		return dateOutput;
	}

	public static Map<Integer, List<String>> readExcel(String fileLocation) throws IOException {

		Map<Integer, List<String>> data = new HashMap<>();
		FileInputStream file = new FileInputStream(new File(fileLocation));
		Workbook workbook = new XSSFWorkbook(file);
		Sheet sheet = workbook.getSheetAt(0);
		int i = 0;
		for (Row row : sheet) {
			data.put(i, new ArrayList<String>());
			for (Cell cell : row) {
				switch (cell.getCellTypeEnum()) {
				case STRING:
					data.get(i).add(cell.getRichStringCellValue().getString());
					break;
				case NUMERIC:
					if (DateUtil.isCellDateFormatted(cell)) {
						data.get(i).add(cell.getDateCellValue() + "");
					} else {
						data.get(i).add((int) cell.getNumericCellValue() + "");
					}
					break;
				case BOOLEAN:
					data.get(i).add(cell.getBooleanCellValue() + "");
					break;
				case FORMULA:
					data.get(i).add(cell.getCellFormula() + "");
					break;
				default:
					data.get(i).add(" ");
				}
			}
			i++;
		}
		if (workbook != null) {
			workbook.close();
		}
		return data;
	}

	

	public static List<ObjectNode> getExcelDataAsJsonObject(String fileLocation) throws IOException {

		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		Workbook workbook = new XSSFWorkbook(new FileInputStream(new File(fileLocation)));

		List<ObjectNode> sheetArray = new ArrayList<>();
		ArrayList<String> columnNames = new ArrayList<>();
		Sheet sheet = workbook.getSheetAt(0);
		Iterator<Row> sheetIterator = sheet.iterator();
		while (sheetIterator.hasNext()) {
			Row currentRow = sheetIterator.next();
			ObjectNode jsonObject = mapper.createObjectNode();
			if (currentRow.getRowNum() != 0) {
				for (int j = 0; j < columnNames.size(); j++) {
					if (currentRow.getCell(j) != null) {
						if (currentRow.getCell(j).getCellType() == CellType.STRING) {
							jsonObject.put(columnNames.get(j), currentRow.getCell(j).getStringCellValue());
						} else if (currentRow.getCell(j).getCellType() == CellType.NUMERIC) {
							if (DateUtil.isCellDateFormatted(currentRow.getCell(j))) {
								jsonObject.put(columnNames.get(j), currentRow.getCell(j).getDateCellValue() + "");
							} else {
								jsonObject.put(columnNames.get(j),
										(int) currentRow.getCell(j).getNumericCellValue() + "");
							}
						} else if (currentRow.getCell(j).getCellType() == CellType.BOOLEAN) {
							jsonObject.put(columnNames.get(j), currentRow.getCell(j).getBooleanCellValue());
						} else if (currentRow.getCell(j).getCellType() == CellType.BLANK) {
							jsonObject.put(columnNames.get(j), "");
						} else if (currentRow.getCell(j).getCellType() == CellType.FORMULA) {
							jsonObject.put(columnNames.get(j), currentRow.getCell(j).getCellFormula() + "");
						}
					} else {
						jsonObject.put(columnNames.get(j), "");
					}
				}
				sheetArray.add(jsonObject);
			} else {
				// store column names
				for (int k = 0; k < currentRow.getPhysicalNumberOfCells(); k++) {
					columnNames.add(currentRow.getCell(k).getStringCellValue());
				}
			}
		}

		return sheetArray;

	}

	public static void writeCriminalExcel(String fileLocation, List<CriminalInputHeaderPOJO> excelDataList)
			throws IOException {
		Workbook workbook = new XSSFWorkbook();

		try {
			Sheet sheet = workbook.createSheet("Sheet 1");
			sheet.setColumnWidth(0, 6000);
			sheet.setColumnWidth(1, 4000);

			Row header = sheet.createRow(0);

			CellStyle headerStyle = workbook.createCellStyle();

			headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFFont font = ((XSSFWorkbook) workbook).createFont();
			font.setFontName("Arial");
			font.setFontHeightInPoints((short) 16);
			font.setBold(true);
			headerStyle.setFont(font);
			CellStyle style = workbook.createCellStyle();
			style.setWrapText(true);

			Cell headerCell = null;
			Cell cell = null;
			Row row = null;
			for (int i = 0; i < excelDataList.size(); i++) {
				if (i == 0) {
					/* Generate Header of Excel */
					int j = 0;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getInsID());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getProcess());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCSPIStatus());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getClientName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCode());
					j++;// Client Code
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCaseRefNumber());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCRMName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCandidateCompleteName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getDateofBirth());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCompleteAddress());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getFathersName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getPinCode());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCity());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getState());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getLDD());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCheckDate());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCheckDueDate());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getBucket());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getVA());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getInitiationDate());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getInitiatorName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getStatus());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getRemarks());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getSingleDouble());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getClientCategory());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getBatch());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getDECompletion());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getClientIndustry());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCCSManager());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCheckTat());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCDEFADV());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getFirstLevelDEStatus());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getPOSFrom());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getPOSTO());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getFadvCDE());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getEntity());
					j++;
				} else {
					/* Generate Body of Excel */
					int j = 0;
					row = sheet.createRow(i);
					/*
					 * cell = row.createCell(j); cell.setCellValue(entry.getValue().toString());
					 * cell.setCellStyle(style);
					 */
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getInsID());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getProcess());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCSPIStatus());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getClientName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCode());
					cell.setCellStyle(style);
					j++;// Client Code
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCaseRefNumber());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCRMName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCandidateCompleteName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getDateofBirth());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCompleteAddress());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getFathersName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getPinCode());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCity());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getState());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getLDD());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCheckDate());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCheckDueDate());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getBucket());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getVA());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getInitiationDate());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getInitiatorName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getStatus());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getRemarks());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getSingleDouble());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getClientCategory());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getBatch());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getDECompletion());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getClientIndustry());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCCSManager());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCheckTat());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCDEFADV());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getFirstLevelDEStatus());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getPOSFrom());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getPOSTO());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getFadvCDE());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getEntity());
					cell.setCellStyle(style);
					j++;
				}
			}

			FileOutputStream outputStream = new FileOutputStream(fileLocation);
			workbook.write(outputStream);
		} finally {
			if (workbook != null) {

				workbook.close();

			}
		}
	}

	public static void writeAddressExcel(String fileLocation, List<AddressInputHeaderPOJO> excelDataList)
			throws IOException {
		Workbook workbook = new XSSFWorkbook();

		try {
			Sheet sheet = workbook.createSheet("Sheet 1");
			sheet.setColumnWidth(0, 6000);
			sheet.setColumnWidth(1, 4000);

			Row header = sheet.createRow(0);

			CellStyle headerStyle = workbook.createCellStyle();

			headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFFont font = ((XSSFWorkbook) workbook).createFont();
			font.setFontName("Arial");
			font.setFontHeightInPoints((short) 16);
			font.setBold(true);
			headerStyle.setFont(font);
			CellStyle style = workbook.createCellStyle();
			style.setWrapText(true);

			Cell headerCell = null;
			Cell cell = null;
			Row row = null;
			for (int i = 0; i < excelDataList.size(); i++) {
				if (i == 0) {
					/* Generate Header of Excel */
					int j = 0;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getComponent());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCheckID());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCaseReferenceNumber());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getClientname());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCandidateName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getAddress());
					j++;// Client Code
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getFathersName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getDateFrom());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getDateTo());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCandidatesContactNos());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getOrgNameInBVF());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCheckTAT());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCheckInternalStatus());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getOpenFor());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getWorkableWIPChecks());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getClientCode());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getDummy());
				} else {
					/* Generate Body of Excel */
					int j = 0;
					row = sheet.createRow(i);
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getComponent());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCheckID());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCaseReferenceNumber());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getClientname());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCandidateName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getAddress());
					cell.setCellStyle(style);
					j++;// Client Code
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getFathersName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getDateFrom());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getDateTo());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCandidatesContactNos());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getOrgNameInBVF());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCheckTAT());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCheckInternalStatus());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getOpenFor());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getWorkableWIPChecks());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getClientCode());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getDummy());
					cell.setCellStyle(style);
				}
			}

			FileOutputStream outputStream = new FileOutputStream(fileLocation);
			workbook.write(outputStream);
		} finally {
			if (workbook != null) {

				workbook.close();

			}
		}
	}

	public static void writeEducationExcel(String fileLocation, List<EducationInputHeaderPOJO> excelDataList)
			throws IOException {
		Workbook workbook = new XSSFWorkbook();

		try {
			Sheet sheet = workbook.createSheet("Sheet 1");
			sheet.setColumnWidth(0, 6000);
			sheet.setColumnWidth(1, 4000);

			Row header = sheet.createRow(0);

			CellStyle headerStyle = workbook.createCellStyle();

			headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFFont font = ((XSSFWorkbook) workbook).createFont();
			font.setFontName("Arial");
			font.setFontHeightInPoints((short) 16);
			font.setBold(true);
			headerStyle.setFont(font);
			CellStyle style = workbook.createCellStyle();
			style.setWrapText(true);

			Cell headerCell = null;
			Cell cell = null;
			Row row = null;
			for (int i = 0; i < excelDataList.size(); i++) {
				if (i == 0) {
					/* Generate Header of Excel */
					int j = 0;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getDateOfInitiation());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getComponent());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCheckID());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getClientName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCaseNumber());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCandidateName());
					j++;
					// Client Code
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCollegeName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getUniversityName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getQualification());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getMajor());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getNumbertype1());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getUniqueno1());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getNumbertype2());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getUniqueno2());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getYearofPassing());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getYearofGraduation());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getClassObtained());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getDocumentsSent());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCheckSubdate());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCheckDuedate());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getInitiatiorsName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getSpecialNotesComments());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getWrittenmandateClient());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getStellarSent());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getStatus());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getSubStatus());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getDocumentAsPerMRL());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getLocation());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getVendor());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getTLName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getPOC());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getDVCompletionTime());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getLDD());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCheckInternalStatus());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getQueueName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getNoOfChecks());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getBTDTWT());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getLHVerificationCost());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getTopClients());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getApprovedRejected());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getRemarks());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getBatchSlot());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getQRCode());
				} else {
					/* Generate Body of Excel */
					int j = 0;
					row = sheet.createRow(i);
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getDateOfInitiation());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getComponent());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCheckID());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getClientName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCaseNumber());
					cell.setCellStyle(style);
					j++;// Client Code
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCandidateName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCollegeName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getUniversityName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getQualification());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getMajor());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getNumbertype1());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getUniqueno1());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getNumbertype2());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getUniqueno2());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getYearofPassing());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getYearofGraduation());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getClassObtained());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getDocumentsSent());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCheckSubdate());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCheckDuedate());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getInitiatiorsName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getSpecialNotesComments());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getWrittenmandateClient());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getStellarSent());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getStatus());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getSubStatus());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getDocumentAsPerMRL());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getLocation());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getVendor());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getTLName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getPOC());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getDVCompletionTime());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getLDD());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCheckInternalStatus());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getQueueName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getNoOfChecks());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getBTDTWT());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getLHVerificationCost());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getTopClients());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getApprovedRejected());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getRemarks());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getBatchSlot());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getQRCode());
					cell.setCellStyle(style);
				}
			}

			FileOutputStream outputStream = new FileOutputStream(fileLocation);
			workbook.write(outputStream);
		} finally {
			if (workbook != null) {

				workbook.close();

			}
		}
	}

	public static void writeBankStatementExcel(String fileLocation, List<BankStatementInputHeaderPOJO> excelDataList)
			throws IOException {
		Workbook workbook = new XSSFWorkbook();

		try {
			Sheet sheet = workbook.createSheet("Sheet 1");
			sheet.setColumnWidth(0, 6000);
			sheet.setColumnWidth(1, 4000);

			Row header = sheet.createRow(0);

			CellStyle headerStyle = workbook.createCellStyle();

			headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFFont font = ((XSSFWorkbook) workbook).createFont();
			font.setFontName("Arial");
			font.setFontHeightInPoints((short) 16);
			font.setBold(true);
			headerStyle.setFont(font);
			CellStyle style = workbook.createCellStyle();
			style.setWrapText(true);

			Cell headerCell = null;
			Cell cell = null;
			Row row = null;
			for (int i = 0; i < excelDataList.size(); i++) {
				if (i == 0) {
					/* Generate Header of Excel */
					int j = 0;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getInsID());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getProcess());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCSPIStatus());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getClientName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCode());
					j++;// Client Code
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCaseRefNumber());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCRMName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCandidateCompleteName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getDateofBirth());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getAccountNumber());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getBankName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getPOSFrom());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getPOSTO());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCity());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getState());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getInitiationDate());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getInitiationBy());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getInitiationStatus());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getInitiationRemarks());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getSingleDouble());
					j++;
				} else {
					/* Generate Body of Excel */
					int j = 0;
					row = sheet.createRow(i);
					/*
					 * cell = row.createCell(j); cell.setCellValue(entry.getValue().toString());
					 * cell.setCellStyle(style);
					 */
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getInsID());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getProcess());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCSPIStatus());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getClientName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCode());
					cell.setCellStyle(style);
					j++;// Client Code
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCaseRefNumber());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCRMName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCandidateCompleteName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getDateofBirth());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getAccountNumber());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getBankName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getPOSFrom());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getPOSTO());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCity());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getState());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getInitiationDate());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getInitiationBy());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getInitiationStatus());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getInitiationRemarks());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getSingleDouble());
					cell.setCellStyle(style);
					j++;
				}
			}

			FileOutputStream outputStream = new FileOutputStream(fileLocation);
			workbook.write(outputStream);
		} finally {
			if (workbook != null) {

				workbook.close();

			}
		}
	}

	public static void writeForm16Excel(String fileLocation, List<Form16InputHeaderPOJO> excelDataList)
			throws IOException {
		Workbook workbook = new XSSFWorkbook();

		try {
			Sheet sheet = workbook.createSheet("Sheet 1");
			sheet.setColumnWidth(0, 6000);
			sheet.setColumnWidth(1, 4000);

			Row header = sheet.createRow(0);

			CellStyle headerStyle = workbook.createCellStyle();

			headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFFont font = ((XSSFWorkbook) workbook).createFont();
			font.setFontName("Arial");
			font.setFontHeightInPoints((short) 16);
			font.setBold(true);
			headerStyle.setFont(font);
			CellStyle style = workbook.createCellStyle();
			style.setWrapText(true);

			Cell headerCell = null;
			Cell cell = null;
			Row row = null;
			for (int i = 0; i < excelDataList.size(); i++) {
				if (i == 0) {
					/* Generate Header of Excel */
					int j = 0;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getInsID());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getClientName());
					j++;// Client Code
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCaseRefNumber());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCandidateCompleteName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getPanNoOfEmployee());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCompleteAddress());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getPanNoOfEmployer());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getTanNoOfEmployer());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getAssesmentYear());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getCity());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getState());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getTDSCertificateNumber());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getInitiatorName());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getStatus());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getRemarksForInsufficiencyWIP());
					j++;
					headerCell = header.createCell(j);
					headerCell.setCellValue(excelDataList.get(i).getInitiationDate());
					j++;
				} else {
					/* Generate Body of Excel */
					int j = 0;
					row = sheet.createRow(i);
					/*
					 * cell = row.createCell(j); cell.setCellValue(entry.getValue().toString());
					 * cell.setCellStyle(style);
					 */
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getInsID());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getClientName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCaseRefNumber());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCandidateCompleteName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getPanNoOfEmployee());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCompleteAddress());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getPanNoOfEmployer());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getTanNoOfEmployer());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getAssesmentYear());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getCity());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getState());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getTDSCertificateNumber());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getInitiatorName());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getStatus());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getRemarksForInsufficiencyWIP());
					cell.setCellStyle(style);
					j++;
					cell = row.createCell(j);
					cell.setCellValue(excelDataList.get(i).getInitiationDate());
					cell.setCellStyle(style);
					j++;
				}
			}

			FileOutputStream outputStream = new FileOutputStream(fileLocation);
			workbook.write(outputStream);
		} finally {
			if (workbook != null) {

				workbook.close();

			}
		}
	}

	public static Boolean checkKeyContains(JsonNode jsonNode, String key) {
		if (jsonNode.has(key) && jsonNode.get(key) != null) {
			return true;
		}
		return false;
	}

	public static String formatDateUtil(String dateStr) {

		SimpleDateFormat formatter1 = new SimpleDateFormat("MMMM-dd-yyyy");
		SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");

		try {
			if (dateStr != null && StringUtils.isNotBlank(dateStr)) {
				Date checkDate = formatter2.parse(dateStr);
				return formatter1.format(checkDate);
			}
		} catch (ParseException e) {
			return dateStr;
		}
		return dateStr;
	}

	public static String formatDateUtil2(String dateStr) {

		SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy");
		SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");

		try {
			if (dateStr != null && StringUtils.isNotBlank(dateStr)) {
				Date checkDate = formatter2.parse(dateStr);
				return formatter1.format(checkDate);
			}
		} catch (ParseException e) {
			return dateStr;
		}
		return dateStr;
	}

	public static String formatDateMilliSec(String dateStr) {
		SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy");
		SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

		try {
			if (dateStr != null && StringUtils.isNotBlank(dateStr)) {
				Date checkDate = formatter2.parse(dateStr);
				return formatter1.format(checkDate);
			}
		} catch (ParseException e) {
			return dateStr;
		}
		return dateStr;
	}

	public static String formatDateQuestions(String dateStr, String question, String questionId) {

		SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy");
		SimpleDateFormat formatter2 = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
		SimpleDateFormat formatter4 = new SimpleDateFormat("HH:mm");
		SimpleDateFormat formatter5 = new SimpleDateFormat("dd MMMM yyyy");

		try {
			if (dateStr != null && StringUtils.isNotBlank(dateStr)) {
				Date checkDate = formatter2.parse(dateStr);
				if (StringUtils.equalsIgnoreCase(question, "Verified time")) {
					return formatter4.format(checkDate);
				} 
				if (StringUtils.equalsIgnoreCase(questionId, "801717") || StringUtils.equalsIgnoreCase(questionId, "806879")) {
					return formatter5.format(checkDate);
				}
				return formatter1.format(checkDate);
			}
		} catch (ParseException e) {
			if (StringUtils.equalsIgnoreCase(questionId, "801717") || StringUtils.equalsIgnoreCase(questionId, "806879")) {
				formatter2 = new SimpleDateFormat("dd-MMM-yyyy");
				Date checkDate;
				try {
					checkDate = formatter2.parse(dateStr);
					return formatter5.format(checkDate);
				} catch (ParseException e1) {
					return dateStr;
				}
			}
			return dateStr;
		}

		return dateStr;
	}
	
	public static String formatString(String inputStr) {

		if (inputStr == null || StringUtils.isEmpty(inputStr)) {
			return inputStr;
		}

		Map<String, String> patternMap = new HashMap<>();
		patternMap.put("#dot#", ".");
		patternMap.put("#DOT#", ".");
		patternMap.put("#Dot#", ".");

		for (Map.Entry<String, String> pattern : patternMap.entrySet()) {
			inputStr = StringUtils.replace(inputStr, pattern.getKey(), pattern.getValue());
		}
		// strips off all non-ASCII characters
		inputStr = inputStr.replaceAll("[^\\x00-\\x7F]", "");

		// erases all the ASCII control characters
//		inputStr = inputStr.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

		// removes non-printable characters from Unicode
		inputStr = inputStr.replaceAll("\\p{C}", "");

		return inputStr.trim();
	}
	
//	public static void main(String[] args) {
//
//		System.out.println(formatDateQuestions("Wed Jan 01 00:00:00 IST 2020", "Verified date"));
//	}
}
