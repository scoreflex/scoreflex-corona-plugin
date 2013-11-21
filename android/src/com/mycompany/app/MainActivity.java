package com.mycompany.app;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import com.scoreflex.*;

import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.webkit.ValueCallback;
import android.net.Uri;
import android.os.Bundle;

public class MainActivity extends com.ansca.corona.CoronaActivity {

	@Override
	protected void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		Scoreflex.registerNetworkReceiver(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Scoreflex.onActivityResult(this, requestCode, resultCode, data);
	}

	public void checkNotification() {
		Intent intent = getIntent();
		Scoreflex.onCreateMainActivity(this, intent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    Scoreflex.unregisterNetworkReceiver(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		CoronaApplication.activityResumed();
	}

	@Override
	protected void onPause() {
		super.onPause();
		CoronaApplication.activityPaused();
	}

	@Override
	public void onBackPressed() {
		if (Scoreflex.backButtonPressed() == false) {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& Scoreflex.backButtonPressed() == true) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}