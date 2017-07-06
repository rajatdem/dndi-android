package edu.cmu.msitese.dndiandroid.datainference.keyword;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.security.Key;
import java.util.List;

/**
 * Created by Yu-Lun Tsai on 28/06/2017.
 */

public class KeywordCountDao {

    private static final String TAG = "ZIRK";

    // Database fields
    private SQLiteDatabase database;
    private KeywordDBHelper dbHelper;
    private String[] allColumns = {
            KeywordDBHelper.KEYWORD_ID,
            KeywordDBHelper.KEYWORD_VALUE,
            KeywordDBHelper.KEYWORD_CATEGORY,
            KeywordDBHelper.KEYWORD_COUNT,};

    public KeywordCountDao(Context context){
        dbHelper = KeywordDBHelper.getInstance(context);
    }

    private int getKeywordID(SQLiteDatabase db, String keyword, String category){

        int res = -1;
        Cursor cursor = db.query(KeywordDBHelper.TABLE_COUNTS,
                new String[]{KeywordDBHelper.KEYWORD_ID},
                KeywordDBHelper.KEYWORD_VALUE + " =? AND " + KeywordDBHelper.KEYWORD_CATEGORY + " =?",
                new String[]{keyword, category},
                null,null,null,null);

        if (cursor.moveToFirst()) {
            //if the row exist then return the id
            res = cursor.getInt(cursor.getColumnIndex(KeywordDBHelper.KEYWORD_ID));
        }
        if(!cursor.isClosed()) {
            cursor.close();
        }

        return res;
    }

    public void addOrUpdateKeywordCount(List<Keyword> keywords){
        for(Keyword keyword: keywords){
            addOrUpdateKeywordCount(keyword.keyword, keyword.category);
        }
    }

    /**
     * @param keyword
     * @param category
     */
    public void addOrUpdateKeywordCount(String keyword, String category){

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            db.beginTransaction();

            int id = getKeywordID(db, keyword, category);
            if (id == -1) {
                values.put(KeywordDBHelper.KEYWORD_VALUE, keyword);
                values.put(KeywordDBHelper.KEYWORD_CATEGORY, category);
                values.put(KeywordDBHelper.KEYWORD_COUNT, Integer.toString(1));
                db.insert(KeywordDBHelper.TABLE_COUNTS, null, values);
            } else {
                Cursor cursor = db.query(KeywordDBHelper.TABLE_COUNTS,
                        new String[]{KeywordDBHelper.KEYWORD_COUNT},
                        KeywordDBHelper.KEYWORD_ID + "=?",
                        new String[]{Integer.toString(id)},
                        null, null, null, null);

                cursor.moveToFirst();
                int count = cursor.getInt(cursor.getColumnIndex(KeywordDBHelper.KEYWORD_COUNT));

                if (!cursor.isClosed()) {
                    cursor.close();
                }
                values.put(KeywordDBHelper.KEYWORD_COUNT, Integer.toString(count + 1));
                db.update(KeywordDBHelper.TABLE_COUNTS, values, KeywordDBHelper.KEYWORD_ID + "=?", new String[]{Integer.toString(id)});
            }
            db.setTransactionSuccessful();
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
        }
        finally {
            db.endTransaction();
            db.close();
        }
    }

    public int getKeywordMatchCount(String keyword){

        int total = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(KeywordDBHelper.TABLE_COUNTS,
                new String[]{KeywordDBHelper.KEYWORD_COUNT},
                KeywordDBHelper.KEYWORD_VALUE + " =?",
                new String[]{keyword},
                null, null, null, null);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            // do what you need with the cursor here
            total = total + cursor.getInt(cursor.getColumnIndex(KeywordDBHelper.KEYWORD_COUNT));
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }
        db.close();
        return total;
    }

    public int getCategoryMatchCount(String category){

        int total = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(KeywordDBHelper.TABLE_COUNTS,
                new String[]{KeywordDBHelper.KEYWORD_COUNT},
                KeywordDBHelper.KEYWORD_CATEGORY + " =?",
                new String[]{category},
                null, null, null, null);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            // do what you need with the cursor here
            total = total + cursor.getInt(cursor.getColumnIndex(KeywordDBHelper.KEYWORD_COUNT));
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }
        db.close();
        return total;
    }

    // for debug purpose
    public void printContentToConsole(){

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(KeywordDBHelper.TABLE_COUNTS,
                allColumns, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            while(!cursor.isAfterLast()){
                // do what you need with the cursor here
                String keyword = cursor.getString(cursor.getColumnIndex(KeywordDBHelper.KEYWORD_VALUE));
                String category = cursor.getString(cursor.getColumnIndex(KeywordDBHelper.KEYWORD_CATEGORY));
                String count = cursor.getString(cursor.getColumnIndex(KeywordDBHelper.KEYWORD_COUNT));
                Log.i(TAG, String.format("Keyword: %s, Category: %s, Count: %s", keyword, category, count));

                cursor.moveToNext();
            }
        }
        else{
            Log.i(TAG, "No data in the database now.");
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }
        db.close();
    }

    public void clearTable(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.deleteTable(db);
        db.close();
    }
}
