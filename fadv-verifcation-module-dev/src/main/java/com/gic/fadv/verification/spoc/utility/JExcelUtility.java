package com.gic.fadv.verification.spoc.utility;

import jxl.*;
import jxl.format.BorderLineStyle;

import java.util.Map;

import com.gic.fadv.verification.spoc.pojo.TemplateHeadersPOJO;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import jxl.read.biff.BiffException;
import java.io.File;
import java.io.IOException;
import jxl.write.*;

public class JExcelUtility {

	public static Map<Integer, List<String>> readJExcel(String fileLocation) throws IOException, BiffException {
		Map<Integer, List<String>> data = new HashMap<>();

		Workbook workbook = Workbook.getWorkbook(new File(fileLocation));
		Sheet sheet = workbook.getSheet(0);
		int rows = sheet.getRows();
		int columns = sheet.getColumns();

		for (int i = 0; i < rows; i++) {
			data.put(i, new ArrayList<String>());
			for (int j = 0; j < columns; j++) {
				data.get(i).add(sheet.getCell(j, i).getContents());
			}
		}
		return data;
	}

	/*
	 * ,String firstName,String lastName, String employeeID,String
	 * entitySpecificId,String attachmentName, String dateOfJoining,String
	 * dateOfExit,String designation,String clientName,String companyName, String
	 * dateOfBirth
	 */
	public static void writeJExcel(List<TemplateHeadersPOJO> th, List<String> excelData)
			throws IOException, WriteException {
		WritableWorkbook workbook = null;
		try {
			File currDir = new File(".");
			String path = currDir.getAbsolutePath();
			String fileLocation = path.substring(0, path.length() - 1) + "tempj.xls";

			workbook = Workbook.createWorkbook(new File(fileLocation));

			WritableSheet sheet = workbook.createSheet("Sheet 1", 0);

			WritableCellFormat headerFormat = new WritableCellFormat();

			/*
			 * WritableFont font = new WritableFont(WritableFont.ARIAL,
			 * 10,WritableFont.BOLD); headerFormat.setFont(font);
			 * headerFormat.setBackground(Colour.LIGHT_BLUE); headerFormat.setWrap(true);
			 */
			// Looping For Header
			for (int i = 0; i < th.size(); i++) {

				Label headerLabel = new Label(i, 0, th.get(i).getHeaderName(), headerFormat);
				sheet.setColumnView(0, 60);
				sheet.addCell(headerLabel);

				/*
				 * headerLabel = new Label(1, 0, th.get(i).getDocumentName(), headerFormat);
				 * sheet.setColumnView(0, 40); sheet.addCell(headerLabel);
				 */
			}
			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setWrap(true);
			for (int i = 0; i < excelData.size(); i++) {
				Label cellLabel = new Label(i, 1, excelData.get(i), cellFormat);
				sheet.addCell(cellLabel);
			}
			/*
			 * Label cellLabel = new Label(0, 1, "John Smith", cellFormat);
			 * sheet.addCell(cellLabel); Number cellNumber = new Number(1, 1, 20,
			 * cellFormat); sheet.addCell(cellNumber);
			 */

			workbook.write();
		} finally {
			if (workbook != null) {
				workbook.close();
			}
		}

	}

	public static void writeJExcelList(List<TemplateHeadersPOJO> th, List<List<String>> excelData, String filePath,
			String xlsFileName) throws IOException, WriteException {
		WritableWorkbook workbook = null;
		try {
			// Create directory for non existed path.
			File newDirectory = new File(filePath);
			boolean isCreated = newDirectory.mkdirs();
			if (isCreated) {
				System.out.printf("1. Successfully created directories, path:%s", newDirectory.getCanonicalPath());
			} else if (newDirectory.exists()) {
				System.out.printf("1. Directory path already exist, path:%s", newDirectory.getCanonicalPath());
			} else {
				System.out.println("1. Unable to create directory");
				return;
			}

			String path = newDirectory.getAbsolutePath();
			// String fileLocation = path.substring(0, path.length() - 1) + "tempj.xls";
			// String fileLocation = path+ "/tempj.xls";
			String fileLocation = path + "/" + xlsFileName + ".xls";
			workbook = Workbook.createWorkbook(new File(fileLocation));

			WritableSheet sheet = workbook.createSheet("Sheet 1", 0);

			WritableCellFormat headerFormat = new WritableCellFormat();
			WritableFont font = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
			headerFormat.setFont(font);
			headerFormat.setBackground(Colour.ICE_BLUE);
			headerFormat.setWrap(true);
			headerFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
			// Looping For Header
			for (int i = 0; i < th.size(); i++) {
				//System.out.println("Header Name"+th.get(i).getHeaderName());
				if(i==0) {
					Label headerLabel = new Label(i, 0, "Serial No.", headerFormat);
					sheet.setColumnView(0, 30);
					//sheet.setRowView(0, 40);
					sheet.addCell(headerLabel);
					//Added Missing Header
					headerLabel = new Label(i+1, 0, th.get(i).getHeaderName(), headerFormat);
					sheet.setColumnView(0, 30);
					//sheet.setRowView(0, 40);
					sheet.addCell(headerLabel);
				}else {
					Label headerLabel = new Label(i+1, 0, th.get(i).getHeaderName(), headerFormat);
					sheet.setColumnView(0, 30);
					//sheet.setRowView(0, 40);
					sheet.addCell(headerLabel);
				}
				

				/*
				 * headerLabel = new Label(1, 0, th.get(i).getDocumentName(), headerFormat);
				 * sheet.setColumnView(0, 40); sheet.addCell(headerLabel);
				 */
			}
			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setFont(font);
			//cellFormat.setBackground(Colour.AQUA);
			//cellFormat.setWrap(true);
			cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
			for (int i = 0; i < excelData.size(); i++) {
				for (int j = 0; j < excelData.get(i).size(); j++) {
					//System.out.println("Value of excel Data"+excelData.get(i).get(j));
					if(j==0) {
						Label cellLabel = new Label(j, i + 1,""+(i+1), cellFormat);
						sheet.addCell(cellLabel);
						sheet.setColumnView(i+1, 30);
						
						cellLabel = new Label(j+1, i + 1, excelData.get(i).get(j), cellFormat);
						sheet.addCell(cellLabel);
						sheet.setColumnView(i+1, 30);
						//sheet.setRowView(j, 40);
					}else {
						Label cellLabel = new Label(j+1, i + 1, excelData.get(i).get(j), cellFormat);
						sheet.addCell(cellLabel);
						sheet.setColumnView(i+1, 30);
						//sheet.setRowView(j, 40);
					}
					
				}

			}
			/*
			 * Label cellLabel = new Label(0, 1, "John Smith", cellFormat);
			 * sheet.addCell(cellLabel); Number cellNumber = new Number(1, 1, 20,
			 * cellFormat); sheet.addCell(cellNumber);
			 */

			workbook.write();
		} finally {
			if (workbook != null) {
				workbook.close();
			}
		}

	}

}
