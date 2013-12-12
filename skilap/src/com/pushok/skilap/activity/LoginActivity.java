package com.pushok.skilap.activity;

import com.pushok.skilap.R;
import com.pushok.skilap.apiData.Api;
import com.pushok.skilap.exception.ScilapException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity implements OnClickListener, ApiLoader {
	private String url;
    private String login;
    private String password;
	private ProgressDialog dialog;
	public String PREFS_NAME = "MyLoginPrefs";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = null;
        setContentView(R.layout.main);

        Button button = (Button)findViewById(R.id.btLogin);
        button.setOnClickListener(this);
        
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String sUrl = settings.getString("url", null);
		String sLogin = settings.getString("login", null);
		String sPassword = settings.getString("password", null);
		if (sUrl != null)
			((EditText)findViewById(R.id.etDomain)).setText(sUrl);
		if (sLogin != null)
			((EditText)findViewById(R.id.etUserName)).setText(sLogin);
		if (sPassword != null)
			((EditText)findViewById(R.id.etPassword)).setText(sPassword);

		if (getLastNonConfigurationInstance() != null)
			preLoad(0);
    }
	@Override
	public void onDestroy() {
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
		super.onDestroy();
	}
	@Override
	public Object onRetainNonConfigurationInstance() {
		return dialog;
	}
    public void onClick(View v) {
    	url = ((EditText)findViewById(R.id.etDomain)).getText().toString();
    	login = ((EditText)findViewById(R.id.etUserName)).getText().toString();
    	password = ((EditText)findViewById(R.id.etPassword)).getText().toString();
    	Api.setUrl(url);
    	Api.setLogin(login);
    	Api.setPassword(password);
    	new MyAsyncTask(this, 0).execute(0);
    }

	public Object load(int id) throws ScilapException {
   		String ret = Api.requestLogin(login, password);
   		if (ret == null) throw new ScilapException();
   		return ret;
	}

	public void preLoad(int id) {
		dialog = new ProgressDialog(this);
	    dialog.setMessage(getString(R.string.DoLogin));
	    dialog.setIndeterminate(true);
	    dialog.setCancelable(true);
	    dialog.show();
	}

	public void afterLoad(AsyncTaskObject o, int id) {
		if (dialog != null)
			dialog.dismiss();
		if (o.isError) {
			new SimpleAlertDlg(this, o.error, false).show();
			return;
		}
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("url", url);
		editor.putString("login", login);
		editor.putString("password", password);
		editor.putString("screenName", (String)o.o);
		editor.commit();
		
	    Intent intent = getIntent();
		intent.putExtra("screenName", (String)o.o);
	    setResult(RESULT_OK, intent);
	    finish();
	}
	
	@Override
	public void onBackPressed() {
	}
}