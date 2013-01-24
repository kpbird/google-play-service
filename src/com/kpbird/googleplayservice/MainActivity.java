package com.kpbird.googleplayservice;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends Activity {

	private TextView txtAccount;
	private TextView txtToken;
	private static final int USER_RECOVERABLE_AUTH = 5;
	private static final int ACCOUNT_PICKER = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		txtAccount = (TextView) findViewById(R.id.txtAccount);
		txtToken = (TextView) findViewById(R.id.txtToken);
	}

	public void buttonClicked(View v) {
		if (v.getId() == R.id.btnCheckService) {
			checkStatus();
		} else if (v.getId() == R.id.btnAccountNames) {
			getAccountNames();
		} else if (v.getId() == R.id.btnGetToken) {
			new GetAuthToken(this, txtAccount.getText().toString()).execute();
		} else if(v.getId() == R.id.btnInvalidate){
			GoogleAuthUtil.invalidateToken(this, txtToken.getText().toString());
		}
		
	}

	public void checkStatus() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		switch (status) {
		case ConnectionResult.SUCCESS:
			Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
			break;
		case ConnectionResult.SERVICE_MISSING:
			Toast.makeText(this, "Service Missing", Toast.LENGTH_SHORT).show();
			break;
		case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
			Toast.makeText(this, "Service Version Update Required",
					Toast.LENGTH_SHORT).show();
			break;
		case ConnectionResult.SERVICE_DISABLED:
			Toast.makeText(this, "Service Disabled", Toast.LENGTH_SHORT).show();
			break;
		}
	}

	private void getAccountNames() {
		Intent intent = AccountPicker.newChooseAccountIntent(null, null,
				new String[] { "com.google" }, false, null, null, null, null);
		startActivityForResult(intent, ACCOUNT_PICKER);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACCOUNT_PICKER && resultCode == RESULT_OK) {
			String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			txtAccount.setText(accountName);
			
		} else if (requestCode == USER_RECOVERABLE_AUTH && resultCode == RESULT_OK) {
			new GetAuthToken(this, txtAccount.getText().toString()).execute();
		} else if (requestCode == USER_RECOVERABLE_AUTH && resultCode == RESULT_CANCELED) {
			Toast.makeText(this, "User rejected authorization.",
					Toast.LENGTH_SHORT).show();
		}
	}

	class GetAuthToken extends AsyncTask<Void, Void, String> {

		private MainActivity mActivity;
		private String mEmail;

		public GetAuthToken(MainActivity mActivity, String mEmail) {
			this.mActivity = mActivity;
			this.mEmail = mEmail;
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				Log.i("MainActivity", mEmail);
				String token = GoogleAuthUtil.getToken(mActivity, mEmail,"oauth2:https://www.googleapis.com/auth/userinfo.profile");
				Log.i("MainActivity", token);
				return token;

			} catch (UserRecoverableAuthException userRecoverableException) {
				mActivity.startActivityForResult(userRecoverableException.getIntent(),USER_RECOVERABLE_AUTH);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null)
				txtToken.setText(result);
		}

	}
}
