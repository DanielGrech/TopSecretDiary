package com.DGSD.SecretDiary.Data;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.SQLException;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created By: Daniel Grech
 * Date: 2/11/11
 * Description:
 */
public class EntryProvider extends ContentProvider {
    private static final String TAG = EntryProvider.class.getSimpleName();

    private static final String AUTHORITY = "com.DGSD.SecretDiary.Data.EntryProvider";

    private static final String BASE_PATH = Database.Table.DIARY_ENTRIES;

    public static final int ENTRIES = 100;

    public static final int ENTRY = 110;

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY  + "/" + BASE_PATH);

    private static final UriMatcher mURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        mURIMatcher.addURI(AUTHORITY, BASE_PATH, ENTRIES);
        mURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ENTRY);
    }

    private Database mDatabase;

    @Override
    public boolean onCreate() {
        mDatabase = new Database(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sort) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(Database.Table.DIARY_ENTRIES);

        int uriType = mURIMatcher.match(uri);
        switch (uriType) {
            case ENTRY:
                //We want to get a single item
                queryBuilder.appendWhere(Database.Field.ID + "=" + uri.getLastPathSegment());
                break;
            case ENTRIES:
                // no filter, return all fields
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        Cursor cursor = queryBuilder.query(mDatabase.getDbHelper().getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sort);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        int uriType = mURIMatcher.match(uri);
        switch (uriType) {
            case ENTRY:
                return String.valueOf(ENTRY);
            case ENTRIES:
                return String.valueOf(ENTRIES);
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = mURIMatcher.match(uri);

        if (uriType != ENTRIES) {
            throw new IllegalArgumentException("Invalid URI for insert");
        }

        SQLiteDatabase sqlDB = mDatabase.getDbHelper().getWritableDatabase();

        try {
            long newID = sqlDB.insertOrThrow(Database.Table.DIARY_ENTRIES, null, values);

            if (newID > 0) {
                Uri newUri = ContentUris.withAppendedId(uri, newID);
                getContext().getContentResolver().notifyChange(uri, null);
                return newUri;
            } else {
                throw new SQLException("Failed to insert row into " + uri);
            }
        } catch (SQLiteConstraintException e) {
            Log.i(TAG, "Ignoring constraint failure.");
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = mURIMatcher.match(uri);

        SQLiteDatabase sqlDB = mDatabase.getDbHelper().getWritableDatabase();

        int rowsAffected = 0;
        switch (uriType) {
            case ENTRIES:
                rowsAffected = sqlDB.delete(Database.Table.DIARY_ENTRIES,
                        selection, selectionArgs);
                break;
            case ENTRY:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsAffected = sqlDB.delete(Database.Table.DIARY_ENTRIES,
                            Database.Field.ID + "=" + id,
                            null);
                } else {
                    rowsAffected = sqlDB.delete(Database.Table.DIARY_ENTRIES,
                            selection + " and " + Database.Field.ID + "=" + id,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int uriType = mURIMatcher.match(uri);

        SQLiteDatabase sqlDB = mDatabase.getDbHelper().getWritableDatabase();

        int rowsAffected = 0;
        switch (uriType) {
            case ENTRIES:
                rowsAffected = sqlDB.update(Database.Table.DIARY_ENTRIES, contentValues,
                        selection, selectionArgs);
                break;
            case ENTRY:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsAffected = sqlDB.update(Database.Table.DIARY_ENTRIES, contentValues,
                            Database.Field.ID + "=" + id,
                            null);
                } else {
                    rowsAffected = sqlDB.update(Database.Table.DIARY_ENTRIES, contentValues,
                            selection + " and " + Database.Field.ID + "=" + id,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
    }
}
