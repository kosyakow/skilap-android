package com.pushok.skilap.apiData;

import java.util.List;

public class AssetsData {
	public String _id;
	public String name;
	public CmdtyData cmdty;
	public String parentId;
	public String path;
	public Double value;
	public Double quantity;
	public Integer deep;
	public Boolean visible = true;
	public Boolean enabled = true;
	public Boolean header = false;
	public List<AssetsData> childs;
	public Integer count = 0;
	public String[] direction = new String[2];
	public int act;
	//for save
	public Boolean placeholder = false;
	public Boolean hidden = false;
	public String type; 
}
