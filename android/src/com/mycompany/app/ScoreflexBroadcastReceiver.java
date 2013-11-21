package com.mycompany.app;

import com.scoreflex.Scoreflex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScoreflexBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("Test", "received broadcast");
		if (CoronaApplication.isActivityVisible()) {
			return;
		}
		Log.d("Test", "activity not visible");
		if (Scoreflex.onBroadcastReceived(context, intent, R.drawable.icon, MainActivity.class)) {
			return;
		}
	}

}
