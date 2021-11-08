package com.gic.fadv.verification.word.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.hssf.usermodel.HeaderFooter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.IBody;
import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFAbstractNum;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFNumbering;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAbstractNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLvl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STNumberFormat;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gic.fadv.verification.word.pojo.WordFileInputPOJO;

@Service
public class WordDocumentServiceImpl implements WordDocumentService {
	private static String logo = "logo-fadv.png";
	private static String outputFile = "letter.docx";
	private static String fontFamily = "Times New Roman";
	private static String colorWhite = "000000";
	@Value("${local.file.location.logo}")
	private String fadvLogo;
	
	@Override
	public String handleSimpleDoc(WordFileInputPOJO wordFileInputPOJO)
			throws InvalidFormatException, IOException, URISyntaxException {

		String checkId = wordFileInputPOJO.getCheckId() != null ? wordFileInputPOJO.getCheckId() : "";
		String institutionName = wordFileInputPOJO.getInstitutionName() != null ? wordFileInputPOJO.getInstitutionName()
				: "";
		String state = wordFileInputPOJO.getState() != null ? wordFileInputPOJO.getState() : "";
		String vendorBillingAddress = wordFileInputPOJO.getVendorBillingAddress() != null
				? wordFileInputPOJO.getVendorBillingAddress()
				: "";
		String gstNumber = wordFileInputPOJO.getGstNumber() != null ? wordFileInputPOJO.getGstNumber() : "";
		String vendorName = wordFileInputPOJO.getVendorName() != null ? wordFileInputPOJO.getVendorName() : "";
		String code = wordFileInputPOJO.getCode() != null ? wordFileInputPOJO.getCode() : "";
		String date = wordFileInputPOJO.getDate() != null ? wordFileInputPOJO.getDate() : "";
		String attn = wordFileInputPOJO.getAttn() != null ? wordFileInputPOJO.getAttn() : "";
		String candidateName = wordFileInputPOJO.getCandidateName() != null ? wordFileInputPOJO.getCandidateName() : "";
		String qualification = wordFileInputPOJO.getQualification() != null ? wordFileInputPOJO.getQualification() : "";
		String majors = wordFileInputPOJO.getMajors() != null ? wordFileInputPOJO.getMajors() : "";
		String yearOfPassing = wordFileInputPOJO.getYearOfPassing() != null ? wordFileInputPOJO.getYearOfPassing() : "";
		String yearOfGraduation = wordFileInputPOJO.getYearOfGraduation() != null
				? wordFileInputPOJO.getYearOfGraduation()
				: "";
		String rollNo = wordFileInputPOJO.getRollNo() != null ? wordFileInputPOJO.getRollNo() : "";

		XWPFDocument xwpfDocument = new XWPFDocument();
//        XWPFParagraph xwpfParagraph = xwpfDocument.createParagraph()
//        XWPFRun xwpfRun = xwpfParagraph.createRun()
		int twipsPerInch = 1440;

		/*******************************************************************/
		/*************************** SET HEADERS *****************************/
		/*******************************************************************/

		XWPFHeader xwpfHeader = xwpfDocument.createHeader(HeaderFooterType.DEFAULT);
		XWPFTable headerTable = xwpfHeader.createTable(1, 2);
//        XWPFParagraph xwpfHeaderParagraph = xwpfHeader.createParagraph()
//        XWPFRun xwpfHeaderRun = xwpfHeaderParagraph.createRun()

//        XWPFParagraph xwpfParagraph = xwpfDocument.createParagraph()
//        XWPFTable headerTable = xwpfDocument.createTable()
		headerTable.setWidth(13 / 2 * twipsPerInch);

		/*
		 * Create CTTblGrid with widths of the 2 columns. It is necessary for
		 * LibreOffice/OpenOffice to accept the column widths.
		 */

		headerTable.getCTTbl().getTblPr().unsetTblBorders();

		/* First column = 2 inches width */
		headerTable.getCTTbl().addNewTblGrid().addNewGridCol().setW(BigInteger.valueOf((long) 2 * twipsPerInch));

		/* Second column = 4 inches width */
		headerTable.getCTTbl().getTblGrid().addNewGridCol().setW(BigInteger.valueOf((long) 4 * twipsPerInch));

		/* Create first row */
		XWPFTableRow headerTableRow = headerTable.getRow(0);

		/* Create first cell */
		XWPFTableCell headerTableCell = headerTableRow.getCell(0);

		/* Set width for first column = 2 inches */
		CTTblWidth tblWidth = headerTableCell.getCTTc().addNewTcPr().addNewTcW();
		tblWidth.setW(BigInteger.valueOf((long) 2 * twipsPerInch));

		/* STTblWidth.DXA is used to specify width in twentieth of a point. */
		tblWidth.setType(STTblWidth.DXA);

		/* First paragraph in first cell */
		XWPFParagraph xwpfHeaderParagraph = headerTableCell.getParagraphArray(0);
		if (xwpfHeaderParagraph == null)
			xwpfHeaderParagraph = headerTableCell.addParagraph();
		headerTableCell.setVerticalAlignment(XWPFVertAlign.CENTER);

		/* First run in paragraph having picture */
		XWPFRun xwpfHeaderRun = xwpfHeaderParagraph.createRun();
		//System.out.println("Path"+ClassLoader.getSystemResource(logo).toURI());
		//System.out.println("New Path"+fadvLogo);
		File f = new File(fadvLogo + logo);
		Path imagePath = Paths.get(f.toURI());
		//Path imagePath = Paths.get(ClassLoader.getSystemResource(logo).toURI());
		xwpfHeaderRun.addPicture(Files.newInputStream(imagePath), Document.PICTURE_TYPE_PNG,
				imagePath.getFileName().toString(), Units.toEMU(250), Units.toEMU(50));

//        /* Second Cell */
		headerTableCell = headerTableRow.addNewTableCell();
		headerTableCell.setWidth(String.valueOf(twipsPerInch / 4));

		/* Second Cell */
		headerTableCell = headerTableRow.addNewTableCell();
		tblWidth = headerTableCell.getCTTc().addNewTcPr().addNewTcW();
		tblWidth.setW(BigInteger.valueOf((long) 3 * twipsPerInch));

		/* Paragraph in Second Cell */
		xwpfHeaderParagraph = headerTableCell.addParagraph();

		ArrayList<String> textList = new ArrayList<>(Arrays.asList("First Advantage Private Limited.",
				"Interface - Building No.7, 1st Floor,", "Link Road, Malad (West)", "Mumbai - 400 064",
				"Tel: (022) 40697000 Ext: 7824", "Fax: (022) 40697249", "Email: pravina.chavri@fadv.com"));
		for (int idx = 0; idx < textList.size(); idx++) {
			setHeaderText(textList.get(idx), xwpfHeaderParagraph);
		}

		/*******************************************************************/
		/*************************** SET BODY ********************************/
		/*******************************************************************/
		XWPFTable bodyTable1 = xwpfDocument.createTable(4, 4);
		bodyTable1.setWidth(6 * twipsPerInch);

		/* First column = 2 inches width */
		bodyTable1.getCTTbl().addNewTblGrid().addNewGridCol().setW(BigInteger.valueOf((long) 2 * twipsPerInch));

		/* Second column = 4 inches width */
		bodyTable1.getCTTbl().getTblGrid().addNewGridCol().setW(BigInteger.valueOf((long) 4 * twipsPerInch));
		XWPFTableRow bodyTable1Row = null;
		XWPFTableCell bodyTable1Cell = null;

		String[][] bodyTable1Values = { { "Institution Name", institutionName, "Date", date },
				{ "Attn.", attn, "Code", code }, { "State", state, "Vendor Name", vendorName },
				{ "Vendor Billing Address", vendorBillingAddress, "GST Number", gstNumber } };

		for (int row = 0; row < 4; row++) {
			bodyTable1Row = bodyTable1.getRow(row);
			for (int col = 0; col < 4; col++) {
				bodyTable1Cell = bodyTable1Row.getCell(col);
				bodyTable1Cell.setVerticalAlignment(XWPFVertAlign.CENTER);
				XWPFParagraph xwpfParagraph = bodyTable1Cell.addParagraph();
				setTableHeaderText(bodyTable1Values[row][col], xwpfParagraph);
			}
		}

		/*******************************************************************/
		XWPFParagraph xwpfParagraph = xwpfDocument.createParagraph();
		XWPFRun xwpfRun = xwpfParagraph.createRun();
		xwpfRun.setText("Note: There is an exemption contained for services provided by an "
				+ "entity registered under section 12AA of the Income-tax Act, 1961 (43 of 1961) by "
				+ "way of charitable activities. Hence, request you to provide the invoice for the "
				+ "verification provided (if GST Registered. ");
		xwpfRun.setColor(colorWhite);
		xwpfRun.setItalic(true);
		xwpfRun.setBold(true);
		xwpfRun.addBreak();
		xwpfRun.setFontFamily(fontFamily);
		xwpfRun.setFontSize(10);

		/*******************************************************************/
		xwpfParagraph = xwpfDocument.createParagraph();
		xwpfRun = xwpfParagraph.createRun();
		xwpfRun.setText("Dear Sir/ Madam,");
		xwpfRun.setColor(colorWhite);
		xwpfRun.addBreak();
		xwpfRun.setFontFamily(fontFamily);
		xwpfRun.setFontSize(10);

		/*******************************************************************/
		xwpfParagraph = xwpfDocument.createParagraph();
		xwpfRun = xwpfParagraph.createRun();
		xwpfRun.setText("Urgent Request for Educational Verification");
		xwpfRun.setColor(colorWhite);
		xwpfRun.setUnderline(UnderlinePatterns.DASH_LONG);
		xwpfRun.addBreak();
		xwpfRun.setFontFamily(fontFamily);
		xwpfRun.setFontSize(10);

		/*******************************************************************/
		xwpfParagraph = xwpfDocument.createParagraph();
		xwpfRun = xwpfParagraph.createRun();
		xwpfRun.setText("First Advantage Private Limited is Asia's leading pre-employment "
				+ "screening company. We are acting on behalf of our client in respect of "
				+ "the provision of Pre-Employment Screening Services. I request you to "
				+ "verify the information provided by our prospective employee who claims "
				+ "to have qualified from your Institute.");
		xwpfRun.setColor(colorWhite);
		xwpfRun.addBreak();
		xwpfRun.setFontFamily(fontFamily);
		xwpfRun.setFontSize(10);

		xwpfParagraph = xwpfDocument.createParagraph();
		xwpfRun = xwpfParagraph.createRun();
		xwpfRun.setText("I would be most grateful if you could take a few minutes and send"
				+ " the response at the earliest. Please contact me if you have any queries."
				+ " Thank you in anticipation of your assistance. Your input is greatly valued "
				+ "you quick response will be much appreciated.");
		xwpfRun.setColor(colorWhite);
		xwpfRun.addBreak();
		xwpfRun.setFontFamily(fontFamily);
		xwpfRun.setFontSize(10);

		/*******************************************************************/
		XWPFTable bodyTable2 = xwpfDocument.createTable(2, 7);
		bodyTable2.setWidth(6 * twipsPerInch);

		/* First column = 2 inches width */
		bodyTable2.getCTTbl().addNewTblGrid().addNewGridCol().setW(BigInteger.valueOf((long) 2 * twipsPerInch));

		/* Second column = 4 inches width */
		bodyTable2.getCTTbl().getTblGrid().addNewGridCol().setW(BigInteger.valueOf((long) 4 * twipsPerInch));
		XWPFTableRow bodyTable2Row = null;
		XWPFTableCell bodyTable2Cell = null;

		String[][] bodyTable2Values = {
				{ "Check Id", "Candidate Name", "Qualification", "Majors", "Year of Passing", "Year of Graduation",
						"Roll # (Unique)" },
				{ checkId, candidateName, qualification, majors, yearOfPassing, yearOfGraduation, rollNo } };

		for (int row = 0; row < 2; row++) {
			bodyTable2Row = bodyTable2.getRow(row);
			for (int col = 0; col < 7; col++) {
				bodyTable2Cell = bodyTable2Row.getCell(col);
				bodyTable2Cell.setVerticalAlignment(XWPFVertAlign.CENTER);
				xwpfParagraph = bodyTable2Cell.addParagraph();
				setTableHeaderText(bodyTable2Values[row][col], xwpfParagraph);
			}
		}

		/*******************************************************************/

		ArrayList<String> bulletList = new ArrayList<>(Arrays.asList("Information and document provided is Correct",
				"Information and document provided is Not Correct",
				"If not Correct, please provide the information as per your records."));

		generateStringList(xwpfDocument, bulletList);

		/*******************************************************************/
		XWPFTable bodyTable3 = xwpfDocument.createTable(4, 3);
		bodyTable3.setWidth(6 * twipsPerInch);

		/* First column = 2 inches width */
		bodyTable3.getCTTbl().addNewTblGrid().addNewGridCol().setW(BigInteger.valueOf((long) 2 * twipsPerInch));

		/* Second column = 4 inches width */
		bodyTable3.getCTTbl().getTblGrid().addNewGridCol().setW(BigInteger.valueOf((long) 4 * twipsPerInch));

		XWPFTableRow bodyTable3Row = null;
		XWPFTableCell bodyTable3Cell = null;

		for (int row = 0; row < 4; row += 2) {
			bodyTable3Row = bodyTable3.getRow(row);
			for (int col = 0; col < 3; col++) {
				bodyTable3Cell = bodyTable3Row.getCell(col);
				bodyTable3Cell.setVerticalAlignment(XWPFVertAlign.CENTER);
				xwpfParagraph = bodyTable3Cell.addParagraph();
				setTableHeaderText("Name1", xwpfParagraph);
			}
		}

		IBody iBody = xwpfDocument.getBodyElements().get(0).getBody();
		for (XWPFParagraph par : iBody.getParagraphs()) {
			CTSpacing spacing;
			try {
				spacing = par.getCTP().getPPr().getSpacing();
			} catch (Exception e) {
				spacing = null;
			}
			if (spacing == null) {
				par.setSpacingLineRule(LineSpacingRule.AUTO);
				spacing = par.getCTP().getPPr().getSpacing();
			}
			spacing.setAfter(BigInteger.valueOf(50));
			spacing.setLineRule(STLineSpacingRule.Enum.forString("auto"));
		}

		/*******************************************************************/
		/* create footer table */
		XWPFFooter xwpfFooter = xwpfDocument.createFooter(HeaderFooterType.DEFAULT);
		XWPFParagraph xwpfFooterParagraph = xwpfFooter.createParagraph();
		XWPFRun xwpfFooterRun = xwpfFooterParagraph.createRun();
		xwpfFooterRun.setText("This message is intended only for the individual or entity "
				+ "to which it is addressed and may contain information that is privileged, "
				+ "confidential and exempt from disclosure under applicable law. If the "
				+ "reader of this message is not the intended recipient, you are hereby "
				+ "notified that any dissemination, distribution or copying of this "
				+ "communication is strictly prohibited. If you have received this communication "
				+ "in error, or if any problems occur with transmission, please notify us "
				+ "immediately by telephone. Thank you.");
		xwpfFooterRun.setColor(colorWhite);
		xwpfFooterRun.setFontFamily(fontFamily);
		xwpfFooterRun.setFontSize(8);

//        xwpfFooterParagraph = xwpfFooter.createParagraph()
//        xwpfFooterRun = xwpfFooterParagraph.createRun()
		XWPFTable footerTable = xwpfFooter.createTable(1, 2);
		footerTable.setWidth(6 * twipsPerInch);

		footerTable.getCTTbl().getTblPr().unsetTblBorders();
		/* First column = 2 inches width */
		footerTable.getCTTbl().addNewTblGrid().addNewGridCol().setW(BigInteger.valueOf((long) 2 * twipsPerInch));

		/* Second column = 4 inches width */
		footerTable.getCTTbl().getTblGrid().addNewGridCol().setW(BigInteger.valueOf((long) 4 * twipsPerInch));
		XWPFTableRow footerTableRow = footerTable.getRow(0);
		XWPFTableCell footerTableCell = footerTableRow.getCell(0);
		xwpfFooterParagraph = footerTableCell.addParagraph();
		xwpfFooterParagraph.setAlignment(ParagraphAlignment.LEFT);
		xwpfFooterRun = xwpfFooterParagraph.createRun();
		xwpfFooterRun.setText("Confidential");
		xwpfFooterRun.setColor(colorWhite);
		xwpfFooterRun.setFontFamily(fontFamily);
		xwpfFooterRun.setFontSize(10);

		footerTableCell = footerTableRow.getCell(1);
		xwpfFooterParagraph = footerTableCell.addParagraph();
		xwpfFooterParagraph.setAlignment(ParagraphAlignment.RIGHT);
		xwpfFooterRun = xwpfFooterParagraph.createRun();
		xwpfFooterRun.setText("Page " + HeaderFooter.page() + " of " + HeaderFooter.numPages());
		xwpfFooterRun.setColor(colorWhite);
		xwpfFooterRun.setFontFamily(fontFamily);
		xwpfFooterRun.setFontSize(10);

		/*******************************************************************/
		/*************************** FILE OUTPUT *****************************/
		/*******************************************************************/

		FileOutputStream out = new FileOutputStream(outputFile);
		xwpfDocument.write(out);
		out.close();
		xwpfDocument.close();
		return outputFile;
	}

