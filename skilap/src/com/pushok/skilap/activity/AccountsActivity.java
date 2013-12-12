package com.pushok.skilap.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import com.pushok.skilap.R;
import com.pushok.skilap.adapter.AccountsAdapter;
import com.pushok.skilap.apiData.Api;
import com.pushok.skilap.apiData.AssetsData;
import com.pushok.skilap.apiData.CmdtyData;
import com.pushok.skilap.exception.ScilapException;
import com.pushok.skilap.exception.TokenException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class AccountsActivity extends Activity implements ApiLoader {
	List<AssetsData> assets = null;
	ProgressDialog dialog;
	MyAsyncTask task;
	String assetsTypes = "BANK_CASH_ASSET_STOCK_MUTUAL_CURENCY";
	String liabilitiesTypes = "CREDIT_LIABILITY_RECEIVABLE_PAYABLE";
	public static String PREFS_NAME = "MyLoginPrefs";
	CmdtyData repCmdty = new CmdtyData("RUB", "ISO4217");
	SharedPreferences settings;
	ArrayNode typesInfo;
	ListView lv;
	@Override
	public Object onRetainNonConfigurationInstance() {
		return assets;
	}
	OnItemClickListener onItemClickListener = new OnItemClickListener() {
    	public void onItemClick(AdapterView<?> parent, View view,
    		int position, long id) {
    		AccountsAdapter adapter = (AccountsAdapter)parent.getAdapter();
    		AssetsData data = (AssetsData) adapter.getItem((int) id);
    		if (!data.enabled)
    			return;
    		Bundle bundle = new Bundle();
    		bundle.putString("data", data._id);
    		bundle.putString("path", data.path);
    		bundle.putDouble("value", data.value);
    		bundle.putString("cmdty", data.cmdty.id);
    		bundle.putInt("act", data.act);
    		bundle.putStringArray("direction", data.direction);
    		Intent myIntent = new Intent(AccountsActivity.this, DetailsActivity.class);
    		myIntent.putExtras(bundle);
    		AccountsActivity.this.startActivityForResult(myIntent, ActivityInfo.DETAILSACTIVITY);
    	}
    };
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
        setContentView(R.layout.accounts);
        lv = (ListView)findViewById(R.id.lvAccounts);
	    dialog = null;
	    task = null;
	    
		String androidID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		Api.setClientId(androidID);
		settings = getSharedPreferences(PREFS_NAME, 0);
		String url = settings.getString("url", null);
		String login = settings.getString("login", null);
		String password = settings.getString("password", null);
		String token = settings.getString("token", null);
		String screenName = settings.getString("screenName", "");

		if (url != null && login != null && password != null) {
	    	Api.setUrl(url);
	    	Api.setLogin(login);
	    	Api.setPassword(password);
	    	Api.setToken(token);
		} else {
    		Intent myIntent = new Intent(AccountsActivity.this, LoginActivity.class);
    		AccountsActivity.this.startActivityForResult(myIntent, ActivityInfo.LOGINACTIVITY);
    		return;
		}
	       
		lv.setOnItemClickListener(onItemClickListener);
	    setTitle(getResources().getText(R.string.Hi) + screenName);
	    assets = (List<AssetsData>) getLastNonConfigurationInstance();
	    if (assets != null) {
			lv.setAdapter(new AccountsAdapter(this, assets));
	    } else {
	    	task = new MyAsyncTask(this, 0);
	    	task.execute(0);
	    }
	}
	@Override
	public void onDestroy() {
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
		if (task != null) {
			task.cancel(true);
			task = null;
		}
		super.onDestroy();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK && requestCode==ActivityInfo.DETAILSACTIVITY) {
			if (data != null && data.getBooleanExtra("Refresh", false))
				new MyAsyncTask(this, 0).execute(0);
		}
		if(resultCode==RESULT_OK && requestCode==ActivityInfo.TRANSACTIONACTIVITY) {
			new MyAsyncTask(this, 0).execute(0);
		}
		if(resultCode==RESULT_OK && requestCode==ActivityInfo.LOGINACTIVITY) {
			assets = new ArrayList<AssetsData>();
	        ((ListView)findViewById(R.id.lvAccounts)).setAdapter(new AccountsAdapter(this, assets));
			String screenName = settings.getString("screenName", "");
		    setTitle(getResources().getText(R.string.Hi) + screenName);
			new MyAsyncTask(this, 0).execute(0);
		}
	}

	public Object load(int id) throws ScilapException {
		ObjectMapper mapper = new ObjectMapper();

		String batchSt = "{"
				+ "\"setup\":{"
					+ "\"cmd\":\"object\","
					+ "\"prm\":"
						+ "{\"token\":\"__TOKEN__\","
						+ "\"repCmdty\":__REPCMDTY__},"
					+ "\"res\":{\"a\":\"merge\"}"
				+ "},"
				+ "\"accounts\":{"
					+ "\"dep\":\"setup\","
					+ "\"cmd\":\"api\","
					+ "\"prm\":[\"cash.getAllAccounts\",\"token\"],"
					+ "\"res\":{\"a\":\"store\",\"v\":\"accounts\"}"
				+ "},"
				+ "\"filter\":{"
					+ "\"dep\":\"accounts\","
					+ "\"cmd\":\"filter\","
					+ "\"prm\":[\"accounts\",\"type\",[\"BANK\",\"CASH\",\"ASSET\",\"STOCK\",\"MUTUAL\",\"CURENCY\",\"CREDIT\",\"LIABILITY\",\"RECEIVABLE\",\"PAYABLE\"],\"IN\"],"
					+ "\"res\":{\"a\":\"store\",\"v\":\"accounts\"}"
				+ "},"
				+ "\"info\":{"
					+ "\"dep\":\"filter\","
					+ "\"cmd\":\"api\","
					+ "\"ctx\":{\"a\":\"each\",\"v\":\"accounts\"},"
					+ "\"prm\":[\"cash.getAccountInfo\",\"token\",\"_id\",[\"value\"]],"
					+ "\"res\":{\"a\":\"merge\"}"
				+ "},"
				+ "\"cmdty\":{"
					+ "\"dep\":\"filter\","
					+ "\"cmd\":\"pluck\","
					+ "\"prm\":[\"accounts\",\"cmdty\",\"unique\"],"
					+ "\"res\":{\"a\":\"clone\",\"v\":\"cmdty\"}"
				+ "},"
				+ "\"rates\":{"
					+ "\"dep\":\"cmdty\","
					+ "\"cmd\":\"api\","
					+ "\"ctx\":{\"a\":\"each\",\"v\":\"cmdty\"},"
					+ "\"prm\":[\"cash.getCmdtyPrice\",\"token\",\"this\",\"repCmdty\",null,\"safe\"],"
					+ "\"res\":{\"a\":\"store\",\"v\":\"rate\"}"
				+ "},"
					+ "\"types\":{"
					+ "\"dep\":\"rates\","
					+ "\"cmd\":\"api\","
					+ "\"prm\":[\"cash.getAssetsTypes\",\"token\"],"
					+ "\"res\":{\"a\":\"store\",\"v\":\"types\"}"
				+ "}"
			+ "}";
		try {
			batchSt = batchSt.replaceAll("__REPCMDTY__", mapper.writeValueAsString(repCmdty));
		} catch (Exception e) {
			throw new ScilapException();
		}
		JsonNode objs = Api.requestBatch(batchSt);
		
		JsonNode sm = objs.get(0);
		
		ArrayNode accs = (ArrayNode) sm.get("accounts");
		Map<String, CmdtyData> cmdtys = createCmdtyMap((ArrayNode)sm.get("cmdty"));
		
		typesInfo = (ArrayNode)sm.get("types");

		List<AssetsData> assets = loadTypes(accs, cmdtys, assetsTypes, getString(R.string.Assets));
		List<AssetsData> liabilities = loadTypes(accs, cmdtys, liabilitiesTypes, getString(R.string.Liabilities));
		assets.addAll(liabilities);
		return assets;
	}
	public JsonNode getTypeInfo(String name) {
		for (JsonNode typeInfo : typesInfo) {
			if (name.equals(typeInfo.get("value").asText()))
				return typeInfo;
		}
		return null;
		
	}
	public List<AssetsData> loadTypes(ArrayNode accs, Map<String, CmdtyData> cmdtys, 
			String types, String name) throws ScilapException {
		List<AssetsData> assets = new ArrayList<AssetsData>();
		for (JsonNode responseAllAccountsData : accs) {
			AssetsData det = new AssetsData();
			det.cmdty = createCmdty(responseAllAccountsData.get("cmdty"));
			det.name = responseAllAccountsData.get("name").asText();
			if (responseAllAccountsData.has("parentId"))
				det.parentId =responseAllAccountsData.get("parentId").asText();
			det._id = responseAllAccountsData.get("_id").asText();
			det.type = responseAllAccountsData.get("type").asText();
			det.value = responseAllAccountsData.get("value").asDouble();
			det.path = det.name;
			if (responseAllAccountsData.has("hidden"))
				det.visible = !"true".equals(responseAllAccountsData.get("hidden"));
			if (responseAllAccountsData.has("placeholder"))
				det.enabled = !"true".equals(responseAllAccountsData.get("placeholder"));
			det.childs = new ArrayList<AssetsData>();
			if (det.visible)
				assets.add(det);
			JsonNode info = getTypeInfo(det.type);
			det.act = info.get("act").asInt();
			det.direction[0] = info.get("recv").asText();
			det.direction[1] = info.get("send").asText();
		}
		AssetsData ad = new AssetsData();
		ad._id = null;
		ad.parentId = null;
		ad.value = 0.0;
		ad.path = "";
		ad.name = name;
		ad.enabled = false;
		ad.header = true;
		ad.childs = new ArrayList<AssetsData>();
		assets.add(ad);
		
		Collections.sort(assets, new Comparator<AssetsData>() {
			public int compare(AssetsData lhs, AssetsData rhs) {
				return lhs.name.compareTo(rhs.name);
			}
		});
		
		for (AssetsData asset : assets)
			if(asset.parentId != null)	
				for (AssetsData a : assets) 
					if(asset.parentId.equals(a._id)) {
						a.childs.add(asset);
						break;
					}
		
		List<AssetsData> assets_ = new ArrayList<AssetsData>();
		for (AssetsData a : assets)
			if (a.parentId == null)
				assets_.add(a);
		
		assets = new ArrayList<AssetsData>();
		addRow(assets_, assets, 0, cmdtys, new ArrayList<AssetsData>(),types);
		return assets;
	}
	protected double addRow(List<AssetsData> assets, List<AssetsData> out, int deep, 
			Map<String, CmdtyData> cmdtys, List<AssetsData> realChilds, String types) {
		double value = 0;
		for (AssetsData a : assets) {
			ArrayList<AssetsData> realChildsFor = new ArrayList<AssetsData>();
			List<AssetsData> childs = a.childs;
			for (AssetsData asset : childs) {
				if (a.path != null && !"".equals(a.path))
					asset.path = a.path + "::" + asset.name;
				else
					asset.path = asset.name;
			}
			a.deep = deep;
			out.add(a);
			if (childs.size() > 0) {
				a.value += addRow(childs, out, deep + 1, cmdtys, realChildsFor,types);
			}
			if ((Math.abs(a.value*100) < 0.5 && realChildsFor.size() == 0) || (!types.contains(a.type) && a.childs.size()==0)) {
				out.remove(a);
				continue;
			}
			realChilds.add(a);
			double rate = 1.0;
			if (a.cmdty != null) {
				CmdtyData cmdty = cmdtys.get(a.cmdty.id);
				if (cmdty != null && cmdty.rate != null)
					rate = cmdty.rate;
			}
			value += a.value*rate;
		}
		return value;
	}

	public void preLoad(int id) {
		dialog = new ProgressDialog(this);
	    dialog.setMessage(getResources().getText(R.string.LoadAccounts));
	    dialog.setIndeterminate(true);
	    dialog.setCancelable(true);
	    dialog.show();
	}

	@SuppressWarnings("unchecked")
	public void afterLoad(AsyncTaskObject obj, int id) {
		if (dialog != null)
			dialog.dismiss();
		if (obj.isError) {
			if (TokenException.class.equals(obj.exception.getClass())) {
	    		Intent myIntent = new Intent(AccountsActivity.this, LoginActivity.class);
	    		AccountsActivity.this.startActivityForResult(myIntent, ActivityInfo.LOGINACTIVITY);
			} else {
				SimpleAlertDlg dlg = new SimpleAlertDlg(this, obj.error, false);
				dlg.show();
			}
			return;
		}
		saveApiInfo();

		assets = (List<AssetsData>)obj.o;
        lv.setAdapter(new AccountsAdapter(this, assets));
		lv.setOnItemClickListener(onItemClickListener);
	}
	protected Map<String, CmdtyData> createCmdtyMap(ArrayNode list) {
		Map<String, CmdtyData> map = new HashMap<String, CmdtyData>();
		for (JsonNode node : list) {
			CmdtyData cmdty = createCmdty(node);
			if (cmdty == null) continue;
			map.put(cmdty.id, cmdty);
		}
		return map;
	}
	protected CmdtyData createCmdty(JsonNode node) {
		CmdtyData cmdty = new CmdtyData();
		if (!node.has("id") || !node.has("space"))
			return null;
		cmdty.id = node.get("id").asText();
		cmdty.space = node.get("space").asText();
		if (node.has("rate"))
			cmdty.rate = node.get("rate").asDouble();
		return cmdty;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.accounts_menu, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.logout:
	    		Intent myIntent = new Intent(AccountsActivity.this, LoginActivity.class);
	    		AccountsActivity.this.startActivityForResult(myIntent, ActivityInfo.LOGINACTIVITY);
//		    	Api.setUrl(null);
		    	Api.setLogin(null);
		    	Api.setPassword(null);
		    	Api.setToken(null);
		    	saveApiInfo();
	            return true;
	        case R.id.relogin:
	        	new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						try {
							Api.reLogin();
						} catch (ScilapException e) {
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						super.onPostExecute(result);
						new MyAsyncTask(AccountsActivity.this, 0).execute(0);						
					}
	        		
	        	}.execute();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	protected void saveApiInfo() {
		//save current api info
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("url", Api.getUrl());
		editor.putString("login", Api.getLogin());
		editor.putString("password", Api.getPassword());
		editor.putString("token", Api.getToken());
		editor.commit();
	}

}
