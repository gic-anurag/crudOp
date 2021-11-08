package com.fadv.cspi.exception;

public final class ExceptionCodes {

	private ExceptionCodes() {
		throw new IllegalStateException("ExceptionCodes class");
	}

	public static final String DATA_NOT_FOUND_ON_SEARCH_CRITERIA = "ERROR_CODE_404";
	public static final String DATA_NOT_FOUND_ON_SEARCH_CRITERIA_MSG = "Data not found for selected criteria";
	public static final String INVALID_INPUT = "Invalid Input";
	public static final String ID_RECORD_NOT_FOUND_MSG = "Record not found";
	public static final String ID_RECORD_NOT_FOUND = "ERROR_CODE_404";
}
