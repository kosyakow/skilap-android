package com.pushok.skilap.exception;

public class ApiException extends ScilapException {
	private static final long serialVersionUID = 1L;
	public ApiException() {
		super();
	}
	public ApiException(String message) {
		super();
		this.message = message;
	}
}
