package com.DGSD.SecretDiary.Fragment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;
import com.DGSD.SecretDiary.Activity.EditEntryChoice;
import com.DGSD.SecretDiary.Data.Database;
import com.DGSD.SecretDiary.Data.EntryProvider;
import com.DGSD.SecretDiary.R;
import com.DGSD.SecretDiary.SecretDiaryApplication;
import com.DGSD.SecretDiary.Utils;

/**
 * Created By: Daniel Grech
 * Date: 2/11/11
 * Description:
 */
public class EntryListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = EntryListFragment.class.getSimpleName();


    private static final String[] FROM = { Database.Field.DATE, Database.Field.TITLE,
            Database.Field.TEXT, Database.Field.ID };

    private static final int[] TO = { R.id.date, R.id.key_text, R.id.value_text };

    private static String[] ROWS_TO_RETURN = {
            Database.Field.ID,
            Database.Field.DATE,
            Database.Field.TITLE,
            Database.Field.TEXT
    };

    private SimpleCursorAdapter mAdapter;

    private SecretDiaryApplication mApplication;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mApplication = (SecretDiaryApplication) getActivity().getApplication();

        // Give some text to display if there is no data.
        setEmptyText("No entries found");

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.entrylist_item, null, FROM, TO, 0);

        setListAdapter(mAdapter);

        this.getListView().setCacheColorHint(Color.TRANSPARENT);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor list_cursor = (Cursor) l.getItemAtPosition(position);
        int item_id = list_cursor.getInt(0);

        //The fields we want to return
        String projection[] = { Database.Field.ID, Database.Field.DATE, Database.Field.FILES,
                                Database.Field.IMG_URIS, Database.Field.LAT, Database.Field.LONG,
                                Database.Field.RECORDINGS, Database.Field.TEXT, Database.Field.TITLE,
                                Database.Field.TAGS};

        //Get our data out of the database
        Cursor c = getActivity().getContentResolver().query(
                Uri.withAppendedPath(EntryProvider.CONTENT_URI,
                        String.valueOf(item_id)), projection, null, null, null);

        Intent i = new Intent(getActivity(), EditEntryChoice.class);

        //Pass the found values through the intent
        if (c.moveToFirst()) {
            i.putExtra(Database.Field.ID, c.getInt(0));
            i.putExtra(Database.Field.DATE, c.getString(1));
            i.putExtra(Database.Field.FILES, c.getString(2));
            i.putExtra(Database.Field.IMG_URIS, c.getString(3));
            i.putExtra(Database.Field.LAT, c.getString(4));
            i.putExtra(Database.Field.LONG, c.getString(5));
            i.putExtra(Database.Field.RECORDINGS, c.getString(6));
            i.putExtra(Database.Field.TEXT, c.getString(7));
            i.putExtra(Database.Field.TITLE, c.getString(8));
            i.putExtra(Database.Field.TAGS, c.getString(9));
        }
        c.close();

        i.putExtra(Utils.EXTRA.INTERNAL, true);
        i.putExtra(Utils.EXTRA.UPDATE, true);

        startActivity(i);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Get a cursor for all entry items
        return new CursorLoader(getActivity(), EntryProvider.CONTENT_URI,
                ROWS_TO_RETURN, null, null,
                Database.Ordering.DATE + Database.Ordering.DESC);
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
}
