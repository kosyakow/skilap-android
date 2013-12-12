package com.pushok.skilap.apiData;

public class ResponseAllAccountsData {
	public Integer id;
	public Integer parentId;
	public CmdtyData cmdty;
	public String name;
	public String type;
	public Double value;
	public Boolean hidden;
	public Boolean placeholder;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getParentId() {
		return parentId;
	}
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
	public CmdtyData getCmdty() {
		return cmdty;
	}
	public void setCmdty(CmdtyData cmdty) {
		this.cmdty = cmdty;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}
	public Boolean getHidden() {
		return hidden;
	}
	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}
	public Boolean getPlaceholder() {
		return placeholder;
	}
	public void setPlaceholder(Boolean placeholder) {
		this.placeholder = placeholder;
	}
}
