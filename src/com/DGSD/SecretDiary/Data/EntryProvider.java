package com.DGSD.SecretDiary.Data;

import android.net.Uri;

/**
 * Created By: Daniel Grech
 * Date: 2/11/11
 * Description:
 */
public class EntryProvider extends BaseProvider {
    private static final String TAG = EntryProvider.class.getSimpleName();

    protected static final String AUTHORITY = "com.DGSD.SecretDiary.Data.EntryProvider";

    private static final String BASE_PATH = Database.Table.DIARY_ENTRIES;

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY  + "/" + BASE_PATH);

    static {
        mURIMatcher.addURI(AUTHORITY, BASE_PATH, MULTIPLE);
        mURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", SINGLE);
    }

    @Override
    public String getTable() {
        return Database.Table.DIARY_ENTRIES;
    }

    @Override
    public String getAuthority() {
        return AUTHORITY;
    }
}
