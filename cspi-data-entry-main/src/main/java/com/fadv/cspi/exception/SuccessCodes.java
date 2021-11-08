package com.fadv.cspi.exception;

public class SuccessCodes {

	private SuccessCodes() {
		throw new IllegalStateException("SuccessCodes class");
	}

	public static final String DATAFOUND_MSG = "Data found for selected criteria";
	public static final String DATAFOUND_CODE = "SUCCESS_CODE_200";
	public static final String CB_CRITERIA_MSG = "Case details found for selected criteria";
	public static final String CIB_CRITERIA_MSG = "Check Id details found for selected criteria";
	public static final String ID_RECORD_FOUND_MSG = "Record found";
	public static final String CB_CRITERIA_DATA_POINT_MSG = "Data Points found for selected case detals criteria";
}
