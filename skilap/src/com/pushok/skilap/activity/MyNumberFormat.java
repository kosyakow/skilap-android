package com.pushok.skilap.activity;

import java.text.NumberFormat;
import java.util.Currency;

import com.pushok.skilap.apiData.CmdtyData;

public class MyNumberFormat {
	public static NumberFormat getCurrencyInstance(CmdtyData cmdty) {
		if (cmdty == null) return NumberFormat.getCurrencyInstance();
		NumberFormat fmt = NumberFormat.getCurrencyInstance();
		fmt.setCurrency(Currency.getInstance(cmdty.id));
		return fmt;
	}
	public static NumberFormat getCurrencyInstance(String id) {
		if (id == null) return NumberFormat.getCurrencyInstance();
		NumberFormat fmt = NumberFormat.getCurrencyInstance();
		fmt.setCurrency(Currency.getInstance(id));
		return fmt;
	}
}
