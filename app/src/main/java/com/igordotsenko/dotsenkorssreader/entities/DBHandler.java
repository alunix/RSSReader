package com.igordotsenko.dotsenkorssreader.entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.igordotsenko.dotsenkorssreader.ReaderContentProvider;
import com.igordotsenko.dotsenkorssreader.ReaderContentProvider.ContractClass;

import java.io.IOException;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {
    private SQLiteDatabase databaseConnection;

    public DBHandler(
            Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
            throws IOException {

        super(context, name, factory, version);

        databaseConnection = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createChannelTable = "CREATE TABLE " + ContractClass.Channel.TABLE + "("
                + ContractClass.Channel.ID + " INTEGER PRIMARY KEY, "
                + ContractClass.Channel.TITLE + " TEXT NOT NULL, "
                + ContractClass.Channel.LINK + " TEXT NOT NULL, "
                + ContractClass.Channel.LAST_BUILD_DATE + " TEXT,"
                + ContractClass.Channel.LAST_BUILD_DATE_LONG + " INTEGER, "
                + "unique(channel_link));";

        String createItemTable = "CREATE TABLE " + ContractClass.Item.TABLE + "("
                + ContractClass.Item.ID + " INTEGER PRIMARY KEY, "
                + ContractClass.Item.CHANNEL_ID + " INTEGER NOT NULL, "
                + ContractClass.Item.TITLE + " TEXT NOT NULL, "
                + ContractClass.Item.LINK + " TEXT NOT NULL, "
                + ContractClass.Item.DESCRIPTION + " TEXT NOT NULL, "
                + ContractClass.Item.PUBDATE + " TEXT, "
                + ContractClass.Item.PUBDATE_LONG + " INTEGER, "
                + ContractClass.Item.THUMBNAIL + " TEXT, "
                + "FOREIGN KEY("+ ContractClass.Item.CHANNEL_ID + ") REFERENCES "
                + ContractClass.Channel.TABLE + "(" + ContractClass.Channel.ID +"));";

        String inserIntoChannel = "INSERT INTO " + ContractClass.Channel.TABLE
                + "(" + ContractClass.Channel.ID + ", "
                + ContractClass.Channel.TITLE + ", "
                + ContractClass.Channel.LINK + ") "
                + "VALUES (1, \"BBC NEWS\", \"http://feeds.bbci.co.uk/news/rss.xml\");";

        db.execSQL(createChannelTable);
        db.execSQL(createItemTable);
        db.execSQL(inserIntoChannel);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public static boolean channelIsAlreadyAdded(String url, Context context){
        String selection = ContractClass.Channel.LINK + " = ?";
        String[] selectionArgs = { url };

        return checkChannel(context, selection, selectionArgs);
    }

    public static boolean channelIsAlreadyAdded(Channel channel, Context context){
        String selection = ContractClass.Channel.TITLE + " = ?";
        String[] selectionArgs = { channel.getTitle() };

        return checkChannel(context, selection, selectionArgs);
    }

    public static Channel insertIntoChannel(Channel channel, Context context) {
        long id = getLastChannelId(context) + 1;
        channel.setId(id);

        ContentValues cv = channelToContentValues(channel);

        context.getContentResolver().insert(
                ContractClass.CHANNEL_CONTENT_URI, cv);

        return channel;
    }

    public static void insertIntoItem(List<Item> items, long channelId, Context context) {
        ContentValues[] values = new ContentValues[items.size()];

        for ( int i = 0; i < items.size(); i++ ) {
            ContentValues cv = itemToContentValues(items.get(i), channelId);

            values[i] = cv;
        }

        context.getContentResolver().bulkInsert(
                ReaderContentProvider.ContractClass.ITEM_CONTENT_URI, values);
    }

    private static long getLastChannelId(Context context) {
        String[] projection = { ContractClass.Channel.ID };
        String order = ContractClass.Channel.ID + " DESC";
        Cursor cursor = context.getContentResolver().query(
                ReaderContentProvider.ContractClass.CHANNEL_CONTENT_URI,
                projection, null, null, order);

        int idIndex = cursor.getColumnIndex(ContractClass.Channel.ID);

        cursor.moveToFirst();
        long id = cursor.getLong(idIndex);

        cursor.close();

        return id;
    }

    private static ContentValues channelToContentValues(Channel channel) {
        ContentValues cv = new ContentValues();

        cv.put(ContractClass.Channel.ID, channel.getId());
        cv.put(ContractClass.Channel.TITLE, channel.getTitle());
        cv.put(ContractClass.Channel.LINK, channel.getLink());
        cv.put(ContractClass.Channel.LAST_BUILD_DATE, channel.getLastBuildDate());
        cv.put(ContractClass.Channel.LAST_BUILD_DATE_LONG, channel.getLastBuildDateLong());

        return cv;
    }

    private static ContentValues itemToContentValues(Item item, long channelId) {
        ContentValues cv = new ContentValues();

        cv.put(ContractClass.Item.CHANNEL_ID, channelId);
        cv.put(ContractClass.Item.LINK, item.getLink());
        cv.put(ContractClass.Item.TITLE, item.getTitle());
        cv.put(ContractClass.Item.DESCRIPTION, item.getContent());
        cv.put(ContractClass.Item.PUBDATE, item.getPubDate());
        cv.put(ContractClass.Item.PUBDATE_LONG, item.getPubDateLong());

        return cv;
    }

    private static boolean checkChannel(Context context, String selection, String[] selectionArgs) {
        Cursor cursor = context.getContentResolver().query(
                ReaderContentProvider.ContractClass.CHANNEL_CONTENT_URI,
                null, selection, selectionArgs, null);

        // If records exists - cursor has more than 0 rows
        boolean recordExists = cursor.getCount() > 0;

        cursor.close();

        return recordExists;
    }
}