package com.DGSD.SecretDiary;

import android.content.Intent;
import android.content.SharedPreferences;
import greendroid.app.GDApplication;

/**
 * Created By: Daniel Grech
 * Date: 1/11/11
 * Description:
 */

public class SecretDiaryApplication extends GDApplication {
	public static final String KEY_HAS_LOGGED_IN_BEFORE = "has_logged_in_flag";

	public static final String KEY_MY_PREFERENCES = "preferences";

	public static final String KEY_ENCRYPTION_TEST = "encryption_test";
	
	public static final String KEY_PASSWORD_HINT = "password_hint";
	
	private SharedPreferences mPrefs;
	
	private String mPassword;

	private String mPasswordHint;

    @Override
	public void onCreate() {
		mPrefs = getSharedPreferences(SecretDiaryApplication.KEY_MY_PREFERENCES, 0);
		
		mPasswordHint = mPrefs.getString(KEY_PASSWORD_HINT, null);
	}

    @Override
    public Class<?> getHomeActivityClass() {
        if(Utils.isTablet(this)) {
            return null;//com.DGSD.SecretDiary.Activity.Tablet.EntryListActivity.class;
        } else {
            return com.DGSD.SecretDiary.Activity.Phone.EntryListActivity.class;
        }
    }

    @Override
    public Intent getMainApplicationIntent() {
        return new Intent(this, com.DGSD.SecretDiary.Activity.Phone.EntryListActivity.class);
    }

	public void setPassword(String password) {
		mPassword = password;
	}
	
	public String getPassword() {
		return mPassword;
	}
	
	public void setPasswordHint(String hint) {
		mPasswordHint = hint;
	}
	
	public String getPasswordHint() {
		return mPasswordHint;
	}
}
