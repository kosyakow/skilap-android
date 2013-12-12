package com.pushok.skilap.adapter;

import java.util.List;

import com.pushok.skilap.R;
import com.pushok.skilap.apiData.AssetsData;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

public class AccsAdapter implements ListAdapter {
	protected AssetsData[] assets = new AssetsData[1];
    protected LayoutInflater inflater;
    
    public AccsAdapter(Context ctx, List<AssetsData> assets) {//
    	this.inflater = LayoutInflater.from(ctx);
    	this.assets = assets.toArray(this.assets);
    }
	public int getCount() {
		return assets.length;
	}
	public Object getItem(int position) {
		return assets[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		return IGNORE_ITEM_VIEW_TYPE;
	}

	public View getView(int position, View convertView,
			ViewGroup parent) {
		if (convertView == null)
			convertView = inflater.inflate(R.layout.accs_list_item, null);
		TextView tvText = (TextView)convertView.findViewById(R.id.tvText);
		tvText.setText(assets[position].path);
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
