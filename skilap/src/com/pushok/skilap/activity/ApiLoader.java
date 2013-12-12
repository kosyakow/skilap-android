package com.pushok.skilap.activity;

import com.pushok.skilap.exception.ScilapException;

public interface ApiLoader {
	void preLoad(int id);
	Object load(int id) throws ScilapException;
	void afterLoad(AsyncTaskObject o, int id);
}
