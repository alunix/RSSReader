package com.igordotsenko.dotsenkorssreader.entities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.igordotsenko.dotsenkorssreader.ReaderContentProvider;
import com.igordotsenko.dotsenkorssreader.ReaderContentProvider.ContractClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {
    public DBHandler(
            Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
            throws IOException {

        super(context, name, factory, version);

        getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Channel.createChannelTable);
        db.execSQL(Channel.inserIntoChannel);
        db.execSQL(Item.createItemTable);
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

    public static Channel insertIntoChannel(Channel channel, ContentResolver contentResolver) {
        long id = getLastChannelId(contentResolver) + 1;
        channel.setId(id);

        ContentValues cv = channelToContentValues(channel);

        contentResolver.insert(ContractClass.CHANNEL_CONTENT_URI, cv);

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

    public static List<Integer> getChannelIds(Context context) {
        String projection[] = { ContractClass.Channel.ID };
        Cursor cursor = context.getContentResolver()
                .query(ContractClass.CHANNEL_CONTENT_URI, projection, null, null, null, null);

        List<Integer> ids = new ArrayList<Integer>();

        if ( cursor.moveToFirst() ) {
            int idColumnIndex = cursor
                    .getColumnIndex(ContractClass.Channel.ID);

            ids = new ArrayList<>();

            do {
                ids.add(cursor.getInt(idColumnIndex));
            } while ( cursor.moveToNext() );
        }

        cursor.close();

        return ids;
    }

    public static void updateChannel(int channelId, Parser parser, ContentResolver contentResolver) throws IOException {
        Channel currentChannel = selectCurrentChannel(channelId, contentResolver);
        Channel updatedChannel = parser.updateExistChannel(currentChannel);

        if ( updatedChannel != null) {
            updateChannelBuiltDate(updatedChannel, channelId, contentResolver);

            // Filter newItemsList by publication date, insert them into DB
            handleNewItems(updatedChannel, channelId, contentResolver);
        }
    }

    private static Channel selectCurrentChannel(long channelId, ContentResolver contentResolver) {
        String selection = ContractClass.Channel.ID + " = ?";
        String[] selectionArgs = { "" + channelId};

        Cursor cursor = contentResolver.query(
                ContractClass.CHANNEL_CONTENT_URI, null, selection, selectionArgs, null);

        cursor.moveToFirst();

        int titleIndex = cursor.getColumnIndex(ContractClass.Channel.TITLE);
        int linkIndex = cursor.getColumnIndex(ContractClass.Channel.LINK);
        int lastBuilDateIndex = cursor.getColumnIndex(ContractClass.Channel.LAST_BUILD_DATE);

        Channel selectedChanndel = new Channel(
                cursor.getString(titleIndex),
                cursor.getString(linkIndex),
                cursor.getString(lastBuilDateIndex));

        cursor.close();

        return selectedChanndel;
    }

    private static void updateChannelBuiltDate(
            Channel updatedChannel, int channelId, ContentResolver contentResolver) {

        ContentValues contentValuesDateString = new ContentValues();
        ContentValues contentValuesDateLong = new ContentValues();

        contentValuesDateString.put(
                ContractClass.Channel.LAST_BUILD_DATE,
                updatedChannel.getLastBuildDate());

        contentValuesDateLong.put(
                ContractClass.Channel.LAST_BUILD_DATE_LONG,
                updatedChannel.getLastBuildDateLong());

        contentResolver.update(
                ContractClass.CHANNEL_CONTENT_URI,
                contentValuesDateString,
                ContractClass.Channel.ID + " = " + channelId,
                null);

        contentResolver.update(
                ContractClass.CHANNEL_CONTENT_URI,
                contentValuesDateLong,
                ContractClass.Channel.ID + " = " + channelId,
                null);
    }

    private static void handleNewItems(
            Channel updatedChannel, long channelId, ContentResolver contentResolver) {
        List<Item> newItemList = updatedChannel.getItems();
        long lastPubdateLong = getLastItemPubdateLong(contentResolver);
        long lastItemId = getLastItemId(contentResolver);

        // Returns filtered itemList with set IDs
        newItemList = filterItemList(newItemList, lastPubdateLong, lastItemId);

        if ( newItemList.size() > 0 ) {
            insertNewItems(newItemList, channelId, contentResolver);
        }
    }

    private static long getLastItemPubdateLong(ContentResolver contentResolver) {
        String projection[] = { ContractClass.Item.PUBDATE_LONG };
        String order = ContractClass.Item.PUBDATE_LONG + " DESC";
        long lastItemPubdateLong = 0;

        Cursor cursor = contentResolver.query(
                ContractClass.ITEM_CONTENT_URI, projection, null, null, order);

        if ( cursor.moveToFirst() ) {
            int pubdateIndex = cursor.getColumnIndex(
                    ContractClass.Item.PUBDATE_LONG);

            lastItemPubdateLong = cursor.getLong(pubdateIndex);
        }

        cursor.close();

        return lastItemPubdateLong;
    }

    private static long getLastItemId(ContentResolver contentResolver) {
        String projection[] = { ContractClass.Item.ID };
        String order = ContractClass.Item.ID + " DESC";
        return selectId(contentResolver, ContractClass.ITEM_CONTENT_URI, projection, order);
    }

    private static long getLastChannelId(ContentResolver contentResolver) {
        String[] projection = { ContractClass.Channel.ID };
        String order = ContractClass.Channel.ID + " DESC";
        return selectId(contentResolver, ContractClass.CHANNEL_CONTENT_URI, projection, order);
    }

    private static long selectId(
            ContentResolver contentResolver, Uri uri, String[] projection, String order) {

        Cursor cursor = contentResolver.query(uri, projection, null, null, order);
        long id = 0;

        if ( cursor.moveToFirst() ) {
            id = cursor.getLong(cursor.getColumnIndex(projection[0]));
        }

        cursor.close();

        return id;
    }

    private static List<Item> filterItemList(
            List<Item> newItemList, long lastPubdateLong, long lastItemId) {

        List<Item> filteredNewItemList = new ArrayList<>();

        for ( Item item : newItemList ) {
            if ( item.getPubDateLong() > lastPubdateLong ) {
                item.setId(++lastItemId);
                filteredNewItemList.add(item);
            }
        }

        return filteredNewItemList;
    }

    private static void insertNewItems(
            List<Item> newItemList, long channelId, ContentResolver contentResolver) {
        ContentValues contentValues = new ContentValues();
        Collections.sort(newItemList);

        for ( Item item : newItemList ) {
            contentValues = itemToContentValues(item, channelId);

            if ( item.getThumbNailUrl() != null ) {
                contentValues.put(
                        ContractClass.Item.THUMBNAIL, item.getThumbNailUrl());
            }

            contentResolver.insert(ContractClass.ITEM_CONTENT_URI, contentValues);

            contentValues.clear();
        }
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

        if ( item.getThumbNailUrl() != null ) {
            cv.put(ContractClass.Item.THUMBNAIL, item.getThumbNailUrl());
        }

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