package com.DGSD.SecretDiary.Fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.DGSD.SecretDiary.Data.Database;
import com.DGSD.SecretDiary.R;
import com.DGSD.SecretDiary.SecretDiaryApplication;

/**
 * Created By: Daniel Grech
 * Date: 6/11/11
 * Description:
 */
public class EditEntryTextFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>  {
    private static final String TAG = EditEntryTextFragment.class.getSimpleName();

    protected SecretDiaryApplication mApplication;

    private EditText mTitle;

    private EditText mText;

    private TextView mDate;

    public static EditEntryTextFragment newInstance(String title, String text, String date){
		EditEntryTextFragment f = new EditEntryTextFragment();

		// Supply title & text input as an argument.
        Bundle args = new Bundle();
        if(title != null) {
            args.putString(Database.Field.TITLE, title);
        }

        if(text != null) {
            args.putString(Database.Field.TEXT, text);
        }

        if(date != null) {
            args.putString(Database.Field.DATE, date);
        }

        f.setArguments(args);

		return f;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.edit_entry_text, container, false);

        mTitle = (EditText) v.findViewById(R.id.title);
        mText = (EditText) v.findViewById(R.id.text);
        mDate = (TextView) v.findViewById(R.id.label_Date);

        Bundle args = getArguments();

        if(args.containsKey(Database.Field.TITLE)) {
            mTitle.setText(args.getString(Database.Field.TITLE));
        }

        if(args.containsKey(Database.Field.TEXT)) {
            mText.setText(args.getString(Database.Field.TEXT));
        }

        if(args.containsKey(Database.Field.DATE)) {
            mDate.setText(args.getString(Database.Field.DATE));
        } else {
            Time now = new Time();
            now.setToNow();
            mDate.setText(now.format("%c"));
        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mApplication = (SecretDiaryApplication) getActivity().getApplication();

        // Prepare the loader.  Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getTitle() {
        return mTitle.getText() == null ? "" : mTitle.getText().toString();
    }

    public String getText() {
        return mText.getText() == null ? "" : mText.getText().toString();
    }

    public String getDate() {
        return mDate.getText() == null ? "" : mDate.getText().toString();
    }
}
