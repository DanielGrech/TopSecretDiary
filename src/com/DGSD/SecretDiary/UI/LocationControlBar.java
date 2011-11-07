package com.DGSD.SecretDiary.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import com.DGSD.SecretDiary.R;

public class LocationControlBar extends LinearLayout {
	public LocationControlBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(HORIZONTAL);
		setGravity(Gravity.CENTER);
		setWeightSum(1.0f);
		
		LayoutInflater.from(context).inflate(R.layout.location_control_bar, this, true);

		((Button) findViewById(R.id.btn_SetCurrentLocation)).setText("Set to Current Location");

		((Button) findViewById(R.id.btn_openInMaps)).setText("Open In Maps");
	}
}
