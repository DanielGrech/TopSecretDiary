package com.DGSD.SecretDiary.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.DGSD.SecretDiary.Activity.Phone.EditEntryActivity;
import com.DGSD.SecretDiary.Data.Database;
import com.DGSD.SecretDiary.R;
import com.DGSD.SecretDiary.SecretDiaryApplication;
import com.DGSD.SecretDiary.UI.DrawableOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * Created By: Daniel Grech
 * Date: 6/11/11
 * Description:
 */
public class EditEntryLocationFragment extends Fragment implements DrawableOverlay.OnTapListener  {
    private static final String TAG = EditEntryLocationFragment.class.getSimpleName();

    protected SecretDiaryApplication mApplication;

    protected EditEntryActivity mActivity;

    protected Drawable mDrawable;

    protected ViewGroup mContainer;

    protected LocationManager mLocationManager;

    protected Double mLat;

    protected Double mLon;

    protected boolean _hasChanged = false;

    public static EditEntryLocationFragment newInstance(Double lat, Double lon){
        EditEntryLocationFragment f = new EditEntryLocationFragment();

        // Supply title & text input as an argument.
        Bundle args = new Bundle();
        if(lat != null && lon != null) {
            args.putDouble(Database.Field.LAT, lat);
            args.putDouble(Database.Field.LONG, lon);
        }

        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(args.containsKey(Database.Field.LAT) && args.containsKey(Database.Field.LONG)) {
            mLat = args.getDouble(Database.Field.LAT);
            mLon = args.getDouble(Database.Field.LONG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        mContainer = (FrameLayout) inflater.inflate(R.layout.edit_entry_location, container, false);

        return mContainer;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = (EditEntryActivity) getActivity();

        mApplication = (SecretDiaryApplication) mActivity.getApplication();

        final MapView map = mActivity.getMapView();
        if(mContainer != null) {
            mContainer.addView(map,0);
        }

        mDrawable = mActivity.getResources().getDrawable(R.drawable.map_marker);

        mLocationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);

        GeoPoint location = null;
        if(mLat != null && mLon != null) {
            //Set the map to show the existing coordinates
            location = new GeoPoint((int)(mLat * 1E6), (int)(mLon * 1E6));

            map.getController().setCenter(location);

        } else {
            //Get the quickest approximation for current location
            //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, new MyLocationListener());

            location = getCurrentLocation();
            if(location == null) {
                Toast.makeText(mActivity, "Unable to find current location", Toast.LENGTH_SHORT).show();
            } else {
                //Update our lat/lon values..
                mLat = location.getLatitudeE6() / 1E6;
                mLon = location.getLongitudeE6() / 1E6;
            }
        }

        try {
            if(location != null) {
                //Add the marker onto the map
                final DrawableOverlay overlay = new DrawableOverlay(mDrawable, this);

                overlay.addOverlay(new OverlayItem(location, "", ""));

                map.getOverlays().clear();
                map.getOverlays().add(overlay);

                map.getController().animateTo(location);
            }
        } catch(Exception e) {
            Log.e(TAG, "Error adding overlay to map: " + e.toString());
        }
    }

    @Override
    public void onDestroyView() {
        //Remove the view from the current hierarchy
        if(mActivity.getMapView() != null) {
            ((ViewGroup)mActivity.getMapView().getParent()).removeView(mActivity.getMapView());
        }

        super.onDestroyView();
    }

    @Override
    public void onTapped(GeoPoint p) {
        //Clear any existing markers
        MapView map = mActivity.getMapView();
        map.getOverlays().clear();

        //Get a new overlay
        final DrawableOverlay overlay = new DrawableOverlay(mDrawable, this);
        overlay.addOverlay(new OverlayItem(p, "", ""));

        //Show the new marker
        map.getOverlays().add(overlay);
        map.getController().animateTo(p);

        //Update our lat/long values
        mLat = p.getLatitudeE6() / 1E6;
        mLon = p.getLongitudeE6() / 1E6;

        _hasChanged = true;
    }

    public void setToCurrentLocation() {
        GeoPoint p = getCurrentLocation();
        if(p == null) {
            Toast.makeText(mActivity, "Unable to find current location", Toast.LENGTH_SHORT).show();
        } else {
            this.onTapped(p);
        }
    }

    public void openCurrentLocationInMaps() {
        if(mLat != null && mLon != null) {
            try {
                String location_uri = "geo:" + mLat + "," + mLon + "?z=18&q=" + mLat + "," + mLon;
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(location_uri));
                startActivity(intent);
            } catch(Throwable e) {
                Toast.makeText(mActivity, "Error opening Google Maps", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mActivity, "Location could not be found", Toast.LENGTH_SHORT).show();
        }
    }

    public Double getLatitude() {
        return mLat;
    }

    public Double getLongitude() {
        return mLon;
    }

    private GeoPoint getCurrentLocation() {
        Location last_known_loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(last_known_loc == null) {
            //We need to try a different provider
            last_known_loc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if(last_known_loc != null) {
            return new GeoPoint((int)(last_known_loc.getLatitude()*1E6),
                    (int)(last_known_loc.getLongitude()*1E6));

        } else {
            return null;
        }
    }

    public boolean hasChangedData() {
        return _hasChanged;
    }

    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location loc) {
            // TODO Auto-generated method stub
            GeoPoint myGeoPoint = new GeoPoint(
                    (int)(loc.getLatitude()*1E6),
                    (int)(loc.getLongitude()*1E6));

            mActivity.getMapView().getController().animateTo(myGeoPoint);

        }

        public void onProviderDisabled(String provider) {

        }

        public void onProviderEnabled(String provider) {

        }

        public void onStatusChanged(String provider,int status, Bundle extras) {

        }
    }
}
