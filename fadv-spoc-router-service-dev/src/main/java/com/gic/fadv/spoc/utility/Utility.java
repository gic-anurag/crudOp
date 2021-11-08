package com.gic.fadv.spoc.utility;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.text.StringEscapeUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Utility {
	public static void printGettersSetters(Class aClass) {
		Method[] methods = aClass.getMethods();

		for (Method method : methods) {
			if (isGetter(method))
				System.out.println("getter: " + method);
			if (isSetter(method))
				System.out.println("setter: " + method);
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

	private static List<LocalDate> countBusinessDaysBetween_Java8(final LocalDate startDate, final LocalDate endDate,
			final Optional<List<LocalDate>> holidays) {
		// Validate method arguments
		if (startDate == null || endDate == null) {
			throw new IllegalArgumentException("Invalid method argument(s) to countBusinessDaysBetween (" + startDate
					+ "," + endDate + "," + holidays + ")");
		}

		// Predicate 1: Is a given date is a holiday
		Predicate<LocalDate> isHoliday = date -> holidays.isPresent() && holidays.get().contains(date);

		// Predicate 2: Is a given date is a weekday
		Predicate<LocalDate> isWeekend = date -> date.getDayOfWeek() == DayOfWeek.SATURDAY
				|| date.getDayOfWeek() == DayOfWeek.SUNDAY;

		// Get all days between two dates
		long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);

		// Iterate over stream of all dates and check each day against any weekday or
		// holiday
		return Stream.iterate(startDate, date -> date.plusDays(1)).limit(daysBetween)
				.filter(isHoliday.or(isWeekend).negate()).collect(Collectors.toList());
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

	public static List<String> findStringBetweenChars(String inputStr, String openChar, String closeChar,
			boolean isTemplate) {
		if (Boolean.TRUE.equals(isTemplate)) {
			inputStr = inputStr.replaceAll("\\<(.*?)\\>", "");
			inputStr = inputStr.replace("&nbsp;", "");

			inputStr = StringEscapeUtils.unescapeHtml4(inputStr);
		}
		String patterStr = openChar + "(.*?)" + closeChar;
		Pattern pattern = Pattern.compile(patterStr, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(inputStr);

		List<String> matchedStrings = new ArrayList<>();
		while (matcher.find()) {
			matchedStrings.add(matcher.group(1));
		}
		return matchedStrings;
	}

	public static Map<String, String> findStringMapBetweenChars(String inputStr, String openChar, String closeChar,
			boolean isTemplate) {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		
		String patterStr = openChar + "(.*?)" + closeChar;
		Pattern pattern = Pattern.compile(patterStr, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(inputStr);

		Map<String, String> matchedStrings = new HashMap<>();
		while (matcher.find()) {
			String matchGrp1 = matcher.group(1);
			if (Boolean.TRUE.equals(isTemplate)) {
				matchGrp1 = matchGrp1.replaceAll("\\<(.*?)\\>", "");
				matchGrp1 = StringEscapeUtils.unescapeHtml4(matchGrp1);
			}
			matchedStrings.put(matchGrp1, matcher.group());
		}
		return matchedStrings;
	}
}
