package com.gic.fadv.verification.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.http.MediaType;

import com.gic.fadv.verification.mapping.model.DbQuestionaireMapping;

//import com.gic.fadv.verification.docs.model.QuestionaireMapping;
import com.gic.fadv.verification.mapping.model.QuestionaireMapping;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

public class Utility {

	private Utility() {
		super();
	}

	private static final Logger logger = LoggerFactory.getLogger(Utility.class);

	public static void printGettersSetters(Class<?> aClass) {
		Method[] methods = aClass.getMethods();

		for (Method method : methods) {
			if (isGetter(method))
				logger.info(Marker.ANY_MARKER, "getter", method);

			if (isSetter(method))
				logger.info(Marker.ANY_MARKER, "setter", method);
		}
	}

	public static boolean isGetter(Method method) {
		if (!method.getName().startsWith("get"))
			return false;

		if (method.getParameterTypes().length != 0)
			return false;

		if (void.class.equals(method.getReturnType()))
			return false;

		return true;
	}

	public static boolean isSetter(Method method) {
		if (!method.getName().startsWith("set"))
			return false;
		if (method.getParameterTypes().length != 1)
			return false;

		return true;
	}

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

	public static Date convertStringToDate(String dateStr) {
		Date parsedDate = null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			parsedDate = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			// this generic but you can control another types of exception
			// look the origin of excption
			e.printStackTrace();
		}
		return parsedDate;
	}

	public static void removeAttachmentFile(File file) {
		Runnable r1 = () -> {
			try {
				Thread.sleep(15000);
			} catch (Exception e) {
				logger.debug(e.getMessage(), e);
			}
			Utility.removeFiles(Collections.singletonList(file));
		};
		try {
			new Thread(r1).start();
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
		}
	}

	public static void removeFiles(List<File> fileObjList) {
		for (File file : fileObjList) {
			try {
				Files.deleteIfExists(file.toPath());
			} catch (Exception e) {
				logger.info(e.getMessage(), e);
			}
		}
	}

	/**
	 * Map csv file with QuestionnerMapping entity
	 * 
	 * @param fileName
	 * @return
	 */
	public static List<QuestionaireMapping> readFromCSV(String fileName) {
		List<QuestionaireMapping> csvDataList = new ArrayList<>();
		try {
			InputStreamReader inStrm = new InputStreamReader(new FileInputStream(fileName));

			// parse CSV file to create a list of `CSVData` objects
			try (Reader reader = new BufferedReader(inStrm)) {

				// create csv bean reader
				CsvToBean<QuestionaireMapping> csvToBean = new CsvToBeanBuilder<QuestionaireMapping>(reader)
						.withType(QuestionaireMapping.class).withIgnoreLeadingWhiteSpace(true).build();

				// convert `CsvToBean` object to list of csvDataList
				csvDataList = csvToBean.parse();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return csvDataList;
	}

	/**
	 * Map csv file with DbQuestionnerMapping Entity
	 * 
	 * @param fileName
	 * @return
	 */
	public static List<DbQuestionaireMapping> readDbQuestionaireMappingCSV(String fileName) {
		List<DbQuestionaireMapping> csvDataList = new ArrayList<>();
		try {
			InputStreamReader inStrm = new InputStreamReader(new FileInputStream(fileName));

			// parse CSV file to create a list of `CSVData` objects
			try (Reader reader = new BufferedReader(inStrm)) {

				// create csv bean reader
				CsvToBean<DbQuestionaireMapping> csvToBean = new CsvToBeanBuilder<DbQuestionaireMapping>(reader)
						.withType(DbQuestionaireMapping.class).withIgnoreLeadingWhiteSpace(true).build();

				// convert `CsvToBean` object to list of csvDataList
				csvDataList = csvToBean.parse();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return csvDataList;
	}

	/**
	 * this utility method convert date from EDT to IST format +9:30 hours added
	 * into current time
	 * 
	 * @return
	 */
	public static Date convertEdtToIst(Date crtDate) {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  hh:mm:ss");
		Calendar calendar = Calendar.getInstance();
		try {
			if (crtDate != null) {

				calendar.setTime(crtDate);
				calendar.add(Calendar.HOUR, 9);
				calendar.add(Calendar.MINUTE, 30);
				crtDate = calendar.getTime();

				return dateFormat.parse(dateFormat.format(crtDate));
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return crtDate;
	}

	public static MediaType getMediaTypeForFileName(ServletContext servletContext, String fileName) {
		// application/pdf
		// application/xml
		// image/gif, ...
		String mineType = servletContext.getMimeType(fileName);
		try {
			MediaType mediaType = MediaType.parseMediaType(mineType);
			return mediaType;
		} catch (Exception e) {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}
}
