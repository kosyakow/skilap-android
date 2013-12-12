package com.pushok.skilap.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import com.pushok.skilap.R;
import com.pushok.skilap.adapter.DetailsAdapter;
import com.pushok.skilap.adapter.SplitData;
import com.pushok.skilap.adapter.TransactionAdapter;
import com.pushok.skilap.adapter.SplitData.Direction;
import com.pushok.skilap.apiData.Api;
import com.pushok.skilap.apiData.SaveSplitsObjectData;
import com.pushok.skilap.exception.ScilapException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

public class DetailsActivity extends Activity implements ApiLoader {
	protected class DetailsInfo {
		DetailsInfo(ArrayNode transactions, ArrayNode accRegs, Map<String, JsonNode> accInfo) {
			this.transactions = transactions;
			this.accRegs = accRegs;
			this.accInfo = accInfo;
		}
		public ArrayNode transactions;
		public ArrayNode accRegs;
		public Map<String, JsonNode> accInfo;
	}
	private String accId;
	private ProgressDialog dialog;
	private MyAsyncTask task;
	private String accPath;
	private Double value;
	private String cmdty;
	private DetailsInfo info;
	private List<SplitData> splits;
	private TransactionAdapter adapter;
	public ListView lvSplits;
	private EditText etDate;
	private EditText etMemo;
	public String[] direction;
	private TabHost tabhost;
	private String trnId = null;
	Map<String, JsonNode> accInfo;
	public int act;
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat jsformat = new SimpleDateFormat("MM/dd/yy");
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat parse = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	private class OnDateClickListener implements OnClickListener {
		protected Context context;
		protected EditText edit;
		public OnDateClickListener(Context context, EditText edit) {
			this.context = context;
			this.edit = edit;
		}
		public void onClick(View v) {
			Date date = new Date();
			try {
				date = format.parse(edit.getText().toString());
			} catch (Exception e) { Log.e("Transaction", "Error date parsing"); }
			DatePickerDialog dlg = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					Date date = new Date(year, monthOfYear, dayOfMonth);
					edit.setText(format.format(date));
				}
			}, date.getYear() + 1900, date.getMonth(), date.getDate());
			dlg.show();
		}
	}
	@Override
	public Object onRetainNonConfigurationInstance() {
		return splits;
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("date", etDate.getText().toString());
		outState.putString("memo", etMemo.getText().toString());
		super.onSaveInstanceState(outState);
	}
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
        etDate.setText(savedInstanceState.getString("date"));
        etMemo.setText(savedInstanceState.getString("memo"));
		super.onRestoreInstanceState(savedInstanceState);
	}
	@SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = null;
        setContentView(R.layout.details);

        tabhost = (TabHost)findViewById(R.id.tabhost);
        tabhost.setup();
        tabhost.addTab(tabhost.newTabSpec("tab1").setIndicator(getResources().getText(R.string.New)).setContent(R.id.tab1));
        tabhost.addTab(tabhost.newTabSpec("tab2").setIndicator(getResources().getText(R.string.Recent)).setContent(R.id.tab2));

		Bundle bundle = this.getIntent().getExtras();
		accId = bundle.getString("data");
		accPath = bundle.getString("path");
		value = bundle.getDouble("value");
		cmdty = bundle.getString("cmdty");
		act = bundle.getInt("act");
		direction = bundle.getStringArray("direction");
		
		setTitle(accPath + " - " + MyNumberFormat.getCurrencyInstance(cmdty).format(value));

        tabhost.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				if (tabId.equals("tab2")) {
				    if (info != null) {
				        ListView lvDetails = (ListView)findViewById(R.id.lvDetails);
				        lvDetails.setAdapter(new DetailsAdapter(DetailsActivity.this, info.transactions, info.accRegs, info.accInfo, cmdty));
				    } else {
				    	task = new MyAsyncTask(DetailsActivity.this, 0);
				    	task.execute(0);
				    }
				}
			}
		});

        Button buttonAdd = (Button)findViewById(R.id.btAddSplit);
        buttonAdd.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
		        SplitData split = new SplitData();
		        split.accId = null;
		        split.accPath = "";
		        double sum = 0.0;
				for (int i = 0; i<splits.size(); i++)
					if (splits.get(i).direction == Direction.Spent)
						sum -= splits.get(i).value;
					else
						sum += splits.get(i).value;
				if (sum < 0) {
					split.direction = Direction.Received;
					split.value = -sum;
				} else {
					split.direction = Direction.Spent;
					split.value = sum;
				}
				splits.add(split);
				adapter.updateData(splits);
				lvSplits.invalidateViews();
			}
		});
        
        Button buttonSave = (Button)findViewById(R.id.btSave);
        buttonSave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
		    	task = new MyAsyncTask(DetailsActivity.this, 1);
		    	task.execute(0);
			}
		});
        
        etDate = (EditText)findViewById(R.id.etDate);
        etDate.setFocusable(false);
        etDate.setOnClickListener(new OnDateClickListener(etDate.getContext(), etDate));
       	etDate.setText(format.format(new Date()));
        
        etMemo = (EditText)findViewById(R.id.etMemo);

		splits = (List<SplitData>) getLastNonConfigurationInstance();
		if (splits == null) {
			splits = new ArrayList<SplitData>();
	        SplitData split1 = new SplitData();
	        split1.accId = accId;
	        split1.accPath = accPath;
	        split1.direction = Direction.Spent;
	        split1.value = 0.0;
	        splits.add(split1);
	        SplitData split2 = new SplitData();
	        split2.accId = null;
	        split2.accPath = "";
	        split2.direction = Direction.Received;
	        split2.value = 0.0;
	        splits.add(split2);
		}
        lvSplits = (ListView)findViewById(R.id.lvSplits);
        adapter = new TransactionAdapter(this, splits);
        lvSplits.setAdapter(adapter);
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
		if(resultCode==RESULT_OK && requestCode==1){
			String id = data.getStringExtra("retId");
			String path = data.getStringExtra("retPath");
			Integer position = data.getIntExtra("position", -1);
			if (path == null) path = "";
			if (position >= 0) {
				if (position == 0) {
					direction = data.getStringArrayExtra("direction");
					act = data.getIntExtra("act", 1);
				}
				SplitData split = splits.get(position);
				split.accId = id;
				split.accPath = path;
				adapter.updateData(splits);
				lvSplits.invalidateViews();
			}
		}
	}
	
	public Object load(int id) throws ScilapException {
		if (id == 0) {
			String batchSt = 
				"{" +
					"\"setup\":{" + 
						"\"cmd\":\"object\"," +
						"\"prm\":{\"token\":\"__TOKEN__\",\"accId\":\"__ACCID__\"}," +
						"\"res\":{\"a\":\"merge\"}" +
					"}," + 
					"\"info1\":{" +
						"\"dep\":\"setup\"," +
						"\"cmd\":\"api\"," +
						"\"prm\":[\"cash.getAccountInfo\",\"token\",\"accId\",[\"value\"]]," +
						"\"res\":{\"a\":\"store\",\"v\":\"accValue\"}" +
					"}," +
					"\"accounts\":{" +
							"\"dep\":\"setup\"," +
							"\"cmd\":\"api\"," +
							"\"prm\":[\"cash.getAccountRegister\",\"token\",\"accId\",0,-6]," +
							"\"res\":{\"a\":\"store\",\"v\":\"accRegs\"}" +
						"}," +
					"\"transaction\":{" +
							"\"dep\":\"accounts\"," +
							"\"cmd\":\"api\"," +
							"\"ctx\":{\"a\":\"each\",\"v\":\"accRegs\"}," +
							"\"prm\":[\"cash.getTransaction\",\"token\",\"_id\"]," +
							"\"res\":{\"a\":\"store\",\"v\":\"transactions\"}" +
						"}," +
					"\"aids\":{" +
							"\"dep\":\"accounts\"," +
							"\"cmd\":\"pluck\"," +
							"\"prm\":[\"accRegs\",\"recv\"]," +
							"\"res\":{\"a\":\"store\",\"v\":\"aids\"}" +
						"}," +
					"\"flatten\":{" +
						"\"dep\":\"aids\"," +
						"\"cmd\":\"flatten\"," +
						"\"prm\":[\"aids\"]," +
						"\"res\":{\"a\":\"clone\",\"v\":\"aids\"}" +
					"}," +
					"\"info\":{" +
							"\"dep\":\"flatten\"," +
							"\"cmd\":\"api\"," +
							"\"ctx\":{\"a\":\"each\",\"v\":\"aids\"}," +
							"\"prm\":[\"cash.getAccountInfo\",\"token\",\"accountId\",[\"path\"]]," +
							"\"res\":{\"a\":\"merge\"}" +
						"}" +
	
				"}";
				
			batchSt = batchSt.replaceAll("__ACCID__", accId);
			ArrayNode objs = (ArrayNode) Api.requestBatch(batchSt);
			
			JsonNode sm = objs.get(0);
			JsonNode accValue = sm.get("accValue");
			value = accValue.get("value").asDouble();
			
			ArrayNode accRegs = (ArrayNode)sm.get("accRegs");
			ArrayNode transactions = new ArrayNode(JsonNodeFactory.instance);
			ArrayNode accI = (ArrayNode)sm.get("aids");
			for (JsonNode stringMap : accRegs)
				transactions.add(stringMap.get("transactions"));
			
			accInfo = new HashMap<String, JsonNode>();
			ObjectNode cur = new ObjectNode(JsonNodeFactory.instance);
			cur.put("_id", accId);
			cur.put("path", accPath);
			accInfo.put(accId, cur);
			for (JsonNode stringMap : accI)
				accInfo.put(stringMap.get("_id").asText(), stringMap);
	
			info = new DetailsInfo(transactions, accRegs, accInfo);
			return info;
		} else {
			SaveSplitsObjectData transaction = new SaveSplitsObjectData(splits.size());
			if (trnId != null)
				transaction._id = trnId;
			try {
				transaction.datePosted = jsformat.format(format.parse(etDate.getText().toString()));
			} catch (ParseException e) {
				transaction.datePosted = jsformat.format(new Date());
			}
			transaction.dateEntered = jsformat.format(new Date());
			transaction.description = ((EditText)findViewById(R.id.etMemo)).getText().toString();
			for (int i = 0; i<splits.size(); i++) {
				transaction.splits[i].accountId = splits.get(i).accId;
				if (splits.get(i).direction != Direction.Spent)
					transaction.splits[i].quantity = splits.get(i).value;
				else 
					transaction.splits[i].quantity = splits.get(i).value*-1;
			}
			return Api.saveTransaction(transaction, accId);
		}
	}
	
	public void preLoad(int id) {
		dialog = new ProgressDialog(this);
		if (id == 0)
			dialog.setMessage(getResources().getText(R.string.LoadDetails));
		else
			dialog.setMessage(getString(R.string.SaveTransaction));
	    dialog.setIndeterminate(true);
	    dialog.setCancelable(true);
	    dialog.show();
	}

	public void afterLoad(AsyncTaskObject o, int id) {
		if (dialog != null)
			dialog.dismiss();
		if (id == 0) {
		if (o.isError) {
			new SimpleAlertDlg(this, o.error, false).show();
			return;
		}
        ListView lvDetails = (ListView)findViewById(R.id.lvDetails);
        lvDetails.setAdapter(new DetailsAdapter(this, info.transactions, info.accRegs, info.accInfo, cmdty));
		setTitle(accPath + " - " + MyNumberFormat.getCurrencyInstance(cmdty).format(value));
		} else {
			if (o.isError) {
				AlertDialog ad = new AlertDialog.Builder(this).create();
				ad.setCancelable(false);
				ad.setMessage("Error");
				ad.setButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
			    	    Intent intent = getIntent();
			    	    setResult(RESULT_OK, intent);
			    	    finish();
					}
				});
				ad.show();
			} else {
	    	    Intent intent = getIntent();
	    	    intent.putExtra("Refresh", true);
	    	    setResult(RESULT_OK, intent);
	    	    finish();
			}
		}
	}
	
	public void showTransaction(JsonNode trn) {
		tabhost.setCurrentTab(0);
	    TextView tv = (TextView) tabhost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
	    tv.setText(getString(R.string.Edit));
	    ArrayNode spls = (ArrayNode) trn.get("splits");
		trnId = trn.get("_id").asText();
		String d = trn.get("dateEntered").asText();
		Date date;
		try {
			date = parse.parse(d);
		} catch (Exception e) {
			try {
				date = new Date(Date.parse(d));
			} catch (Exception e1) {
				date = null;
			}
		}
		if (date == null)
			etDate.setText(d);
		else
			etDate.setText(format.format(date));
		if (trn.get("description") != null)
			etMemo.setText(trn.get("description").asText());
		splits = new ArrayList<SplitData>();
		for (JsonNode jsonNode : spls) {
	        SplitData split = new SplitData();
	        split.accId = jsonNode.get("accountId").asText();
	        split.accPath = accInfo.get(jsonNode.get("accountId").asText()).get("path").asText();
	        split.value =jsonNode.get("value").asDouble();
	        if (split.value < 0) {
	        	split.direction = Direction.Spent;
	        	split.value = Math.abs(split.value);
	        } else
	        	split.direction = Direction.Received;
	        splits.add(split);
		}
        lvSplits = (ListView)findViewById(R.id.lvSplits);
        adapter = new TransactionAdapter(this, splits);
        lvSplits.setAdapter(adapter);
	}
}