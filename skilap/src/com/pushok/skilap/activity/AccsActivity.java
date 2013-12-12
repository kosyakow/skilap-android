package com.pushok.skilap.activity;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import com.pushok.skilap.R;
import com.pushok.skilap.adapter.AccsAdapter;
import com.pushok.skilap.apiData.Api;
import com.pushok.skilap.apiData.AssetsData;
import com.pushok.skilap.apiData.CmdtyData;
import com.pushok.skilap.exception.ApiException;
import com.pushok.skilap.exception.ScilapException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class AccsActivity extends Activity implements ApiLoader {
    private String FILENAME = "skilap";
    private ListView lvAll;
    private ListView lvLast;
	private LinkedList<AssetsData> last;
	private boolean bLoaded = false;
	private ProgressDialog dialog;
	private MyAsyncTask task;
	private List<AssetsData> data = null;
	ArrayNode types;
	JsonNode type = null;
	ArrayNode currencies;
	JsonNode currency = null;
	AssetsData parent = null;
	EditText etName;
	EditText etParent;
	EditText etCurrency;
	EditText etType;

	protected class MyItemClickListener implements OnItemClickListener {
    	public void onItemClick(AdapterView<?> parent, View view,
	    		int position, long id) {
	    		AccsAdapter adapter = (AccsAdapter)parent.getAdapter();
	    		AssetsData data = (AssetsData) adapter.getItem((int) id);
	    		save(data);
	    	    final Intent intent = getIntent();
	    	    intent.putExtra("retId", data._id);
	    	    intent.putExtra("retPath", data.path);
	    	    intent.putExtra("act", data.act);
	    	    intent.putExtra("direction", data.direction);
	    	    setResult(RESULT_OK, intent);
	    	    finish();
	    	}
	}
	@Override
	public Object onRetainNonConfigurationInstance() {
		return data;
	}
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.accs);

        TabHost tabhost = (TabHost)findViewById(R.id.tabhost);
        tabhost.setup();
        tabhost.addTab(tabhost.newTabSpec("tab1").setIndicator(getResources().getText(R.string.Recent)).setContent(R.id.tab1));
        tabhost.addTab(tabhost.newTabSpec("tab2").setIndicator(getResources().getText(R.string.All)).setContent(R.id.tab2));
        tabhost.addTab(tabhost.newTabSpec("tab3").setIndicator(getResources().getText(R.string.New)).setContent(R.id.tab3));

        lvAll = (ListView)findViewById(R.id.lvAll);
        lvLast = (ListView)findViewById(R.id.lvLast);
        lvAll.setOnItemClickListener(new MyItemClickListener());
        lvLast.setOnItemClickListener(new MyItemClickListener());
	    
		last = new LinkedList<AssetsData>();
        try {
        	FileInputStream fis = openFileInput(FILENAME);
        	InputStreamReader reader = new InputStreamReader(fis);
    		ObjectMapper mapper = new ObjectMapper();
    		AssetsData[] data = mapper.readValue(reader, AssetsData[].class);
        	for (AssetsData assetsData : data)
        		last.add(assetsData);
    		lvLast.setAdapter(new AccsAdapter(this, last));
        	fis.close();
        } catch (Exception e) {
        	Log.d("TransactionActivity", "Last accounts read error");
        }

        tabhost.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				if (tabId.equals("tab2") && !bLoaded)
				    try {
				    	task = new MyAsyncTask(AccsActivity.this, 0);
				    	task.execute(0);
			        } catch (Exception e) {
			        	Log.e("Error", e.toString());
			        }
				if (tabId.equals("tab3") && !bLoaded)
				    try {
				    	task = new MyAsyncTask(AccsActivity.this, 0);
				    	task.execute(0);
			        } catch (Exception e) {
			        	Log.e("Error", e.toString());
			        }
			}
		});
        
        etName = (EditText)findViewById(R.id.etName);
        
        etType = (EditText)findViewById(R.id.etType);
        etType.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final CharSequence items[] = new String[types.size()];
				for (int i = 0; i<types.size(); i++) {
					items[i] = types.get(i).get("value").asText();
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(AccsActivity.this);
				builder.setTitle(getString(R.string.SelectType));
				builder.setItems(items, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	etType.setText(items[item]);
				    	type = types.get(item);
				    }
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
        
        etCurrency = (EditText)findViewById(R.id.etCurrency);
        etCurrency.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final CharSequence items[] = new String[currencies.size()];
				for (int i = 0; i<currencies.size(); i++) {
					items[i] = currencies.get(i).get("country").asText() + " - " + 
							currencies.get(i).get("name").asText();
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(AccsActivity.this);
				builder.setTitle(getString(R.string.SelectCurrency));
				builder.setItems(items, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	etCurrency.setText(items[item]);
				    	currency = currencies.get(item);
				    }
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
        
        etParent = (EditText)findViewById(R.id.etParent);
        etParent.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final CharSequence items[] = new String[data.size()];
				for (int i = 0; i<data.size(); i++) {
					items[i] = data.get(i).path;
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(AccsActivity.this);
				builder.setTitle(getString(R.string.SelectParent));
				builder.setItems(items, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	etParent.setText(items[item]);
				    	parent = data.get(item);
				    }
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
        
        Button buttonSave = (Button)findViewById(R.id.btSave);
        buttonSave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
		    	task = new MyAsyncTask(AccsActivity.this, 1);
		    	task.execute(0);
			}
		});
        
		data = (List<AssetsData>)getLastNonConfigurationInstance();
		if (data != null) {
			lvAll.setAdapter(new AccsAdapter(this, data));
			bLoaded = true;
		}
        if (last.size() == 0)
	        tabhost.setCurrentTab(1);
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
	
	private void save(AssetsData data) {
		boolean bFound = false;
		for (AssetsData ad : last) {
			if (data._id.equals(ad._id)) {
				ad.count++;
				bFound = true;
			}
		}
		if (!bFound) {
			data.count = 1;
			last.addFirst(data);
		}
		while (last.size() > 15)
			last.removeLast();

		try {
        	FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_WORLD_WRITEABLE);
        	OutputStreamWriter writer = new OutputStreamWriter(fos);
    		ObjectMapper mapper = new ObjectMapper();
    		mapper.writeValue(writer, last.toArray());
        	writer.flush();
        	fos.close();
		} catch (Exception e) {
        	Log.d("TransactionActivity", "Last accounts write error");
		}
	}
	
	public Object load(int id) throws ScilapException {
		if (id == 0) {
			String batchSt = 
				"{" +
					"\"setup\":{" + 
						"\"cmd\":\"object\"," +
						"\"prm\":{\"token\":\"__TOKEN__\"}," +
						"\"res\":{\"a\":\"merge\"}" +
					"}," + 
					"\"accounts\":{" +
							"\"dep\":\"setup\"," +
							"\"cmd\":\"api\"," +
							"\"prm\":[\"cash.getAllAccounts\",\"token\"]," +
							"\"res\":{\"a\":\"store\",\"v\":\"accounts\"}" +
						"}," +
					"\"info\":{" +
							"\"dep\":\"accounts\"," +
							"\"cmd\":\"api\"," +
							"\"ctx\":{\"a\":\"each\",\"v\":\"accounts\"}," +
							"\"prm\":[\"cash.getAccountInfo\",\"token\",\"_id\",[\"path\"]]," +
							"\"res\":{\"a\":\"store\",\"v\":\"info\"}" +
					"}," +
					"\"types\":{" +
						"\"dep\":\"accounts\"," +
						"\"cmd\":\"api\"," +
						"\"prm\":[\"cash.getAssetsTypes\",\"token\"]," +
						"\"res\":{\"a\":\"store\",\"v\":\"types\"}" +
						"}," +
					"\"currencies\":{" +
						"\"dep\":\"accounts\"," +
						"\"cmd\":\"api\"," +
						"\"prm\":[\"cash.getAllCurrencies\",\"token\"]," +
						"\"res\":{\"a\":\"store\",\"v\":\"currencies\"}" +
					"}" +
				"}";
				
			JsonNode objs = Api.requestBatch(batchSt);
			JsonNode sm = objs.get(0);
			List<AssetsData> data = new ArrayList<AssetsData>();
			ArrayNode info = (ArrayNode)sm.get("accounts");
			types = (ArrayNode)sm.get("types");
			currencies = (ArrayNode)sm.get("currencies");
			for (JsonNode obj : info) {
				AssetsData d = new AssetsData();
				JsonNode i = obj.get("info");
				d.name = obj.get("type").asText();
				for (JsonNode typeInfo : types) {
					if (d.name.equals(typeInfo.get("value").asText())) {
						d.act = typeInfo.get("act").asInt();
						d.direction[0] = typeInfo.get("recv").asText();
						d.direction[1] = typeInfo.get("send").asText();
						break;
					}
				}
				d.path = i.get("path").asText();
				d._id = i.get("_id").asText();
				data.add(d);
			}
			Collections.sort(data, new Comparator<AssetsData>() {
				public int compare(AssetsData lhs, AssetsData rhs) {
					if (lhs == null || rhs == null)
						return 0;
					if (lhs.path == null)
						return 0;
					return lhs.path.compareTo(rhs.path);
				}
			});
			this.data = data;
			return data;
		}
		if (id == 1) {
			String batchSt = 
					"{" +
						"\"setup\":{" + 
							"\"cmd\":\"object\"," +
							"\"prm\":{\"token\":\"__TOKEN__\",\"account\":__ACCOUNT__}," +
							"\"res\":{\"a\":\"merge\"}" +
						"}," + 
						"\"save\":{" +
								"\"dep\":\"setup\"," +
								"\"cmd\":\"api\"," +
								"\"prm\":[\"cash.saveAccount\",\"token\",\"account\"]," +
								"\"res\":{\"a\":\"store\",\"v\":\"acc\"}" +
						"}," +
						"\"types\":{" +
							"\"dep\":\"save\"," +
							"\"cmd\":\"api\"," +
							"\"prm\":[\"cash.getAssetsTypes\",\"token\"]," +
							"\"res\":{\"a\":\"store\",\"v\":\"types\"}" +
						"}" +
					"}";
			AssetsData newAcc = new AssetsData();
			newAcc.name = etName.getText().toString();
			if (newAcc.name == null || type == null || currency == null ||
					parent == null)
				throw new ApiException("Wrong values");
			newAcc.parentId = parent._id;
			newAcc.type = type.get("value").asText();
			newAcc.cmdty = new CmdtyData(currency.get("iso").asText(), "ISO4217");
	        CheckBox cbHidden = (CheckBox)findViewById(R.id.cbHidden);
			newAcc.hidden = cbHidden.isChecked(); 
	        CheckBox cbPlaceHolder = (CheckBox)findViewById(R.id.cbPlaceHolder);
			newAcc.placeholder = cbPlaceHolder.isChecked(); 
    		ObjectMapper mapper = new ObjectMapper();
    		String accSt;
			try {
				accSt = mapper.writeValueAsString(newAcc);
			} catch (Exception e) {
				return null;
			}
			batchSt = batchSt.replaceAll("__ACCOUNT__", accSt);	
			JsonNode objs = Api.requestBatch(batchSt);
			JsonNode sm = objs.get(0);
			return sm;
		}
		return null;
	}
	
	public void preLoad(int id) {
		dialog = new ProgressDialog(this);
	    dialog.setMessage(getResources().getText(R.string.LoadAccounts));
	    dialog.setIndeterminate(true);
	    dialog.setCancelable(true);
	    dialog.show();
	}

	@SuppressWarnings("unchecked")
	public void afterLoad(AsyncTaskObject o, int id) {
		if (dialog != null)
			dialog.dismiss();
		if (o.isError) {
			new SimpleAlertDlg(this, o.error, false).show();
			return;
		}
		if (id == 0) {
			data = (List<AssetsData>)o.o;
			lvAll.setAdapter(new AccsAdapter(this, data));
			bLoaded = true;
		}
		if (id == 1) {
			JsonNode sm = (JsonNode) o.o;
			JsonNode newAcc = sm.get("acc");
			ArrayNode types = (ArrayNode) sm.get("types");
    	    Intent intent = getIntent();
    	    intent.putExtra("retId", newAcc.get("_id").asText());
    	    String retPath = newAcc.get("name").asText();
    	    if (parent.path != null && parent.path.length() > 0)
    	    	retPath = parent.path + "::" + retPath;
    	    intent.putExtra("retPath", retPath);
    	    
    	    String type = newAcc.get("type").asText();
    	    for (JsonNode jsonNode : types) {
				if (type.equals(jsonNode.get("value").asText())) {
		    	    intent.putExtra("act", jsonNode.get("act").asInt());
		    	    String[] direction = new String[2];
					direction[0] = jsonNode.get("recv").asText();
					direction[1] = jsonNode.get("send").asText();
		    	    intent.putExtra("direction", direction);
					break;
				}
			}
    	    
    	    setResult(RESULT_OK, intent);
    	    finish();
		}
	}

}
