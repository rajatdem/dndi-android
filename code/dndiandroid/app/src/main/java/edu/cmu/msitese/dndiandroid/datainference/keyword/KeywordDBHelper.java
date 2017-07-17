package edu.cmu.msitese.dndiandroid.datainference.keyword;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Yu-Lun Tsai on 28/06/2017.
 */

class KeywordDBHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "keywordsDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    static final String TABLE_COUNTS = "matchCount";

    // Keyword Table Columns
    static final String KEYWORD_ID = "_id";
    static final String KEYWORD_VALUE = "keyword";
    static final String KEYWORD_CATEGORY = "category";
    static final String KEYWORD_COUNT = "count";

    private static KeywordDBHelper sInstance;

    public static synchronized KeywordDBHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new KeywordDBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    // Note: there is another version with a specified database error handler
    private KeywordDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_COUNTS_TABLE = "CREATE TABLE " + TABLE_COUNTS +
                "(" +
                KEYWORD_ID + " INTEGER PRIMARY KEY," +
                KEYWORD_VALUE + " TEXT," +
                KEYWORD_CATEGORY + " TEXT," +
                KEYWORD_COUNT + " INTEGER DEFAULT 0" +
                ")";
        db.execSQL(CREATE_COUNTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_COUNTS);
            onCreate(db);
        }
    }

    public void deleteTable(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COUNTS);
        onCreate(db);
    }
}
