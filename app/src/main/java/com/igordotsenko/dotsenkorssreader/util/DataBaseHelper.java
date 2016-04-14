package com.igordotsenko.dotsenkorssreader.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.igordotsenko.dotsenkorssreader.MainActivity;
import com.igordotsenko.dotsenkorssreader.entities.Channel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {
    private String dbPath;
    private String dbName;

    private Context context;
    private SQLiteDatabase dataBaseConnetction;

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

        // Keep context reference for access to assets, get db path and name
        this.context = context;
        dbPath = context.getFilesDir().getPath();
        dbName = name;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public SQLiteDatabase getDataBaseConnection() throws IOException {
        if ( dataBaseConnetction == null ) {
            createDataBase();
            dataBaseConnetction = SQLiteDatabase.openDatabase(dbPath + dbName, null, SQLiteDatabase.OPEN_READWRITE);
        }
        return dataBaseConnetction;
    }

    @Override
    public synchronized void close() {
        if ( dataBaseConnetction != null ) {
            dataBaseConnetction.close();
        }
        super.close();
    }

    public List<Channel> selectAllChannels() throws IOException {
        Log.i(MainActivity.LOG_TAG, "selectAllChannels started");
        List<Channel> channelList = new ArrayList<>();
        String order = "id ASC";

        // Retrieve rows from "channel" table
        Cursor cursor = getDataBaseConnection().query(Channel.TABLE, null, null, null, null, null, order);

        Log.i(MainActivity.LOG_TAG, "Cursor retrieved");

        // If rows returned - fill channels list. Id and title are enough for MainActivity's RecyclerView
        if ( cursor.moveToFirst() ) {
            Log.i(MainActivity.LOG_TAG, "Cursor is not empty");
            int idIndex = cursor.getColumnIndex(Channel.ID);
            int titleIndex = cursor.getColumnIndex(Channel.TITLE);

            Log.i(MainActivity.LOG_TAG, "Start filling channel list");

            do {
                Log.i(MainActivity.LOG_TAG, "id = " + cursor.getInt(idIndex) + " title: " + cursor.getString(titleIndex));
                channelList.add(new Channel(cursor.getInt(idIndex), cursor.getString(titleIndex)));
            }
            while ( cursor.moveToNext() );
        }

        cursor.close();
        Log.i(MainActivity.LOG_TAG, "selectAllChannels finished");
        return channelList;
    }

    private void createDataBase() throws IOException {
        Log.i(MainActivity.LOG_TAG, "createDataBase started");
        // If dbPath exists already - do nothing
        if ( dataBaseExists() ) {
            Log.i(MainActivity.LOG_TAG, "createDataBase: dbPath exists");
            return;
        }

        // Create empty database
        SQLiteDatabase newDB = getReadableDatabase();
        Log.i(MainActivity.LOG_TAG, "createDataBase: empty dbPath created");
        Log.i(MainActivity.LOG_TAG, "createDataBase: empty dbPath path: " + newDB.getPath());

        // Copy dbPath from assets to just created empty dbPath
        copyDataBase();
    }

    private boolean dataBaseExists() {
        String path = dbPath + dbName;
        SQLiteDatabase db_connection;

        // Try to open dbPath. If dbPath does not exits - SQLiteException will be thrown
        try {
            db_connection = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }

        // Close connection if it was established
        if ( db_connection != null ) {
            db_connection.close();
        }

        // Returns true if dbPath exists
        return db_connection != null;
    }

    private void copyDataBase() throws IOException {
        Log.i(MainActivity.LOG_TAG, "copyDataBase started");
        // Open database from assets
        InputStream is = context.getResources().getAssets().open(dbName);

        OutputStream os = new FileOutputStream(dbPath + dbName);
        Log.i(MainActivity.LOG_TAG, "copyDataBase: streams opened");
        Log.i(MainActivity.LOG_TAG, "copyDataBase: coping started");
        byte[] buffer = new byte[1024];
        int byteRead;

        try {
            while ((byteRead = is.read(buffer)) > 0) {
                os.write(buffer, 0, byteRead);
            }
        } catch (IOException e) {
            Log.i(MainActivity.LOG_TAG, "copyDataBase: error during file coping: " + e.getMessage());
            throw new IOException("Error during file coping");
        } finally {
            is.close();
            os.flush();
            os.close();
            Log.i(MainActivity.LOG_TAG, "copyDataBase: streams closed, method finished");
        }
    }
}
