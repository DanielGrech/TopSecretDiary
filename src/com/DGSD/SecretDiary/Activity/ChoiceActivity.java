package com.DGSD.SecretDiary.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.DGSD.SecretDiary.Utils;

/**
 * Created By: Daniel Grech
 * Date: 6/11/11
 * Description:Base class which helps choose between 2 classes, one for tablets, one for phones
 */
public abstract class ChoiceActivity extends Activity {

    public abstract Intent getPhoneIntent();

    public abstract Intent getTabletIntent();

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        Intent intent = null;
        //Confirm that we are starting this activity internally from the app
        if(getIntent().getBooleanExtra(Utils.EXTRA.INTERNAL, false)) {
            //Show our entry list
            if(Utils.isTablet(this)) {
                intent = getTabletIntent();
            } else {
                intent = getPhoneIntent();
            }
        } else {
            //Go directly to create a new entry
        }

		startActivity(intent);
		finish();
    }
}
