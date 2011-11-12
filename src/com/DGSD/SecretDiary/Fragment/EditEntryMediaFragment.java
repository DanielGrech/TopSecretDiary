package com.DGSD.SecretDiary.Fragment;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.DGSD.SecretDiary.Data.Database;
import com.DGSD.SecretDiary.Data.TagProvider;
import com.DGSD.SecretDiary.Encryption;
import com.DGSD.SecretDiary.R;
import com.DGSD.SecretDiary.SecretDiaryApplication;
import com.DGSD.SecretDiary.Utils;

import java.util.HashSet;

/**
 * Created By: Daniel Grech
 * Date: 6/11/11
 * Description:
 */
public class EditEntryMediaFragment extends DialogFragment {

    private static final String TAG = EditEntryMediaFragment.class.getSimpleName();

    protected SecretDiaryApplication mApplication;

    protected String mPwd;

    private TabHost mTabs;

    private TextView mEmptyText;

    private HashSet<String> mImgUris;

    private HashSet<String> mRecordingUris;

    public static EditEntryMediaFragment newInstance(String image_uris, String recording_uris){
		EditEntryMediaFragment f = new EditEntryMediaFragment();

		// Supply title & text input as an argument.
        Bundle args = new Bundle();
        if(image_uris != null) {
            String[] imgs = image_uris.split(",");
            if(imgs != null) {
                args.putStringArray(Database.Field.IMG_URIS, imgs);
            }
        }

        if(recording_uris != null) {
            String[] recordings = recording_uris.split(",");
            if(recordings != null) {
                args.putStringArray(Database.Field.RECORDINGS, recordings);
            }
        }

        f.setArguments(args);

		return f;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mImgUris = new HashSet<String>();
        mRecordingUris = new HashSet<String>();

        String[] img_uris = getArguments().getStringArray(Database.Field.IMG_URIS);

        if(img_uris != null) {
            for(String s : img_uris) {
                mImgUris.add(s);
            }
        }

        String[] recording_uris = getArguments().getStringArray(Database.Field.RECORDINGS);

        if(recording_uris != null) {
            for(String s : recording_uris) {
                mRecordingUris.add(s);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.edit_entry_media, container, false);

        mTabs = (TabHost) v.findViewById(R.id.tabhost);

        mEmptyText = (TextView) v.findViewById(R.id.empty_text);

        mTabs.setup();

        if(mImgUris.size() > 0 || mRecordingUris.size() > 0) {
            //We have some media, hide the empty text & show our tabs
            mEmptyText.setVisibility(View.GONE);
            mTabs.setVisibility(View.VISIBLE);
        }

        if(mImgUris.size() > 0) {
            //Add our images tab
            TabHost.TabSpec imgTab = mTabs.newTabSpec("Images");
            imgTab.setIndicator("Images");
            imgTab.setContent(new TabHost.TabContentFactory(){
                @Override
                public View createTabContent(String s) {
                    TextView t = new TextView(getActivity());

                    t.setText(Utils.join(mImgUris, ","));

                    return t;
                }
            });
            mTabs.addTab(imgTab);
        }

        if(mRecordingUris.size() > 0) {
            //Add our recordings tab
            TabHost.TabSpec recordingsTab = mTabs.newTabSpec("Recordings");
            recordingsTab.setIndicator("Recordings");
            recordingsTab.setContent(new TabHost.TabContentFactory(){
                @Override
                public View createTabContent(String s) {
                    TextView t = new TextView(getActivity());

                    t.setText(Utils.join(mRecordingUris, ","));

                    return t;
                }
            });
            mTabs.addTab(recordingsTab);
        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mApplication = (SecretDiaryApplication) getActivity().getApplication();

        mPwd = mApplication.getPassword();
    }

    public void addImageUri(Uri u) {

    }

    public void addRecordingUri(Uri u) {

    }

    public String getRecordingUris() {
        return Utils.join(mRecordingUris, ",");
    }

    public String getImageUris() {
        return Utils.join(mImgUris, ",");
    }

    public boolean hasChangedData() {
        return false;
    }
}
