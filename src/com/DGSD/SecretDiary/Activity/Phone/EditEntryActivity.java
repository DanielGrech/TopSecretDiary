package com.DGSD.SecretDiary.Activity.Phone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;
import com.DGSD.SecretDiary.Activity.BaseActivity;
import com.DGSD.SecretDiary.Data.Database;
import com.DGSD.SecretDiary.Data.EntryProvider;
import com.DGSD.SecretDiary.Fragment.EditEntryLocationFragment;
import com.DGSD.SecretDiary.Fragment.EditEntryTagFragment;
import com.DGSD.SecretDiary.Fragment.EditEntryTextFragment;
import com.DGSD.SecretDiary.R;
import com.DGSD.SecretDiary.Utils;
import com.google.android.maps.MapView;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitleProvider;
import greendroid.widget.ActionBar;
import greendroid.widget.ActionBarItem;

/**
 * Created By: Daniel Grech
 * Date: 6/11/11
 * Description:
 */
public class EditEntryActivity extends BaseActivity {
    private static final String TAG = EditEntryActivity.class.getSimpleName();

    private ActionBar mActionBar;

    private static final int ADD_ENTRY = 0;

    private static final int NUM_PAGES = 4;

    private static final int TEXT_DETAILS = 0;

    private static final int TAG_DETAILS = 1;

    private static final int MEDIA_DETAILS = 2;

    private static final int LOCATION_DETAILS = 3;

    private boolean mIsUpdate;

    private Integer mEntryId = null;

    private ViewPager mPager;

    private FragmentAdapter mAdapter;

    //We can only have 1 mapview per activity, so we need to keep it here
    public MapView mMapView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.edit_entry);

        mActionBar = getGdActionBar();
        mActionBar.setTitle("Secret Diary Entry");
        mActionBar.setType(ActionBar.Type.Normal);
        mActionBar.addItem(ActionBarItem.Type.Add, ADD_ENTRY);

        Bundle b = getIntent().getExtras();
        if(b.containsKey(Database.Field.ID)) {
            mEntryId = (Integer) b.get(Database.Field.ID);
        }

	    setMapView( new MapView(this, getResources().getString(R.string.maps_key_debug)) );

        mIsUpdate = b.getBoolean(Utils.EXTRA.UPDATE, false);

        mAdapter = new FragmentAdapter(getSupportFragmentManager());
        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
		indicator.setViewPager(mPager);
		indicator.setFooterIndicatorStyle(TitlePageIndicator.IndicatorStyle.Underline);
    }

    public class FragmentAdapter extends FragmentPagerAdapter  implements TitleProvider {
        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public String getTitle(int position) {
            switch(position) {
                case TEXT_DETAILS:
                    return "TEXT";
                case TAG_DETAILS:
                    return "TAGS";
                case MEDIA_DETAILS:
                    return "MEDIA";
                case LOCATION_DETAILS:
                    return "LOCATION";
                default:
                    return "ENTRY";
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public Fragment getItem(int position) {
            Intent i = EditEntryActivity.this.getIntent();

            Fragment f = null;

            switch(position) {
                case TEXT_DETAILS:
                    f = EditEntryTextFragment.newInstance(i.getStringExtra(Database.Field.TITLE),
                             i.getStringExtra(Database.Field.TEXT), i.getStringExtra(Database.Field.DATE));
                    break;
                case TAG_DETAILS:
                    f = EditEntryTagFragment.newInstance(i.getStringExtra(Database.Field.TAGS));
                    break;
                case MEDIA_DETAILS:
                    f = EditEntryTextFragment.newInstance(i.getStringExtra(Database.Field.TITLE),
                             i.getStringExtra(Database.Field.TEXT), i.getStringExtra(Database.Field.DATE));
                    break;
                case LOCATION_DETAILS:
                    Double lat = null, lon = null;

                    try {
                       lat = Double.valueOf(i.getStringExtra(Database.Field.LAT));
                       lon = Double.valueOf(i.getStringExtra(Database.Field.LONG));
                    } catch(Exception e) {
                       //o well..
                    }
                    if(lat == null || lon == null) {
                        f = EditEntryLocationFragment.newInstance(null, null);
                    } else {
                        f = EditEntryLocationFragment.newInstance(lat, lon);
                    }
                    break;
            }

            return f;
        }
    }

    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
        switch(item.getItemId()) {
            case ADD_ENTRY:
                //Using instantiateItem as a hack to get already instantiated fragment
                final EditEntryTextFragment textFragment = (EditEntryTextFragment) mAdapter.instantiateItem(mPager, TEXT_DETAILS);
                final EditEntryTagFragment tagFragment = (EditEntryTagFragment) mAdapter.instantiateItem(mPager, TAG_DETAILS);
                final EditEntryLocationFragment locationFragment = (EditEntryLocationFragment) mAdapter.instantiateItem(mPager, LOCATION_DETAILS);

                Database.ArgumentBuilder builder = new Database.ArgumentBuilder();

                builder.add(Database.Field.TITLE, textFragment.getTitle());
                builder.add(Database.Field.TEXT, textFragment.getText());
                builder.add(Database.Field.DATE, textFragment.getDate());
                builder.add(Database.Field.IMG_URIS,"");
                builder.add(Database.Field.RECORDINGS,"");
                builder.add(Database.Field.TAGS, tagFragment.getTags());
                builder.add(Database.Field.FILES,"");
                builder.add(Database.Field.LAT, locationFragment.getLatitude() == null ? "" : String.valueOf(locationFragment.getLatitude()));
                builder.add(Database.Field.LONG,locationFragment.getLongitude() == null ? "" : String.valueOf(locationFragment.getLongitude()));

                try {
                    if(mIsUpdate) {
                        //Update existing item
                        getContentResolver().update(
                                Uri.withAppendedPath(EntryProvider.CONTENT_URI, String.valueOf(mEntryId)), builder.build(),
                                null, null);
                        Toast.makeText(this, "Entry Updated", Toast.LENGTH_SHORT).show();
                    }   else {
                        //Insert a new item
                        getContentResolver().insert(EntryProvider.CONTENT_URI, builder.build());
                        Toast.makeText(this, "New Entry saved", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                } catch(Exception e) {
                    Log.e(TAG, e.toString());
                    Toast.makeText(this, "Error saving entry. Please try again", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                return super.onHandleActionBarItemClick(item, position);
        }
        return true;
    }

    @Override
	public void onDestroy() {
		mMapView = null;

		super.onDestroy();
	}

    public MapView getMapView() {
        return mMapView;
    }

    public void setMapView(MapView m) {
        if(mMapView != null) {
            Log.e(TAG, "Attemping to set a second map view. Ignoring");
            return;
        }
		mMapView = m;
		mMapView.setClickable(true);
		mMapView.setBuiltInZoomControls(true);
		mMapView.getController().setZoom(12);
	}

}
