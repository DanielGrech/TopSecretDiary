package com.DGSD.SecretDiary.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.DGSD.SecretDiary.Encryption;
import com.DGSD.SecretDiary.SecretDiaryApplication;

/**
 * Created By: Daniel Grech
 * Date: 7/11/11
 * Description: Base instance for all providers
 */
public abstract class BaseProvider extends ContentProvider {
    protected static final String TAG = BaseProvider.class.getSimpleName();

    public static final int MULTIPLE = 100;

    public static final int SINGLE = 110;

    protected static final UriMatcher mURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private Database mDatabase;

    private String mPwd;

    public abstract String getTable();

    public abstract String getAuthority();

    @Override
    public boolean onCreate() {
        mDatabase = new Database(getContext());
        return false;
    }

    private String getPassword() {
        if(mPwd == null) {
            mPwd = ((SecretDiaryApplication)getContext()).getPassword();
        }

        return mPwd;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sort) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(getTable());

        int uriType = mURIMatcher.match(uri);

        if(uriType == SINGLE) {
            //We want to get a single item
            queryBuilder.appendWhere(Database.Field.ID + "=" + uri.getLastPathSegment());
        } else if(uriType == MULTIPLE) {
            //No filter. Return all fields
        } else {
           throw new IllegalArgumentException("Unknown URI");
        }

        try {
            String[] enc_args = null;
            if(selectionArgs != null) {
                enc_args = new String[selectionArgs.length];
                for(int i = 0, size=selectionArgs.length; i < size; i++) {
                    enc_args[i] = Encryption.decrypt(getPassword(), selectionArgs[i]);
                }
            }

            Cursor cursor = queryBuilder.query(mDatabase.getDbHelper().getReadableDatabase(),
                projection, selection, enc_args, null, null, sort);

            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        } catch(Exception e) {
            Log.e(TAG, "Error querying database: " + e.toString());
            return null;
        }
    }

    @Override
    public String getType(Uri uri) {
        int uriType = mURIMatcher.match(uri);

        if(uriType == SINGLE || uriType == MULTIPLE) {
            return String.valueOf(uriType);
        } else {
            return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = mURIMatcher.match(uri);

        if (uriType != MULTIPLE) {
            throw new IllegalArgumentException("Invalid URI for insert");
        }

        SQLiteDatabase sqlDB = mDatabase.getDbHelper().getWritableDatabase();

        try {
            long newID = sqlDB.insertOrThrow(getTable(), null, Database.encryptValues(getPassword(),values));

            if (newID > 0) {
                Uri newUri = ContentUris.withAppendedId(uri, newID);
                getContext().getContentResolver().notifyChange(uri, null);
                return newUri;
            } else {
                throw new SQLException("Failed to insert row into " + uri);
            }
        } catch (SQLiteConstraintException e) {
            Log.e(TAG, "Ignoring constraint failure: " + e.toString());
        } catch(Exception e) {
            Log.e(TAG, "Error inserting into database: " + e.toString());
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = mURIMatcher.match(uri);

        SQLiteDatabase sqlDB = mDatabase.getDbHelper().getWritableDatabase();

        try {
            String[] enc_args = null;
            if(selectionArgs != null) {
                enc_args = new String[selectionArgs.length];
                for(int i = 0, size=selectionArgs.length; i < size; i++) {
                    enc_args[i] = Encryption.encrypt(getPassword(), selectionArgs[i]);
                }
            }

            int rowsAffected = 0;
            if(uriType == SINGLE) {
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsAffected = sqlDB.delete(getTable(),
                            Database.Field.ID + "=" + id,
                            null);
                } else {
                    rowsAffected = sqlDB.delete(getTable(),
                            selection + " and " + Database.Field.ID + "=" + id,
                            enc_args);
                }
            } else if(uriType == MULTIPLE) {
                rowsAffected = sqlDB.delete(getTable(), selection, enc_args);
            } else {
                throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
            }

            getContext().getContentResolver().notifyChange(uri, null);
            return rowsAffected;
        } catch(Exception e) {
            Log.e(TAG, "Error deleting item: " + e.toString());
            return 0;
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int uriType = mURIMatcher.match(uri);

        SQLiteDatabase sqlDB = mDatabase.getDbHelper().getWritableDatabase();

        try {
            String[] enc_args = null;
            if(selectionArgs != null) {
                enc_args = new String[selectionArgs.length];
                for(int i = 0, size=selectionArgs.length; i < size; i++) {
                    enc_args[i] = Encryption.encrypt(getPassword(), selectionArgs[i]);
                }
            }

            int rowsAffected = 0;
            if(uriType == SINGLE) {
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsAffected = sqlDB.update(getTable(), Database.encryptValues(getPassword(),contentValues),
                            Database.Field.ID + "=" + id,
                            null);
                } else {
                    rowsAffected = sqlDB.update(getTable(), Database.encryptValues(getPassword(),contentValues),
                            selection + " and " + Database.Field.ID + "=" + id,
                            enc_args);
                }
            } else if(uriType == MULTIPLE) {
                rowsAffected = sqlDB.update(getTable(), contentValues,
                            selection, selectionArgs);
            } else {
                throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
            }

            getContext().getContentResolver().notifyChange(uri, null);
            return rowsAffected;
        }catch(Exception e) {
            Log.e(TAG, "Error updating entry: " + e.toString());
            return 0;
        }
    }
}
