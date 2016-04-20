package com.igordotsenko.dotsenkorssreader;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.igordotsenko.dotsenkorssreader.entities.Channel;
import com.igordotsenko.dotsenkorssreader.entities.DBHandler;
import com.igordotsenko.dotsenkorssreader.entities.Item;

import java.io.IOException;

public class ReaderContentProvider extends ContentProvider {
    private static final int CHANNEL = 1;
    private static final int ITEM = 2;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private DBHandler mDbHandler;
    private SQLiteDatabase mDatabase;

    static {
        sUriMatcher.addURI(ContractClass.AUTHORITY, ContractClass.Channel.TABLE, CHANNEL);
        sUriMatcher.addURI(ContractClass.AUTHORITY, ContractClass.ITEM_TABLE, ITEM);
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
        switch (sUriMatcher.match(uri)) {
            case CHANNEL:
                tableName = ContractClass.Channel.TABLE;
                contentUri = ContractClass.CHANNEL_CONTENT_URI;
                break;
            case ITEM:
                tableName = ContractClass.ITEM_TABLE;
                contentUri = ContractClass.ITEM_CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        mDatabase = mDbHandler.getWritableDatabase();
        rowID = mDatabase.insert(tableName, null, values);
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
        Uri notificationTarget;

        switch ( sUriMatcher.match(uri) ) {
            case CHANNEL:
                tableName = ContractClass.Channel.TABLE;
                notificationTarget = ContractClass.CHANNEL_CONTENT_URI;
                break;
            case ITEM:
                tableName = ContractClass.ITEM_TABLE;
                notificationTarget = ContractClass.ITEM_CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        mDatabase = mDbHandler.getWritableDatabase();
        Cursor cursor = mDatabase.query(
                tableName, projection, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(
                getContext().getContentResolver(), notificationTarget);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String tableName;
        int rowsUpdatedCount;

        switch ( sUriMatcher.match(uri) ) {
            case CHANNEL:
                tableName = ContractClass.Channel.TABLE;
                break;
            case ITEM:
                tableName = ContractClass.ITEM_TABLE;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        mDatabase = mDbHandler.getWritableDatabase();
        rowsUpdatedCount = mDatabase.update(tableName, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdatedCount;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int rowsInserted = 0;
        String tableName;

        switch (sUriMatcher.match(uri)) {
            case ITEM: {
                tableName = ContractClass.ITEM_TABLE;
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        mDatabase = mDbHandler.getWritableDatabase();
        mDatabase.beginTransaction();
        try {
            for (ContentValues cv : values) {
                mDatabase.insertOrThrow(tableName, null, cv);
            }
            mDatabase.setTransactionSuccessful();

            getContext().getContentResolver().notifyChange(uri, null);
            rowsInserted = values.length;
        } finally {
            mDatabase.endTransaction();
        }

        return rowsInserted;
    }

    public static class ContractClass {
        public static final String AUTHORITY = "com.igordotsenko.dotsenkorssreader";

        public static class Channel {
            public static String TABLE = "channel";
            public static final String ID = "_ID";
            public static final String TITLE = "channel_title";
            public static final String LINK = "channel_link";
            public static final String LAST_BUILD_DATE = "channel_last_build_date";
            public static final String LAST_BUILD_DATE_LONG = "channel_last_build_date_long";
        }

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
                "content://" + AUTHORITY + "/" + Channel.TABLE);
        public static final Uri ITEM_CONTENT_URI = Uri.parse(
                "content://" + AUTHORITY + "/" + ITEM_TABLE);
    }
}
