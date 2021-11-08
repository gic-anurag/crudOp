package com.fadv.cspi.exception;

public class ServiceException extends BaseException {

	private static final long serialVersionUID = 1L;

	public ServiceException(Exception e) {
		super("Service Exception", e);
	}

	public ServiceException(String msg) {
		super(msg);
	}

	public ServiceException(String msg, Exception e) {
		super(msg, e);
	}

	public ServiceException(String message, String messageId) {
		super(message, messageId);
	}

	public ServiceException(String message, String messageId, Exception e) {
		super(message, messageId, e);
	}
}
