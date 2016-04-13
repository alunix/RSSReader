package com.igordotsenko.dotsenkorssreader.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.igordotsenko.dotsenkorssreader.entities.Channel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static String DB_PATH = "/data/data/com.com.igordotsenko.dotsenkorssreader/databases/";
    private static String DB_NAME = "rss_reader.db";

    private Context context;
    private SQLiteDatabase dataBaseConnetction;

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

        // Keep context reference for access to assets
        this.context = context;
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
            dataBaseConnetction = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
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
        List<Channel> channelList = new ArrayList<>();
        String order = "ORDER by id ASC";

        // Retrieve rows from "channel" table
        Cursor cursor = getDataBaseConnection().query(Channel.TABLE, null, null, null, null, null, order);

        // If rows returned - fill channels list. Id and title are enough for MainActivity's RecyclerView
        if ( cursor.moveToFirst() ) {
            int idIndex = cursor.getColumnIndex(Channel.ID);
            int titleIndex = cursor.getColumnIndex(Channel.TITLE);

            while ( cursor.moveToNext() ) {
                channelList.add(new Channel(cursor.getInt(idIndex), cursor.getString(titleIndex)));
            }

        }

        cursor.close();

        return channelList;
    }

    private void createDataBase() throws IOException {
        // If db exists already - do nothing
        if ( dataBaseExists() ) {
            return;
        }

        // Create empty database
        getReadableDatabase();

        // Copy db from assets to just created empty db
        copyDataBase();
    }

    private boolean dataBaseExists() {
        String path = DB_PATH + DB_NAME;
        SQLiteDatabase db_connection;

        // Try to open db. If db does not exits - SQLiteException will be thrown
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

        // Returns true if db exists
        return db_connection != null;
    }

    private void copyDataBase() throws IOException {
        // Open database from assets
        InputStream is = context.getResources().getAssets().open(DB_NAME);

        OutputStream os = new FileOutputStream(DB_PATH + DB_NAME);

        byte[] buffer = new byte[1024];
        int byteRead;

        try {
            while ((byteRead = is.read(buffer)) > 0) {
                os.write(buffer, 0, byteRead);
            }
        } catch (IOException e) {
            throw new IOException("Error during file coping");
        } finally {
            is.close();
            os.flush();
            os.close();
        }
    }
}
