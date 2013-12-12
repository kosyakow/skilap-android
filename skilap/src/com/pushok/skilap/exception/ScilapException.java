package com.pushok.skilap.exception;

public class ScilapException extends Exception {
	String message = null;
	private static final long serialVersionUID = 1L;
	public String getMessage() {
		if (message == null)
			return this.getClass().toString();
		return message;
	}
}
