package com.vandergriff.core.db.jdbc;
public class ParamsMapperException extends Exception {

	private static final long serialVersionUID = -6605101823296782927L;

	/**
	 * 
	 */
	public ParamsMapperException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public ParamsMapperException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ParamsMapperException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public ParamsMapperException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ParamsMapperException(Throwable cause) {
		super(cause);
	}

}
