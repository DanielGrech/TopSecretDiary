package com.DGSD.SecretDiary.Activity;

import android.content.Intent;
import android.os.Bundle;

/**
 * Created By: Daniel Grech
 * Date: 1/11/11
 * Description:
 */
public class EditEntryChoice extends ChoiceActivity {
    @Override
    public Intent getPhoneIntent() {
        Intent i = new Intent(this, com.DGSD.SecretDiary.Activity.Phone.EditEntryActivity.class);

        //Pass along any extras
        Bundle b = getIntent().getExtras();
        if(b != null) {
            i.putExtras(b);
        }

        return i;
    }

    @Override
    public Intent getTabletIntent() {
        return null;
    }
}
