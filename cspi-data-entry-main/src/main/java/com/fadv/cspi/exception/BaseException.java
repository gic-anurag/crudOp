package com.fadv.cspi.exception;

public class BaseException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String messageId;

	public BaseException(String message) {
		super(message);
		this.messageId = "";
	}

	public BaseException(String message, Exception e) {
		super(message);
		this.messageId = "";
	}

	public BaseException(String message, String messageId) {
		super(message);
		this.messageId = messageId;
	}

	public BaseException(String message, String messageId, Exception e) {
		super(message);
		this.messageId = messageId;
	}

	public String getMessageId() {
		return this.messageId;
	}
}
