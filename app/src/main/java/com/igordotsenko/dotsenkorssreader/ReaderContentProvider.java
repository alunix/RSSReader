package com.igordotsenko.dotsenkorssreader;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.igordotsenko.dotsenkorssreader.entities.DBHandler;

import java.io.IOException;

public class ReaderContentProvider extends ContentProvider {
    private static final int CHANNEL = 1;
    private static final int ITEM = 2;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String DB_NAME = "rss_reader.db";
    public static final int DB_VERSION = 1;

    private DBHandler mDbHandler;
    private SQLiteDatabase mDatabase;

    static {
        sUriMatcher.addURI(ContractClass.AUTHORITY, ContractClass.Channel.TABLE, CHANNEL);
        sUriMatcher.addURI(ContractClass.AUTHORITY, ContractClass.Item.TABLE, ITEM);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowDeleted = 0;

        switch ( sUriMatcher.match(uri) ) {
            case CHANNEL:
                // Remove Channel
                mDatabase = mDbHandler.getWritableDatabase();
                rowDeleted = mDatabase.delete(ContractClass.Channel.TABLE, selection, selectionArgs);

                //Remove items of this channel
                selection = ContractClass.Item.CHANNEL_ID + " = ?";
                mDatabase.delete(ContractClass.Item.TABLE, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(ContractClass.CHANNEL_CONTENT_URI, null);
                getContext().getContentResolver().notifyChange(ContractClass.ITEM_CONTENT_URI, null);
                break;
        }

        return rowDeleted;
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
                tableName = ContractClass.Item.TABLE;
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
                    getContext(), DB_NAME, null, DB_VERSION);
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
                tableName = ContractClass.Item.TABLE;
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
                tableName = ContractClass.Item.TABLE;
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
                tableName = ContractClass.Item.TABLE;
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

        public static final Uri CHANNEL_CONTENT_URI = Uri.parse(
                "content://" + AUTHORITY + "/" + Channel.TABLE);

        public static final Uri ITEM_CONTENT_URI = Uri.parse(
                "content://" + AUTHORITY + "/" + Item.TABLE);

        public static class Channel {
            public static String TABLE = "channel";
            public static final String ID = "_ID";
            public static final String TITLE = "channel_title";
            public static final String LINK = "channel_link";
            public static final String LAST_BUILD_DATE = "channel_last_build_date";
            public static final String LAST_BUILD_DATE_LONG = "channel_last_build_date_long";
        }

        public static class Item {
            public static final String TABLE = "item";
            public static final String ID = "_ID";
            public static final String CHANNEL_ID = "item_channel_id";
            public static final String TITLE = "item_title";
            public static final String LINK = "item_link";
            public static final String DESCRIPTION = "item_description";
            public static final String PUBDATE = "item_pubdate";
            public static final String PUBDATE_LONG = "item_pubdate_long";
            public static final String THUMBNAIL = "item_thumbnail_url";
            public static final String SUBTITLE = "item_subtitle";
        }

    }
}
