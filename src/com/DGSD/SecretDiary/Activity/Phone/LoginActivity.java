package com.DGSD.SecretDiary.Activity.Phone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.DGSD.SecretDiary.Activity.BaseActivity;
import com.DGSD.SecretDiary.Activity.EntryListChoice;
import com.DGSD.SecretDiary.Encryption;
import com.DGSD.SecretDiary.R;
import com.DGSD.SecretDiary.SecretDiaryApplication;
import com.DGSD.SecretDiary.Utils;
import greendroid.widget.ActionBar;
import greendroid.widget.ActionBar.Type;
import greendroid.widget.ActionBarItem;
import greendroid.widget.NormalActionBarItem;

/**
 * Created By: Daniel Grech
 * Date: 1/11/11
 * Description:
 */

public class LoginActivity extends BaseActivity {
	public static final int ACTION_HELP = 0;

	public static final int ACTION_SUBMIT = 1;
	
	private static final String KEY_LAST_ALERT_TITLE = "alert_title";

	private static final String KEY_LAST_ALERT_MESSAGE = "alert_message";
	
	private ActionBar mActionBar;
	
	private SharedPreferences mPrefs;
	
	private SecretDiaryApplication mApplication;
	
	private EditText mPasswordField;
	
	private AlertDialog currentDialog;

	private String mLastAlertTitle;

	private String mLastAlertMessage;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.login);
		
		//Get Application preferences
		mPrefs = getSharedPreferences(SecretDiaryApplication.KEY_MY_PREFERENCES, 0);
		mApplication = (SecretDiaryApplication) getApplication();
		
		//Set up Action bar
		mActionBar = getGdActionBar();
		mActionBar.setTitle("Unlock Secret Diary");
		mActionBar.setType(Type.Empty);
		
		addActionBarItem(ActionBarItem.Type.Help, ACTION_HELP);
		addActionBarItem(mActionBar
                .newActionBarItem(NormalActionBarItem.class)
                .setDrawable(R.drawable.ic_menu_forward), ACTION_SUBMIT);
		
		//Get handles to activity views
		mPasswordField = (EditText) findViewById(R.id.password);
		
		//Setup Handlers
		mPasswordField.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
					return false;
				} else{
					Utils.hideKeyboard(mPasswordField);
					attemptUnlock();
					return true;
				}
			}
		});
		
		//Restore any previously showing dialogs
		restoreDialog(savedInstanceState);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(currentDialog != null && currentDialog.isShowing()){
			outState.putString(KEY_LAST_ALERT_TITLE, mLastAlertTitle);
			outState.putString(KEY_LAST_ALERT_MESSAGE, mLastAlertMessage);
		}
	}
	
	@Override
	public void onStop() {
		if(currentDialog != null) {
			currentDialog.dismiss();
			currentDialog = null;
		}

		super.onStop();
	}

	private void restoreDialog(Bundle bundle) {
		if(bundle != null) {
			mLastAlertTitle = bundle.getString(KEY_LAST_ALERT_TITLE);

			mLastAlertMessage = bundle.getString(KEY_LAST_ALERT_MESSAGE);

			if(mLastAlertTitle != null && mLastAlertMessage != null) {
				showDialog(mLastAlertTitle, mLastAlertMessage);
			}
		}
	}
	
	private void showDialog(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(title);
		builder.setMessage(message);

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		currentDialog = builder.create();

		mLastAlertTitle = title;
		
		mLastAlertMessage = message;
		
		currentDialog.show();
	}
	
	private boolean unlock(String password){
		try {
			Encryption.decrypt(password, 
					mPrefs.getString(SecretDiaryApplication.KEY_ENCRYPTION_TEST, null));
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch(item.getItemId()) {
			case ACTION_HELP:
				showDialog("Help Pressed", "You pressed the help button");
				break;
			case ACTION_SUBMIT:
				attemptUnlock();
				break;
			default:
				return super.onHandleActionBarItemClick(item, position);
		}
		return true;
	}
	
	public void attemptUnlock() {
		final String password = mPasswordField.getText().toString();
		
		if(password == null || password.length() == 0) {
			showDialog("Error", "Please enter your password");
            mPasswordField.setText("");
			return;
		}
		
		if(unlock(password)) {
            mApplication.setPassword(password);

            Intent i = new Intent(this, EntryListChoice.class);
            i.putExtra(Utils.EXTRA.INTERNAL, getIntent().getBooleanExtra(Utils.EXTRA.INTERNAL, false));

            startActivity(i);

			return;
		} else {
			showDialog("Error!", "Your login was not successful");
            mPasswordField.setText("");
			return;
		}
	}
}
