package fadv.verification.workflow.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class Utility {

	private Utility() {
		throw new IllegalStateException("Utility class");
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

	public static Long getDateDiffInMinutes(Date fromDate, Date toDate) {

		LocalDateTime localFromDate = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		LocalDateTime localToDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

		return Duration.between(localFromDate, localToDate).toMinutes();
	}

	public static String formatString(String inputStr) {

		if (inputStr == null || StringUtils.isEmpty(inputStr)) {
			return inputStr;
		}

		Map<String, String> patternMap = new HashMap<>();
		patternMap.put("#dot#", ".");

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

	public static String compareName(String primaryName, String secondaryName) {
		String newSecName = "";
		if (StringUtils.isNotEmpty(secondaryName)
				&& !StringUtils.equalsIgnoreCase(primaryName.trim(), secondaryName.trim())) {
			String[] primaryNameEle = primaryName.split(" \\s*");
			String[] secondaryNameEle = secondaryName.split(" \\s*");
			if (primaryNameEle.length > secondaryNameEle.length
					&& Boolean.TRUE.equals(stringArrMatch(secondaryNameEle, primaryNameEle))) {
				newSecName = secondaryName;
			} else if (primaryNameEle.length < secondaryNameEle.length) {
				newSecName = secondaryName;
			} else if (primaryNameEle.length == secondaryNameEle.length
					&& Boolean.TRUE.equals(stringArrMatch(primaryNameEle, secondaryNameEle))) {
				newSecName = secondaryName;
			}
		}
		return newSecName;
	}

	public static boolean stringArrMatch(String[] primaryArr, String[] secondaryArr) {
		List<String> secondaryList = Arrays.asList(secondaryArr);
		int countNotMatch = 0;
		for (String primary : primaryArr) {
			if (!secondaryList.contains(primary))
				countNotMatch++;
		}
		if (countNotMatch > 0) {
			return true;
		}
		return false;
	}
}
