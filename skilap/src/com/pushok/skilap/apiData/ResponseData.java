package com.pushok.skilap.apiData;

import org.codehaus.jackson.JsonNode;

public class ResponseData {
	public int id;
	public JsonNode error;
	public JsonNode result;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public JsonNode getError() {
		return error;
	}
	public void setError(JsonNode error) {
		this.error = error;
	}
	public JsonNode getResult() {
		return result;
	}
	public void setResult(JsonNode result) {
		this.result = result;
	}
}
