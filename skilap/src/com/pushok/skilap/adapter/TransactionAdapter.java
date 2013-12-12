package com.pushok.skilap.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pushok.skilap.R;
import com.pushok.skilap.activity.AccsActivity;
import com.pushok.skilap.activity.DetailsActivity;
import com.pushok.skilap.adapter.SplitData.Direction;

import android.content.Intent;
import android.database.DataSetObserver;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class TransactionAdapter implements ListAdapter {
	protected SplitData[] data = new SplitData[1];
    protected LayoutInflater inflater;
    protected DetailsActivity activity;
    protected Map<Integer, View> viewMap;
    
	public
	TransactionAdapter(DetailsActivity activity, List<SplitData> data) {
		this.activity = activity;
    	this.inflater = LayoutInflater.from(activity);
    	this.data = data.toArray(this.data);
    	viewMap = new HashMap<Integer, View>();
    }
	public void updateData(List<SplitData> data) {
    	this.data = data.toArray(new SplitData[1]);
	}
	public int getCount() {
		return data.length;
	}
	public Object getItem(int position) {
		return data[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		return IGNORE_ITEM_VIEW_TYPE;
	}

	abstract class MyTextWatcher implements TextWatcher {
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}
	}
	interface DataSetter {
		public void setData(Integer id, String s);
	}
	class EditTextWithId {
		public EditText et;
		public Integer id;
		public boolean changed = false;
		public EditTextWithId(EditText _et, Integer _id, final DataSetter setter) {
			this.et = _et;
			this.id = _id;
			this.et.addTextChangedListener(new MyTextWatcher() {
				public void afterTextChanged(Editable s) {
					changed = true;
				}
			});
			this.et.setOnFocusChangeListener(new OnFocusChangeListener() {
				public void onFocusChange(View v, boolean hasFocus) {
					if (!changed)
						return;
					changed = false;
					if (!hasFocus)
						setter.setData(id, et.getText().toString());
				}
			});
			this.et.setOnEditorActionListener(new OnEditorActionListener() {
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					if (!changed)
						return false;
					changed = false;
					setter.setData(id, et.getText().toString());
					return false;
				}
			});
		}
		public void setText(String s) {
			et.setText(s);
		}
	}
	class EditAccount {
		public EditText et;
		public Integer id;
		public EditAccount(final EditText et, Integer _id) {
			this.et = et;
			id = _id;
			this.et.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(activity, AccsActivity.class);
					intent.putExtra("position", id);
					activity.startActivityForResult(intent, 1);		
				}
			});
		}
		public void setText(String s) {
			et.setText(s);
		}
	}
	
	public View getView(int position, View convertView,
			ViewGroup parent) {
		final int pos = position;
		if (convertView == null)
			convertView = inflater.inflate(R.layout.split_list_item, null);
		else 
			return convertView;
		viewMap.put(position, convertView);
		
		Spinner spiner = (Spinner)convertView.findViewById(R.id.spinner);
		spiner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int i, long arg3) {
				if (data.length == 2) {
					if (i == 0 ^ pos != 0) {
						if (data[0].direction == Direction.Spent)
							return;
						data[0].direction = Direction.Spent;
						data[1].direction = Direction.Received;
					} else {
						if (data[0].direction == Direction.Received)
							return;
						data[0].direction = Direction.Received;
						data[1].direction = Direction.Spent;
					}
					activity.lvSplits.invalidateViews();
					return;
				}
				
				if (i == 0)
					data[pos].direction = Direction.Spent;
				else
					data[pos].direction = Direction.Received;
			}
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		EditAccount etAccount = new EditAccount((EditText)convertView.findViewById(R.id.etAccount),
				position);
		EditTextWithId etValue = new EditTextWithId((EditText)convertView.findViewById(R.id.etValue),
				position, new DataSetter() {
					public void setData(Integer id, String s) {
						try {
							data[id].value = Double.valueOf(s);
							if (data.length == 2) {
								data[0].value = data[1].value = data[id].value;
								EditText et = (EditText)viewMap.get(0).findViewById(R.id.etValue);
								et.setText(data[id].value.toString());
								et = (EditText)viewMap.get(1).findViewById(R.id.etValue);
								et.setText(data[id].value.toString());
							}
						} catch (Exception e) {
//							data[id].value = 0.0;
						}
					}
				});
		
		etAccount.setText(data[position].accPath);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, activity.direction);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spiner.setAdapter(spinnerArrayAdapter);
		spiner.setSelection(data[position].direction.ordinal());
		if (data[position].value != 0)
			etValue.setText(data[position].value.toString());
		etAccount.et.setFocusable(false);
		
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
