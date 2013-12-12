package com.pushok.skilap.apiData;

import java.util.List;

public class LastAssetsData {
	public Integer id;
	public String name;
	public String cmdty;
	public Integer parentId;
	public String path;
	public Double value;
	public Double quantity;
	public Integer deep;
	public Boolean visible = true;
	public Boolean enabled = true;
	public Boolean header = false;
	public List<LastAssetsData> childs;
}