	public void setSingleLineSpacing(XWPFParagraph xwpfParagraph) {
		CTPPr ctpPr = xwpfParagraph.getCTP().getPPr();
		if (ctpPr == null)
			ctpPr = xwpfParagraph.getCTP().addNewPPr();
		CTSpacing ctSpacing = ctpPr.isSetSpacing() ? ctpPr.getSpacing() : ctpPr.addNewSpacing();
		ctSpacing.setAfter(BigInteger.valueOf(0));
		ctSpacing.setBefore(BigInteger.valueOf(0));
		ctSpacing.setLineRule(STLineSpacingRule.AUTO);
		ctSpacing.setLine(BigInteger.valueOf(240));
	}

	public void setHeaderText(String string, XWPFParagraph xwpfParagraph) {
		xwpfParagraph.setAlignment(ParagraphAlignment.LEFT);
		XWPFRun xwpfRun = xwpfParagraph.createRun();
		xwpfRun.setText(string);
		xwpfRun.setColor(colorWhite);
		xwpfRun.setBold(true);
		xwpfRun.addBreak();
		xwpfRun.setFontFamily(fontFamily);
		xwpfRun.setFontSize(10);
	}

	public void setTableHeaderText(String string, XWPFParagraph xwpfParagraph) {
		xwpfParagraph.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun xwpfRun = xwpfParagraph.createRun();
		xwpfRun.setText(string);
		xwpfRun.setColor(colorWhite);
		xwpfRun.setFontFamily(fontFamily);
		xwpfRun.setFontSize(10);
	}

