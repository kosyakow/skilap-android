package com.pushok.skilap.activity;

public class AsyncTaskObject {
	public boolean isError;
	public String error;
	public Object o;
	public Exception exception;
	public AsyncTaskObject(boolean isError, String error, Object o, Exception e) {
		this.isError = isError;
		this.error = error;
		this.o = o;
		this.exception = e;
		
	}
	public static AsyncTaskObject Ok(Object o) {
		return new AsyncTaskObject(false, null, o, null);
	}
	public static AsyncTaskObject Error(String error, Exception e) {
		return new AsyncTaskObject(true, error, null, e);
	}
}
