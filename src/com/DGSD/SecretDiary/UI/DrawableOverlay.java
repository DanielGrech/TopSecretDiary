package com.DGSD.SecretDiary.UI;

/**
 * Created By: Daniel Grech
 * Date: 7/11/11
 * Description:
 */

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import java.util.ArrayList;

/**
 * Item overlay to display on top of the map.
 */
public class DrawableOverlay extends ItemizedOverlay<OverlayItem> {
    private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

    private OnTapListener mOnTapListener;

    private boolean isPinch = false;

    public DrawableOverlay(Drawable defaultMarker, OnTapListener onTapListener) {
        super(boundCenterBottom(defaultMarker));
        mOnTapListener = onTapListener;
    }

    public DrawableOverlay(Drawable defaultMarker) {
        this(defaultMarker, null);
    }

    public void addOverlay(OverlayItem overlay) {
        mOverlays.add(overlay);
        populate();
    }

    public void setOnTapListener(OnTapListener listener) {
        mOnTapListener = listener;
    }

    @Override
    protected OverlayItem createItem(int i) {
        return mOverlays.get(i);
    }

    @Override
    public int size() {
        return mOverlays.size();
    }

    @Override
    public boolean onTap(GeoPoint p, MapView mapView) {
        if(isPinch) {
            return false;
        }

        if(mOnTapListener != null && p != null) {
            mOnTapListener.onTapped(p);
            return true;
        }
        return super.onTap(p, mapView);
    }

    /* We override this event to detect if the user is doing a pinch-zoom instead of tap */
    @Override
    public boolean onTouchEvent(MotionEvent e, MapView mapView)
    {
        if ( e.getAction()==0 ) {
            // Touch down, don't know it's a pinch yet
            isPinch=false;
        }

        if ( e.getAction()==2 && e.getPointerCount()==2 ) {
            // Two fingers, def a pinch
            isPinch=true;
        }

        return super.onTouchEvent(e,mapView);
    }

    public static interface OnTapListener {
        public void onTapped(GeoPoint p);
    }
}