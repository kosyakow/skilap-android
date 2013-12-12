package com.pushok.skilap.apiData;

public class CmdtyData {
	public String id;
	public String space;
	public Double rate;
	public CmdtyData() {}
	public CmdtyData(String id, String space) {
		this.id = id;
		this.space = space;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSpace() {
		return space;
	}
	public void setSpace(String space) {
		this.space = space;
	}
	public Double getRate() {
		return rate;
	}
	public void setRate(Double rate) {
		this.rate = rate;
	}
}
