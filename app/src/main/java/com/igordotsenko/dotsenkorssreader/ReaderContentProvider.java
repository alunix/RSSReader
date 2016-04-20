package com.igordotsenko.dotsenkorssreader;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.igordotsenko.dotsenkorssreader.entities.Channel;
import com.igordotsenko.dotsenkorssreader.entities.DBHandler;
import com.igordotsenko.dotsenkorssreader.entities.Item;

import java.io.IOException;

public class ReaderContentProvider extends ContentProvider {
    private static final int CHANNEL = 1;
    private static final int ITEM = 2;
    private static final UriMatcher uriMatcher= new UriMatcher(UriMatcher.NO_MATCH);

    private DBHandler mDbHandler;
    private SQLiteDatabase mDatabaseConnection;

    static {
        uriMatcher.addURI(ContractClass.AUTHORITY, ContractClass.CHANNEL_TABLE, CHANNEL);
        uriMatcher.addURI(ContractClass.AUTHORITY, ContractClass.ITEM_TABLE, ITEM);
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
                tableName = ContractClass.CHANNEL_TABLE;
                contentUri = ContractClass.CHANNEL_CONTENT_URI;
                break;
            case ITEM:
                tableName = ContractClass.ITEM_TABLE;
                contentUri = ContractClass.ITEM_CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        establishDatabaseConnection();
        rowID = mDatabaseConnection.insert(tableName, null, values);
        resultUri = ContentUris.withAppendedId(contentUri, rowID);
        getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }

    @Override
    public boolean onCreate() {
        try {
            mDbHandler = new DBHandler(
                    getContext(), MainActivity.DB_NAME, null, MainActivity.DB_VERSION);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        String tableName;

        switch ( uriMatcher.match(uri) ) {
            case CHANNEL:
                tableName = ContractClass.CHANNEL_TABLE;
                break;
            case ITEM:
                tableName = ContractClass.ITEM_TABLE;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        establishDatabaseConnection();
        Cursor cursor = mDatabaseConnection.query(
                tableName, projection, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(
                getContext().getContentResolver(), ContractClass.ITEM_CONTENT_URI);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String tableName;
        int rowsUpdatedCount;

        switch ( uriMatcher.match(uri) ) {
            case CHANNEL:
                tableName = ContractClass.CHANNEL_TABLE;
                break;
            case ITEM:
                tableName = ContractClass.ITEM_TABLE;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        establishDatabaseConnection();
        rowsUpdatedCount = mDatabaseConnection.update(tableName, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdatedCount;
    }

    private void establishDatabaseConnection() {
        if ( mDatabaseConnection == null ) {
            try {
                mDatabaseConnection = mDbHandler.getDatabaseConnection();
            } catch (IOException e) {
                e.printStackTrace();
                throw new Error("Error during database connection establishing");
            }
        }
    }

    public static class ContractClass {
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

        public static final Uri CHANNEL_CONTENT_URI = Uri.parse(
                "content://" + AUTHORITY + "/" + CHANNEL_TABLE);
        public static final Uri ITEM_CONTENT_URI = Uri.parse(
                "content://" + AUTHORITY + "/" + ITEM_TABLE);
    }
}
