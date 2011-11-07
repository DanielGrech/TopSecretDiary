package com.DGSD.SecretDiary.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.DGSD.SecretDiary.Encryption;

import java.util.Map;
import java.util.Set;

/**
 * Created By: Daniel Grech
 * Date: 1/11/11
 * Description:
 */

public class Database {
	private static final String TAG = Database.class.getSimpleName();

	private final DbHelper mDbHelper;

	private static final int VERSION = 1;

	private static final String DATABASE_NAME = "diary.db";

	private SQLiteDatabase mDatabase;

	public Database(Context context) {
		mDbHelper = new DbHelper(context);

		mDatabase = mDbHelper.getWritableDatabase();

		Log.i(TAG, "Initialized data");
	}

	public DbHelper getDbHelper() {
		return mDbHelper;
	}

	public void close() {
		mDbHelper.close();
	}

	public boolean insert(String table, ContentValues values) {
		try {
			mDatabase.insertOrThrow(table, null, values);
			return true;
		} catch(SQLException e) {
			return false;
		}
	}

    public int update(String table, int id, ContentValues values) {
		return mDatabase.update(table, values, Field.ID + "=" + id, null);
	}

	public int delete(String table, int id) {
		return mDatabase.delete(table, Field.ID + "=" + id, null);
	}

	public Cursor get(String table, int id, String order) {
		return mDatabase.query(table, null, Field.ID + "=" + id, null,
				null, null, order);
	}

	public Cursor getAll(String table, String order) {
		return mDatabase.query(table, null, null, null,
				null, null, order);
	}

    @Override
	protected void finalize() throws Throwable {
	    try {
	        if(mDatabase != null) {
	        	mDatabase.close();
	        	mDbHelper.close();
	        }
	    } finally {
	        super.finalize();
	    }
	}

	// DbHelper implementations
	public class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + Table.DIARY_ENTRIES + " (" +
					Field.ID + " INTEGER PRIMARY KEY, " +
					Field.DATE + " text, " +
					Field.LAT + " text, " +
					Field.LONG + " text, " +
					Field.IMG_URIS + " text, " +
                    Field.RECORDINGS + " text, " +
                    Field.TAGS + " text, " +
					Field.FILES + " text, " +
					Field.TITLE + " text, " +
					Field.TEXT + " text)");

            db.execSQL("CREATE TABLE " + Table.TAGS + " (" +
                       Field.ID + " INTEGER PRIMARY KEY, " +
                       Field.TAG_NAME + " text, " +
                       Field.TAG_COLOR + " text)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("drop table " + Table.DIARY_ENTRIES);
            db.execSQL("drop table " + Table.TAGS);
			this.onCreate(db);
		}
	}

    public static class Ordering {
        public static final String ASC = " ASC";

        public static final String DESC = " DESC";

        public static final String DATE = Field.DATE;

        public static final String ID = Field.ID;

        public static final String TITLE = Field.TITLE;

        public static final String TAG_NAME = Field.TAG_NAME;
    }

    public static class Table {
        public static final String DIARY_ENTRIES = "diary_entries";

        public static final String TAGS = "tags";
    }

    public static class Field {
        //------- Common Fields
        public static final String ID = "_id";

        //------- Fields for Entry Table
        public static final String TITLE = "title";

        public static final String TEXT = "text";

        public static final String DATE = "date";

        public static final String IMG_URIS = "image_uris";

        public static final String RECORDINGS = "recordings";

        public static final String TAGS = "tags";

        public static final String FILES = "files";

        public static final String LAT = "latitude";

        public static final String LONG = "longitude";

        //------- Fields for Tag Table
        public static final String TAG_NAME = "tag_name";

        public static final String TAG_COLOR = "tag_color";
    }

    public static class ArgumentBuilder {
        ContentValues values;

        public ArgumentBuilder() {
            values = new ContentValues();
        }

        public void add(String key, String value) {
            values.put(key, value);
        }

        public ContentValues build() {
            return values;
        }
    }

    public static ContentValues encryptValues(String password, ContentValues input) throws Exception{
        if(input == null) {
            return null;
        }

        Set<Map.Entry<String, Object>> vals=input.valueSet();

        ContentValues retval = new ContentValues();
        for (Map.Entry<String, Object> entry : vals) {
            //Encrypt all entries except for id
            if(entry.getKey() != Database.Field.ID) {
                retval.put(entry.getKey(), Encryption.encrypt(password, (String) entry.getValue()));
            }
        }

        return retval;
    }
}
