package com.vandergriff.core.db.jdbc;

public class PrimaryKeyProviderException extends Exception {

	private static final long serialVersionUID = -3998991029686287629L;

	public PrimaryKeyProviderException() {
		super();
	}

	public PrimaryKeyProviderException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PrimaryKeyProviderException(String message, Throwable cause) {
		super(message, cause);
	}

	public PrimaryKeyProviderException(String message) {
		super(message);
	}

	public PrimaryKeyProviderException(Throwable cause) {
		super(cause);
	}

}
