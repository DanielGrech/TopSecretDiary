/**
 * TODO: Add animation to view adding on tag page:
 *  this.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation));
 *  (http://android-er.blogspot.com/2009/10/listview-and-listactivity-layout.html)
 */

package com.DGSD.SecretDiary.Activity.Phone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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
public class EditEntryActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    private static final String TAG = EditEntryActivity.class.getSimpleName();

    private ActionBar mActionBar;

    private static final int NUM_PAGES = 4;

    //Constants for action bar items
    private static final int ADD_ENTRY = 0;

    private static final int SET_CURRENT_LOCATION = 1;

    private static final int OPEN_IN_MAPS = 2;

    //Constants for individual pages
    private static final int TEXT_DETAILS = 0;

    private static final int TAG_DETAILS = 1;

    private static final int MEDIA_DETAILS = 2;

    private static final int LOCATION_DETAILS = 3;

    private boolean mIsUpdate;

    private Integer mEntryId = null;

    private ViewPager mPager;

    private FragmentAdapter mAdapter;

    private ActionBarItem mSetCurrentLocationItem;

    //We can only have 1 mapview per activity, so we need to keep it here
    public MapView mMapView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.edit_entry);

        mActionBar = getGdActionBar();
        mActionBar.setTitle("New Entry");
        mActionBar.setType(ActionBar.Type.Normal);
        mActionBar.addItem(ActionBarItem.Type.Add, ADD_ENTRY);

        Bundle b = getIntent().getExtras();
        if(b.containsKey(Database.Field.ID)) {
            mEntryId = (Integer) b.get(Database.Field.ID);
        }

        setMapView( new MapView(this, getResources().getString(R.string.maps_key_debug_imac)) );

        mIsUpdate = b.getBoolean(Utils.EXTRA.UPDATE, false);

        mAdapter = new FragmentAdapter(getSupportFragmentManager());
        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(mPager);
        indicator.setFooterIndicatorStyle(TitlePageIndicator.IndicatorStyle.Underline);

        //Register listener to make changes when pages are scrolled
        indicator.setOnPageChangeListener(this);
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
        //Using instantiateItem as a hack to get already instantiated fragment
        final EditEntryTextFragment textFragment = (EditEntryTextFragment) mAdapter.instantiateItem(mPager, TEXT_DETAILS);
        final EditEntryTagFragment tagFragment = (EditEntryTagFragment) mAdapter.instantiateItem(mPager, TAG_DETAILS);
        final EditEntryLocationFragment locationFragment = (EditEntryLocationFragment) mAdapter.instantiateItem(mPager, LOCATION_DETAILS);

        switch(item.getItemId()) {
            case ADD_ENTRY:
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
                    boolean has_error = false;
                    if(mIsUpdate) {
                        //Update existing item
                        getContentResolver().update(
                                Uri.withAppendedPath(EntryProvider.CONTENT_URI, String.valueOf(mEntryId)), builder.build(),
                                null, null);
                        Toast.makeText(this, "Entry Updated", Toast.LENGTH_SHORT).show();
                    }   else {
                        //Insert a new item
                        Uri retval = getContentResolver().insert(EntryProvider.CONTENT_URI, builder.build());
                        if(retval != null) {
                            Toast.makeText(this, "New Entry saved", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error creating new entry saved", Toast.LENGTH_SHORT).show();
                            has_error = true;
                        }
                    }
                    if(!has_error) {
                        finish();
                    }
                } catch(Exception e) {
                    Log.e(TAG, e.toString());
                    Toast.makeText(this, "Error saving entry. Please try again", Toast.LENGTH_SHORT).show();
                }
                break;

            case SET_CURRENT_LOCATION:
                locationFragment.setToCurrentLocation();
                break;

            case OPEN_IN_MAPS:
                locationFragment.openCurrentLocationInMaps();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            final EditEntryTextFragment textFragment = (EditEntryTextFragment) mAdapter.instantiateItem(mPager, TEXT_DETAILS);
            final EditEntryTagFragment tagFragment = (EditEntryTagFragment) mAdapter.instantiateItem(mPager, TAG_DETAILS);
            final EditEntryLocationFragment locationFragment = (EditEntryLocationFragment) mAdapter.instantiateItem(mPager, LOCATION_DETAILS);

            if(!textFragment.hasChangedData() &&
                    !tagFragment.hasChangedData()
                    && !locationFragment.hasChangedData()) {
                return super.onKeyDown(keyCode, event);
            }


            AlertDialog.Builder builder =
                    new AlertDialog.Builder(EditEntryActivity.this);

            builder.setTitle("Are you sure?");
            builder.setMessage("Your changes will be lost");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            builder.create().show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {
        //Do nothing.
    }

    @Override
    public void onPageSelected(int page) {
        //Adjust the action bar items.

        //Remove existing actionbar items
        mActionBar.removeAllItems();

        switch(page) {
            case TEXT_DETAILS:

                break;

            case TAG_DETAILS:

                break;

            case MEDIA_DETAILS:

                break;

            case LOCATION_DETAILS:
                //Current location
                mActionBar.addItem(ActionBarItem.Type.LocateMyself, SET_CURRENT_LOCATION);

                //Open in maps item
                mActionBar.addItem(ActionBarItem.Type.Map, OPEN_IN_MAPS);

                break;
        }

        //Add/save item
        mActionBar.addItem(ActionBarItem.Type.Add, ADD_ENTRY);

        //Make sure we hide the map zoom controller for our mapview
        if(mMapView != null)
        if(page == LOCATION_DETAILS) {
            mMapView.getZoomButtonsController().getContainer().setVisibility(View.VISIBLE);
        } else {
            mMapView.getZoomButtonsController().getContainer().setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {
        //Do Nothing
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
