package com.DGSD.SecretDiary.Data;

import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created By: Daniel Grech
 * Date: 2/11/11
 * Description:
 */
public class TagProvider extends ContentProvider {
    private static final String TAG = TagProvider.class.getSimpleName();

    private static final String AUTHORITY = "com.DGSD.SecretDiary.Data.TagProvider";

    private static final String BASE_PATH = Database.Table.TAGS;

    public static final int TAGS = 100;

    public static final int SINGLE_TAG = 110;

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY  + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/tags";

    private static final UriMatcher mURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        mURIMatcher.addURI(AUTHORITY, BASE_PATH, TAGS);
        mURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", SINGLE_TAG);
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
        queryBuilder.setTables(Database.Table.TAGS);

        int uriType = mURIMatcher.match(uri);
        switch (uriType) {
            case SINGLE_TAG:
                //We want to get a single item
                queryBuilder.appendWhere(Database.Field.ID + "=" + uri.getLastPathSegment());
                break;
            case TAGS:
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
            case SINGLE_TAG:
                return String.valueOf(SINGLE_TAG);
            case TAGS:
                return String.valueOf(TAGS);
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = mURIMatcher.match(uri);

        if (uriType != TAGS) {
            throw new IllegalArgumentException("Invalid URI for insert");
        }

        SQLiteDatabase sqlDB = mDatabase.getDbHelper().getWritableDatabase();

        try {
            long newID = sqlDB.insertOrThrow(Database.Table.TAGS, null, values);

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
            case TAGS:
                rowsAffected = sqlDB.delete(Database.Table.TAGS,
                        selection, selectionArgs);
                break;
            case SINGLE_TAG:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsAffected = sqlDB.delete(Database.Table.TAGS,
                            Database.Field.ID + "=" + id,
                            null);
                } else {
                    rowsAffected = sqlDB.delete(Database.Table.TAGS,
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
            case TAGS:
                rowsAffected = sqlDB.update(Database.Table.TAGS, contentValues,
                        selection, selectionArgs);
                break;
            case SINGLE_TAG:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsAffected = sqlDB.update(Database.Table.TAGS, contentValues,
                            Database.Field.ID + "=" + id,
                            null);
                } else {
                    rowsAffected = sqlDB.update(Database.Table.TAGS, contentValues,
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
