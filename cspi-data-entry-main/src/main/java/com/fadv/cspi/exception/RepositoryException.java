package com.fadv.cspi.exception;

public class RepositoryException extends Exception {

	private static final long serialVersionUID = 1L;

	public RepositoryException(String msg) {
		super(msg);
	}

	public RepositoryException(Exception e) {
		super("Repository Exception", e);
	}

	public RepositoryException(String msg, Exception e) {
		super(msg, e);
	}
}
