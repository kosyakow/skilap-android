package com.pushok.skilap.apiData;

import java.util.LinkedHashMap;

public class BatchObjectElement {
	public String dep;
	public String cmd;
	public LinkedHashMap<String,Object> ctx;
	public Object[] prm;
	public LinkedHashMap<String,Object> res;
	public String getDep() {
		return dep;
	}
	public void setDep(String dep) {
		this.dep = dep;
	}
	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	public LinkedHashMap<String,Object> getCtx() {
		return ctx;
	}
	public void setCtx(LinkedHashMap<String,Object> ctx) {
		this.ctx = ctx;
	}
	public Object[] getPrm() {
		return prm;
	}
	public void setPrm(Object[] prm) {
		this.prm = prm;
	}
	public LinkedHashMap<String,Object> getRes() {
		return res;
	}
	public void setRes(LinkedHashMap<String,Object> res) {
		this.res = res;
	}
}
