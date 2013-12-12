package com.pushok.skilap.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class SimpleAlertDlg extends AlertDialog {
	protected Activity activity;
	protected boolean close;
	protected SimpleAlertDlg(Activity _activity, String msg, boolean _close) {
		super(_activity);
		activity = _activity;
		close = _close;
		setCancelable(false);
		setMessage(msg);
		setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (close)
					activity.finish();
				dialog.dismiss();
			}
		});
		}

}
