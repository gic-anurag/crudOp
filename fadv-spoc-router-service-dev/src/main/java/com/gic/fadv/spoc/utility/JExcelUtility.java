package com.gic.fadv.spoc.utility;

import jxl.*;
import java.util.Map;

import com.gic.fadv.spoc.pojo.TemplateHeaders;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import jxl.read.biff.BiffException;
import java.io.File;
import java.io.IOException;
import jxl.write.*;
import jxl.write.Number;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;

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
	public static void writeJExcel(List<TemplateHeaders> th, List<String> excelData)
			throws IOException, WriteException {
		WritableWorkbook workbook = null;
		try {
			File currDir = new File(".");
			String path = currDir.getAbsolutePath();
			String fileLocation = path.substring(0, path.length() - 1) + "tempj.xls";

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

				Label headerLabel = new Label(i, 0, th.get(i).getHeaderName(), headerFormat);
				sheet.setColumnView(0, 60);
				sheet.addCell(headerLabel);

				/*
				 * headerLabel = new Label(1, 0, th.get(i).getDocumentName(), headerFormat);
				 * sheet.setColumnView(0, 40); sheet.addCell(headerLabel);
				 */
			}

			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setFont(font);
			cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

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

}
