package com.pedal.app.pedal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Akash seth on 12/31/2015.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    public static final int DatabaseVer = 1;
    public static final String DatabaseName = "CycingApp";
    public static final String TableNameLoggedIn = "UserDataLoggedIn";
    public static final String TableNameNotLoggedIn = "UserDataNotLoggedIn";
    public static final String timeElapsed = "timeElapsed";
    public static final String distance = "distance";
    public static final String avgSpeed = "avgSpeed";
    public static final String lastActive = "lastActive";
    public static final String startActivityTime = "startActivityTime";
    public static final String calories = "calories";
    public static final String isSynced = "isSynced";
    public static final String isSyncedToLoggedIn = "isSyncedToLoggedIn";
    public static final String userId = "userId";

    public DatabaseHandler(Context context) {
        super(context, DatabaseName, null, DatabaseVer);

    }

    public void onCreate(SQLiteDatabase db) {
        String createUserDataTableLoggedIn = "CREATE TABLE " + TableNameLoggedIn + "(" + userId + " INTEGER," + distance + " TEXT," + avgSpeed + " TEXT," + timeElapsed + " TEXT," + lastActive + " TEXT," + isSynced + " TEXT," + startActivityTime + " Text," + calories + " Text" + ")";
        db.execSQL(createUserDataTableLoggedIn);
        String createUserDataTableNotLoggedIn = "CREATE TABLE " + TableNameNotLoggedIn + "(" + distance + " TEXT," + avgSpeed + " TEXT," + timeElapsed + " TEXT," + lastActive + " TEXT," + isSyncedToLoggedIn + " TEXT," + startActivityTime + " Text," + calories + " Text" + ")";
        db.execSQL(createUserDataTableNotLoggedIn);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        db.execSQL("DROP TABLE IF EXISTS " + TableNameLoggedIn);
        db.execSQL("DROP TABLE IF EXISTS " + TableNameNotLoggedIn);
        // Create tables again
        onCreate(db);
    }

    public long insertValuesInTableLoggedIn(UserData userData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(userId, userData.getUserId());
        values.put(distance, userData.getDistance());
        values.put(avgSpeed, userData.getAvgSpeed());
        values.put(timeElapsed, userData.getTimeElapsed());
        values.put(lastActive, userData.getLastActive());
        values.put(isSynced, "no");
        values.put(startActivityTime, userData.getStartActivityTime());
        values.put(calories, userData.getCalories());


        long rowId = db.insert(TableNameLoggedIn, null, values);
        db.close();

        return rowId;

    }

    public long insertValuesInTableNotLoggedIn(UserData userData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(distance, userData.getDistance());
        values.put(avgSpeed, userData.getAvgSpeed());
        values.put(timeElapsed, userData.getTimeElapsed());
        values.put(lastActive, userData.getLastActive());
        values.put(isSyncedToLoggedIn, "no");
        values.put(startActivityTime, userData.getStartActivityTime());
        values.put(calories, userData.getCalories());

        long rowId = db.insert(TableNameNotLoggedIn, null, values);
        db.close();

        return rowId;

    }

    public boolean deleteLatestRow(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TableNameLoggedIn, lastActive + "='" + date + "'", null) > 0;
    }

    public Cursor getSingleRowDetailsOfUserNotLoggedIn() {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select *from " + TableNameNotLoggedIn + "", null);
        if (cursor.getCount() > 0) {
            cursor.moveToLast();
        }
        return cursor;

    }

    public Cursor getSingleRowDetailsOfUserLoggedIn() {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select *from " + TableNameLoggedIn + "", null);
        if (cursor.getCount() > 0) {
            cursor.moveToLast();
        }
        return cursor;

    }

    public String composeJson() {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select *from " + TableNameLoggedIn + " where isSynced= '" + "no" + "' ", null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("userId", cursor.getString(0));
                map.put("distance", cursor.getString(1));
                map.put("avgspeed", cursor.getString(2));
                map.put("timeelapsed", cursor.getString(3));
                map.put("time", cursor.getString(4));
                map.put("startTime", cursor.getString(6));
                map.put("calories", cursor.getString(7));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        db.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }

    public ArrayList getMultipleRowsData(int userId) {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select *from " + TableNameLoggedIn + " where userId= " + userId, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("distance", cursor.getString(1));
                map.put("timeelapsed", cursor.getString(3));
                map.put("time", cursor.getString(4));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        db.close();
        return wordList;
    }

    public boolean isAllDbSynced() {
        int count;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select *from " + TableNameLoggedIn + " where isSynced= '" + "no" + "' ", null);
        count = cursor.getCount();
        if (count == 0)
            return true;
        return false;
    }

    public void updateDbSyncStatus(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String updateQuery = "Update " + TableNameLoggedIn + " set " + isSynced + " = '" + "yes" + "' where " + lastActive + "='" + date + "'";
        //Log.d("query", date);
        db.execSQL(updateQuery);
        db.close();
    }

    public void fetchNotSyncedDataFromNotLoggedInTableAndCopyDataToLoggedInTable(int userid) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select *from " + TableNameNotLoggedIn + " where isSyncedToLoggedIn= '" + "no" + "' ", null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("distance", cursor.getString(0));
                map.put("avgspeed", cursor.getString(1));
                map.put("timeelapsed", cursor.getString(2));
                map.put("time", cursor.getString(3));
                map.put("startTime", cursor.getString(5));
                map.put("calories", cursor.getString(6));

                updateDbSyncStatusOfNotLoggedInTable(map.get("time"));
                copyDataToLoggedInTable(userid, map);

            } while (cursor.moveToNext());
        }
        db.close();

    }

    public long copyDataToLoggedInTable(int userid, HashMap<String, String> map) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(userId, userid);
        values.put(distance, map.get("distance"));
        values.put(avgSpeed, map.get("avgspeed"));
        values.put(timeElapsed, map.get("timeelapsed"));
        values.put(lastActive, map.get("time"));
        values.put(isSynced, "no");
        values.put(startActivityTime, map.get("startTime"));
        values.put(calories, map.get("calories"));

        long rowId = db.insert(TableNameLoggedIn, null, values);
        db.close();

        return rowId;

    }

    public boolean isAllDbSyncedOfNotLoggedUser() {
        int count;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select *from " + TableNameNotLoggedIn + " where isSyncedToLoggedIn= '" + "no" + "' ", null);
        count = cursor.getCount();
        if (count == 0)
            return true;
        return false;
    }

    public void updateDbSyncStatusOfNotLoggedInTable(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String updateQuery = "Update " + TableNameNotLoggedIn + " set " + isSyncedToLoggedIn + " = '" + "yes" + "' where " + lastActive + "='" + date + "'";
       // Log.d("query", date);
        db.execSQL(updateQuery);
        db.close();
    }

    public ArrayList fetchAllDataFromNotLoggedInTable() {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select *from " + TableNameNotLoggedIn, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("distance", cursor.getString(0));
                map.put("timeelapsed", cursor.getString(2));
                map.put("time", cursor.getString(3));
                map.put("startTime", cursor.getString(5));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        db.close();
        return wordList;
    }
}