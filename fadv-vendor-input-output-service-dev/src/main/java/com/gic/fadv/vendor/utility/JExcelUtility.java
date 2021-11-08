package com.gic.fadv.vendor.utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class JExcelUtility {

	@Value("${local.file.location}")
	private static String localFileLocation;

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

	public static void writeJExcel(String fileLocation, List<Map<String, Object>> recordMapResultList)
			throws IOException, WriteException {
		WritableWorkbook workbook = null;
		try {
			/*
			 * File currDir = new File("."); String path = currDir.getAbsolutePath(); String
			 * fileLocation = path.substring(0, path.length() - 1) +
			 * "Address_Input_Mapping.xls";
			 * System.out.println("File Location"+localFileLocation); String fileLocation =
			 * localFileLocation+"Address_Input_Mapping.xls";
			 */

			workbook = Workbook.createWorkbook(new File(fileLocation));

			WritableSheet sheet = workbook.createSheet("Sheet 1", 0);

			WritableCellFormat headerFormat = new WritableCellFormat();
			WritableFont font = new WritableFont(WritableFont.ARIAL, 16, WritableFont.BOLD);
			headerFormat.setFont(font);
			headerFormat.setBackground(Colour.LIGHT_BLUE);
			headerFormat.setWrap(true);
			Label headerLabel = null;

			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setWrap(true);

			Label cellLabel = null;
			for (int i = 0; i < recordMapResultList.size(); i++) {
				Map<String, Object> recordMapResult = recordMapResultList.get(i);
				if (i == 0) {
					/* Generate Header of Excel */
					int j = 0;
					for (Map.Entry<String, Object> entry : recordMapResult.entrySet()) {
						System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
						headerLabel = new Label(j, 0, entry.getKey(), headerFormat);
						sheet.addCell(headerLabel);
						j++;
					}
					/* Generate Body of Excel */
					j = 0;
					for (Map.Entry<String, Object> entry : recordMapResult.entrySet()) {
						System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
						cellLabel = new Label(j, i + 1, entry.getValue().toString(), cellFormat);
						sheet.addCell(cellLabel);
						j++;
					}
				} else {
					/* Generate Body of Excel */
					int j = 0;
					for (Map.Entry<String, Object> entry : recordMapResult.entrySet()) {
						System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
						cellLabel = new Label(j, i + 1, entry.getValue().toString(), cellFormat);
						sheet.addCell(cellLabel);
						j++;
					}
				}
			}
			/*
			 * Label headerLabel = new Label(0, 0, "Name", headerFormat);
			 * sheet.setColumnView(0, 60); sheet.addCell(headerLabel); headerLabel = new
			 * Label(1, 0, "Age", headerFormat); sheet.setColumnView(0, 40);
			 * sheet.addCell(headerLabel);
			 */

			/*
			 * WritableCellFormat cellFormat = new WritableCellFormat();
			 * cellFormat.setWrap(true);
			 * 
			 * Label cellLabel = new Label(0, 2, "John Smith", cellFormat);
			 * sheet.addCell(cellLabel); Number cellNumber = new Number(1, 2, 20,
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
