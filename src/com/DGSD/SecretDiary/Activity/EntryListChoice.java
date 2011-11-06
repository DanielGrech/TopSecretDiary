package com.DGSD.SecretDiary.Activity;

import android.content.Intent;

/**
 * Created By: Daniel Grech
 * Date: 1/11/11
 * Description:
 */
public class EntryListChoice extends ChoiceActivity {
    @Override
    public Intent getPhoneIntent() {
        return new Intent(this, com.DGSD.SecretDiary.Activity.Phone.EntryListActivity.class);
    }

    @Override
    public Intent getTabletIntent() {
        return null;
    }
}
