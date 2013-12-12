package com.pushok.skilap.adapter;

public class SplitData {
	public enum Direction {Spent, Received};
	public String accId;
	public String accPath;
	public Direction direction;
	public Double value;
}

