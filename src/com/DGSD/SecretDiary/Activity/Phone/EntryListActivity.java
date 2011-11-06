package com.DGSD.SecretDiary.Activity.Phone;

import android.content.Intent;
import android.os.Bundle;
import com.DGSD.SecretDiary.Activity.BaseActivity;
import com.DGSD.SecretDiary.Activity.EditEntryChoice;
import com.DGSD.SecretDiary.Fragment.EntryListFragment;
import com.DGSD.SecretDiary.R;
import com.DGSD.SecretDiary.Utils;
import greendroid.widget.ActionBar;
import greendroid.widget.ActionBarItem;

/**
 * Created By: Daniel Grech
 * Date: 1/11/11
 * Description:
 */
public class EntryListActivity extends BaseActivity {
    public static final int ADD_ITEM = 0;

    ActionBar mActionBar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarContentView(R.layout.entrylist);

        mActionBar = getGdActionBar();

        mActionBar.setTitle("Secret Diary");
        mActionBar.setType(ActionBar.Type.Empty);
        mActionBar.addItem(ActionBarItem.Type.Compose, ADD_ITEM);


        getSupportFragmentManager().beginTransaction().add(R.id.container, new EntryListFragment()).commit();
    }

    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
        switch(item.getItemId()) {
            case ADD_ITEM:
                Intent i = new Intent(this, EditEntryChoice.class);
                i.putExtra(Utils.EXTRA.INTERNAL, true);
                startActivity(i);
                break;
            default:
                return super.onHandleActionBarItemClick(item, position);
        }
        return true;
    }


}
