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
public class EditEntryTagFragment extends DialogFragment
        implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener{

    private static final String TAG = EditEntryTagFragment.class.getSimpleName();

    protected SecretDiaryApplication mApplication;

    protected String mPwd;

    private SimpleCursorAdapter mAdapter;

    private HashSet<String> tags;

    private ListView mListView;

    private EditText mNewTagName;

    private ImageButton mNewTagButton;

    private int mLastSelectedColor = Color.BLACK;

    private static final String[] FROM = { Database.Field.TAG_NAME,
            Database.Field.TAG_COLOR, Database.Field.ID };

    private static final int[] TO = { R.id.date, R.id.key_text, R.id.value_text };

    private static String[] ROWS_TO_RETURN = {
            Database.Field.ID,
            Database.Field.TAG_NAME,
            Database.Field.TAG_COLOR
    };

    public static EditEntryTagFragment newInstance(String tagCsv){
		EditEntryTagFragment f = new EditEntryTagFragment();

		// Supply title & text input as an argument.
        Bundle args = new Bundle();
        if(tagCsv != null) {
            String[] tags = tagCsv.split(",");
            if(tags != null) {
                args.putStringArray(Database.Field.TAGS, tags);
            }
        }

        f.setArguments(args);

		return f;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        tags = new HashSet<String>();

        String[] old_tags = getArguments().getStringArray(Database.Field.TAGS);

        if(old_tags != null) {
            for(String s : old_tags) {
                tags.add(s);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.edit_entry_tag, container, false);

        mListView = (ListView) v.findViewById(R.id.tag_list);

        mNewTagName = (EditText) v.findViewById(R.id.new_tag_name);

        mNewTagButton = (ImageButton) v.findViewById(R.id.btn_AddNewTag);
        mNewTagButton.setEnabled(false);

        mNewTagName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if(mNewTagName.getText() != null && mNewTagName.getText().length() > 0) {
                    //Enable the button now that we have a tag name
                    mNewTagButton.setEnabled(true);
                } else {
                    //Disable the button if no tag name has been entered
                    mNewTagButton.setEnabled(false);
                }
            }
        });

        mNewTagName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
					return false;
				} else{
                    if(mNewTagName.getText() != null &&
                            mNewTagName.getText().toString().replaceAll("\\n","").length() > 0) {
                        mNewTagButton.performClick();
                    }

                    return true;
				}
			}
		});

        mNewTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Database.ArgumentBuilder builder = new Database.ArgumentBuilder();
                builder.add(Database.Field.TAG_NAME, mNewTagName.getText().toString().replaceAll("\\n",""));
                builder.add(Database.Field.TAG_COLOR, Integer.toString(mLastSelectedColor));
                try {

                    Cursor c = getActivity().getContentResolver().query(TagProvider.CONTENT_URI, null,
                            Database.Field.TAG_NAME + "='" + mNewTagName.getText().toString() + "'" ,
                            null,null);

                    if(c.moveToFirst()) {
                        //We already have a tag by this name, update it
                        getActivity().getContentResolver().update(
                                Uri.withAppendedPath(TagProvider.CONTENT_URI, String.valueOf(c.getInt(0))), builder.build(),
                                null, null);
                        Toast.makeText(getActivity(), "Existing tag updated", Toast.LENGTH_SHORT).show();
                    } else {
                        //Insert a new item
                        getActivity().getContentResolver().insert(TagProvider.CONTENT_URI, builder.build());

                        Toast.makeText(getActivity(), "New Tag saved", Toast.LENGTH_SHORT).show();
                    }

                    //Reset the text field
                    Utils.hideKeyboard(mNewTagName);
                    mNewTagName.setText("");

                    //Reset the tag color
                    mLastSelectedColor = Color.BLACK;
                } catch(Exception e) {
                    Log.e(TAG, e.toString());
                    Toast.makeText(getActivity(), "Error saving entry. Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mApplication = (SecretDiaryApplication) getActivity().getApplication();

        mPwd = mApplication.getPassword();

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new TagAdapter(FROM, TO);

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setItemsCanFocus(false);

        // Prepare the loader.  Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        ViewHolder vh = (ViewHolder) view.getTag();

        if(mListView.isItemChecked(pos)) {
            tags.add(vh.checked_text.getText().toString());
        } else {
            tags.remove(vh.checked_text.getText().toString());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Get a cursor for all entry items
        return new CursorLoader(getActivity(), TagProvider.CONTENT_URI,
                ROWS_TO_RETURN, null, null,
                Database.Ordering.TAG_NAME + Database.Ordering.DESC);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // Swap the new cursor in. (Old cursor is automatically closed)
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    public String getTags() {
        //Return the tags as a CSV string
        return Utils.join(tags, ",");
    }

    public boolean hasChangedData() {
        Bundle args = getArguments();

        String[] orig_tags = getArguments().getStringArray(Database.Field.TAGS);

        if(orig_tags == null) {
            //We have no data in the first place
            return tags.size() > 0 ? true : false;
        }

        if(tags.size() != orig_tags.length) {
            //We have definitely changed some tag selection
            return true;
        } else {
            //Check that very tag in orig_tags is in tags
            for(String t :orig_tags) {
                if(!tags.contains(t)) {
                    return true;
                }
            }
        }

        return false;
    }

    private class TagAdapter extends SimpleCursorAdapter {
        private int name_column = -1;

        private int color_column = -1;

        public TagAdapter(String[] from, int[] to) {
            super(getActivity(), R.layout.taglist_item, null, from, to, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            Cursor c = getCursor();

            View v = LayoutInflater.from(context).inflate(R.layout.taglist_item, parent, false);

            if(name_column < 0) {
                name_column = c.getColumnIndex(Database.Field.TAG_NAME);
            }

            if(color_column < 0) {
                color_column = c.getColumnIndex(Database.Field.TAG_COLOR);
            }

            //Bind the values to the appropriate view
            ViewHolder vh = new ViewHolder((CheckedTextView)v.findViewById(R.id.checkedTextBox));

            v.setTag(vh);

            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final String name = cursor.getString(name_column);
            final String color = cursor.getString(color_column);

            ViewHolder vh = (ViewHolder) view.getTag();
            try {
                vh.checked_text.setText(Encryption.decrypt(mPwd,name));
                vh.checked_text.setTextColor(Integer.valueOf(Encryption.decrypt(mPwd,color)));

                if(tags.contains(Encryption.decrypt(mPwd,name))) {
                    mListView.setItemChecked(cursor.getPosition(), true);
                } else {
                    mListView.setItemChecked(cursor.getPosition(), false);
                }
            } catch(Exception e) {
                Log.e(TAG, "Error binding view: " + Log.getStackTraceString(e));
            }
        }
    }

    private class ViewHolder {
        public CheckedTextView checked_text;
        public ViewHolder(CheckedTextView ctb) {
            checked_text = ctb;
        }
    }
}