	private void generateStringList(XWPFDocument xwpfDocument, ArrayList<String> bulletList) {
		CTAbstractNum cTAbstractNum = CTAbstractNum.Factory.newInstance();
		cTAbstractNum.setAbstractNumId(BigInteger.valueOf(0));
		CTLvl cTLvl = cTAbstractNum.addNewLvl();
		cTLvl.addNewNumFmt().setVal(STNumberFormat.BULLET);

		XWPFAbstractNum abstractNum = new XWPFAbstractNum(cTAbstractNum);
		XWPFNumbering numbering = xwpfDocument.createNumbering();
		BigInteger abstractNumID = numbering.addAbstractNum(abstractNum);
		BigInteger numID = numbering.addNum(abstractNumID);

		for (String item : bulletList) {
			XWPFParagraph bulletedPara = xwpfDocument.createParagraph();
			XWPFRun run = bulletedPara.createRun();
			run.setFontFamily(fontFamily);
			run.setFontSize(10);
			run.setText(item);
			bulletedPara.setNumID(numID);
		}
	}

	public String convertTextFileToString(String fileName) {
		try (Stream<String> stream = Files.lines(Paths.get(ClassLoader.getSystemResource(fileName).toURI()))) {
			return stream.collect(Collectors.joining(" "));
		} catch (IOException | URISyntaxException e) {
			return null;
		}
	}
}
