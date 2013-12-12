package com.pushok.skilap.apiData;

public class SaveSplitsObjectData {
	public class SaveSplitObjectData {
		public Double quantity;
		public String accountId;
	}
	public SaveSplitsObjectData(int size) {
		splits = new SaveSplitObjectData[size];
		for (int i = 0; i<size; i++)
			splits[i] = new SaveSplitObjectData();
	}
	public String datePosted;
	public String dateEntered;
	public String description;
	public String _id = null;
	public SaveSplitObjectData[] splits;
}
