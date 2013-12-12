package com.pushok.skilap.activity;

import android.os.AsyncTask;
import android.util.Log;

public class MyAsyncTask extends AsyncTask<Integer, Integer, AsyncTaskObject> {
	private ApiLoader loader;
	private int id;
	public MyAsyncTask(ApiLoader loader, int id) {
		this.loader = loader;
		this.id = id;
	}
	protected void onPreExecute() {
		loader.preLoad(id);
	}
	@Override
	protected AsyncTaskObject doInBackground(Integer... params) {
		try {
			return AsyncTaskObject.Ok(loader.load(id));
		} catch (Exception e) {
			Log.d("Async exception", e.toString());
			return AsyncTaskObject.Error((e.getMessage() != null)?e.getMessage():e.toString(), e);
		}
	}
	protected void onPostExecute(AsyncTaskObject o) {
		loader.afterLoad(o, id);
	}
}

