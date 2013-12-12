package com.pushok.skilap.apiData;

public class RequestData {
	public int id = 1;
	public String method = "core.getApiToken";
	public Object[] params = {"", "", ""};
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public Object[] getParams() {
		return params;
	}
	public void setParams(Object[] params) {
		this.params = params;
	}
}
