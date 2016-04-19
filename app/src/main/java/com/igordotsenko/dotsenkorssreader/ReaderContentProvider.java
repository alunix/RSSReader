package com.igordotsenko.dotsenkorssreader;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.igordotsenko.dotsenkorssreader.entities.Channel;
import com.igordotsenko.dotsenkorssreader.entities.DBHandler;
import com.igordotsenko.dotsenkorssreader.entities.Item;

import java.io.IOException;

public class ReaderContentProvider extends ContentProvider {
//    private static final Uri CHANNEL_CONTENT_URI = Uri.parse("content://" + ReaderRawData.AUTHORITY + "/" + ReaderRawData.CHANNEL_TABLE);
//    private static final Uri ITEM_CONTENT_URI = Uri.parse("content://" + ReaderRawData.AUTHORITY + "/" + ReaderRawData.ITEM_TABLE);
    private static final int CHANNEL = 1;
    private static final int ITEM = 2;
    private static final UriMatcher uriMatcher= new UriMatcher(UriMatcher.NO_MATCH);

    private DBHandler dbHandler;
    private SQLiteDatabase databaseConnection;

    static {
        uriMatcher.addURI(ReaderRawData.AUTHORITY, ReaderRawData.CHANNEL_TABLE, CHANNEL);
        uriMatcher.addURI(ReaderRawData.AUTHORITY, ReaderRawData.ITEM_TABLE, ITEM);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID;
        Uri resultUri;
        String tableName;
        Uri contentUri;
        switch (uriMatcher.match(uri)) {
            case CHANNEL:
                tableName = ReaderRawData.CHANNEL_TABLE;
                contentUri = ReaderRawData.CHANNEL_CONTENT_URI;
                break;
            case ITEM:
                tableName = ReaderRawData.ITEM_TABLE;
                contentUri = ReaderRawData.ITEM_CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        establishDatabaseConnection();
        rowID = databaseConnection.insert(tableName, null, values);
        resultUri = ContentUris.withAppendedId(contentUri, rowID);
        // Notify content resolver that data changes in resultUri address occured
        getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }

    @Override
    public boolean onCreate() {
        try {
            dbHandler = new DBHandler(getContext(), MainActivity.DB_NAME, null, MainActivity.DB_VERSION);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String tableName;

        switch ( uriMatcher.match(uri) ) {
            case CHANNEL:
                tableName = ReaderRawData.CHANNEL_TABLE;
                Log.i(ItemListActivity.ITEM_LIST_TAG, "Content provider query() match channel");
                break;
            case ITEM:
                tableName = ReaderRawData.ITEM_TABLE;
                Log.i(ItemListActivity.ITEM_LIST_TAG, "Content provider query() match item");
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        establishDatabaseConnection();
        Cursor cursor = databaseConnection.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
        // Ask ContentReslover to notify this cursor about any data changes in ITEM_CONTENT_URI
        cursor.setNotificationUri(getContext().getContentResolver(), ReaderRawData.ITEM_CONTENT_URI);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String tableName;
        int rowsUpdatedCount;

        switch ( uriMatcher.match(uri) ) {
            case CHANNEL:
                tableName = ReaderRawData.CHANNEL_TABLE;
                break;
            case ITEM:
                tableName = ReaderRawData.ITEM_TABLE;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        establishDatabaseConnection();
        rowsUpdatedCount = databaseConnection.update(tableName, values, selection, selectionArgs);
        // Notify content resolver, that data by uri address was changed
        getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdatedCount;
    }

    private void establishDatabaseConnection() {
        if ( databaseConnection == null ) {
            try {
                databaseConnection = dbHandler.getDatabaseConnection();
            } catch (IOException e) {
                e.printStackTrace();
                throw new Error("Error during database connection establishing");
            }
        }
    }

    public static class ReaderRawData {
        public static final String AUTHORITY = "com.igordotsenko.dotsenkorssreader";

        public static String CHANNEL_TABLE = Channel.TABLE;
        public static final String CHANNEL_ID = Channel.ID;
        public static final String CHANNEL_TITLE = Channel.TITLE;
        public static final String CHANNEL_LINK = Channel.LINK;
        public static final String CHANNEL_LAST_BUILD_DATE = Channel.LAST_BUILD_DATE;
        public static final String CHANNEL_LAST_BUILD_DATE_LONG = Channel.LAST_BUILD_DATE_LONG;

        public static final String ITEM_TABLE = Item.TABLE;
        public static final String ITEM_ID = Item.ID;
        public static final String ITEM_CHANNEL_ID = Item.CHANNEL_ID;
        public static final String ITEM_TITLE = Item.TITLE;
        public static final String ITEM_LINK = Item.LINK;
        public static final String ITEM_DESCRIPTION = Item.DESCRIPTION;
        public static final String ITEM_PUBDATE = Item.PUBDATE;
        public static final String ITEM_PUBDATE_LONG = Item.PUBDATE_LONG;
        public static final String ITEM_THUMBNAIL = Item.THUMBNAIL;

        public static final Uri CHANNEL_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CHANNEL_TABLE);
        public static final Uri ITEM_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + ITEM_TABLE);
    }
}
