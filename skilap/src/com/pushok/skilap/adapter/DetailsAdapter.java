package com.pushok.skilap.adapter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;

import com.pushok.skilap.R;
import com.pushok.skilap.activity.DetailsActivity;
import com.pushok.skilap.activity.MyNumberFormat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class DetailsAdapter implements ListAdapter {
	private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
	private SimpleDateFormat parse = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	protected ArrayNode transactions = null;
	protected ArrayNode register = null;
	protected Map<String, JsonNode> accInfo;
    protected LayoutInflater inflater;
	protected NumberFormat currFmt;
	protected DetailsActivity activity;
    
	public DetailsAdapter(DetailsActivity ctx, ArrayNode transactions, 
			ArrayNode register, Map<String, JsonNode> accInfo, String cmdty) {
    	this.inflater = LayoutInflater.from(ctx);
    	this.transactions = transactions;
    	this.register = register;
    	this.accInfo = accInfo;
    	currFmt = MyNumberFormat.getCurrencyInstance(cmdty);
    	activity = ctx;
    }
	public int getCount() {
		return transactions.size();
	}
	public Object getItem(int position) {
		return transactions.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		return IGNORE_ITEM_VIEW_TYPE;
	}

	public View getView(int position, View convertView,
			ViewGroup parent) {
		position = transactions.size() - position - 1;
		if (convertView == null)
			convertView = inflater.inflate(R.layout.details_list_item, null);
		TextView tvPath = (TextView)convertView.findViewById(R.id.tvPath);
		TextView tvDate = (TextView)convertView.findViewById(R.id.tvDate);
		TextView tvDesc = (TextView)convertView.findViewById(R.id.tvDesc);
		TextView tvCash = (TextView)convertView.findViewById(R.id.tvCash);
		
		ArrayNode recv = (ArrayNode)register.get(position).get("recv");
		if (recv.size() == 1)
			tvPath.setText(accInfo.get(recv.get(0).get("accountId").asText()).get("path").asText());
		else
			tvPath.setText("-- Multiple --");
		
		final JsonNode trNode = transactions.get(position); 
		String d = trNode.get("dateEntered").asText();
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
			tvDate.setText(d);
		else
			tvDate.setText(format.format(date));
		
		
		String desc = "";
		if (trNode.get("description")!=null)
			trNode.get("description").asText();
		tvDesc.setText(desc);
		
		JsonNode val = register.get(position).get("send");
		tvCash.setText(currFmt.format(val.get("quantity").asDouble()));
		
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.showTransaction(trNode);
			}
		});
		return convertView;
	}

	public int getViewTypeCount() {
		return 1;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isEmpty() {
		return false;
	}

	public void registerDataSetObserver(DataSetObserver observer) {
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
	}

	public boolean areAllItemsEnabled() {
		return true;
	}

	public boolean isEnabled(int position) {
		return true;
	}
	
}
