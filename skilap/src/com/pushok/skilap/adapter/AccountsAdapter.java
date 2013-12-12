package com.pushok.skilap.adapter;

import java.text.NumberFormat;
import java.util.List;

import com.pushok.skilap.R;
import com.pushok.skilap.activity.MyNumberFormat;
import com.pushok.skilap.apiData.AssetsData;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class AccountsAdapter implements ListAdapter, SectionIndexer {
	protected AssetsData[] assets = new AssetsData[1];
    protected LayoutInflater inflater;
    
    public AccountsAdapter(Context ctx, List<AssetsData> assets) {//
    	this.inflater = LayoutInflater.from(ctx);
    	this.assets = assets.toArray(this.assets);
    }
	public int getCount() {
		if (assets.length == 1 && assets[0] == null)
			return 0;
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
			convertView = inflater.inflate(R.layout.accounts_list_item, null);
		TextView tvText = (TextView)convertView.findViewById(R.id.tvText);
		TextView tvValue = (TextView)convertView.findViewById(R.id.tvValue);
		String name = assets[position].name;
		for (int i = 0; i < assets[position].deep; i++)
			name = "   " + name;
		tvText.setText(name);
		NumberFormat currFmt = MyNumberFormat.getCurrencyInstance(assets[position].cmdty);
		tvValue.setText(currFmt.format(assets[position].value));
		if (!assets[position].enabled) {
			tvText.setTextColor(Color.LTGRAY);
			tvValue.setTextColor(Color.LTGRAY);
		}
		if (assets[position].header) {
			convertView.setBackgroundColor(Color.DKGRAY);
		}
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
	public int getPositionForSection(int section) {
		if (section == 0) return 0;
		return 5;
	}
	public int getSectionForPosition(int position) {
		if (position < 5) return 0;
		return 1;
	}
	public Object[] getSections() {
		return new String[] {"Test1", "Test2"};
	}
	
}
