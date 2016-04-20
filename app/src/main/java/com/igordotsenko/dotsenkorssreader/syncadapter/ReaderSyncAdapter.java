package com.igordotsenko.dotsenkorssreader.syncadapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import static com.igordotsenko.dotsenkorssreader.ReaderContentProvider.ContractClass;
import com.igordotsenko.dotsenkorssreader.ItemListActivity;
import com.igordotsenko.dotsenkorssreader.ReaderContentProvider;
import com.igordotsenko.dotsenkorssreader.entities.Channel;
import com.igordotsenko.dotsenkorssreader.entities.Item;
import com.igordotsenko.dotsenkorssreader.entities.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReaderSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String AUTHORITY = ContractClass.AUTHORITY;
    private static final Uri CHANNEL_CONTENT_URI = Uri.parse(
            "content://" + AUTHORITY + "/" + ContractClass.Channel.TABLE);

    private static final Uri ITEM_CONTENT_URI = Uri.parse(
            "content://" + AUTHORITY + "/" + ContractClass.Item.TABLE);

    private Context mContext;
    private ContentResolver mContentResolver;

    public ReaderSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(
            Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {

        Log.i(ItemListActivity.ITEM_LIST_TAG, "onPerformSync started");
        Parser parser = new Parser();
        List<Integer> ids;

        // Retrieve ids of channels that should be updated
        ids = getChannelIds();


        //Try to update feeds
        for ( int channelId : ids ) {
            try {
                Log.i(ItemListActivity.ITEM_LIST_TAG, "try to update channel: " + channelId);
                updateChannel(channelId, parser);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    //TODO move these methods to DBHandler
    private List<Integer> getChannelIds() {
        String projection[] = { ContractClass.Channel.ID };
        Cursor cursor = mContext.getContentResolver()
                .query(CHANNEL_CONTENT_URI, projection, null, null, null, null);

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

    private void updateChannel(int channelId, Parser parser) throws IOException {
        Channel currentChannel = selectCurrentChannel(channelId);
        Channel updatedChannel = parser.updateExistChannel(currentChannel);

        if ( updatedChannel != null) {
            updateChannelBuiltDate(updatedChannel, channelId);

            // Filter newItemsList by publication date, insert them into DB
            handleNewItems(updatedChannel, channelId);
        }
    }

    private Channel selectCurrentChannel(long channelId) {
        String selection = ContractClass.Channel.ID + " = ?";
        String[] selectionArgs = { "" + channelId};

        Cursor cursor = mContentResolver.query(
                CHANNEL_CONTENT_URI, null, selection, selectionArgs, null);

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

    private void updateChannelBuiltDate(Channel updatedChannel, int channelId) {
        ContentValues contentValuesDateString = new ContentValues();
        ContentValues contentValuesDateLong = new ContentValues();

        contentValuesDateString.put(
                ContractClass.Channel.LAST_BUILD_DATE,
                updatedChannel.getLastBuildDate());

        contentValuesDateLong.put(
                ContractClass.Channel.LAST_BUILD_DATE_LONG,
                updatedChannel.getLastBuildDateLong());

        mContentResolver.update(
                CHANNEL_CONTENT_URI,
                contentValuesDateString,
                ContractClass.Channel.ID + " = " + channelId,
                null);

        mContentResolver.update(
                CHANNEL_CONTENT_URI,
                contentValuesDateLong,
                ContractClass.Channel.ID + " = " + channelId,
                null);
    }

    private void handleNewItems(Channel updatedChannel, long channelId) {
        List<Item> newItemList = updatedChannel.getItems();
        long lastPubdateLong = getLastItemPubdateLong();
        long lastItemId = getLastItemId();

        // Returns filtered itemList with set IDs
        newItemList = filterItemList(newItemList, lastPubdateLong, lastItemId);

        if ( newItemList.size() > 0 ) {
            insertNewItems(newItemList, channelId);
        }
    }

    private long getLastItemPubdateLong() {
        String projection[] = { ContractClass.Item.PUBDATE_LONG };
        String order = ContractClass.Item.PUBDATE_LONG + " DESC";
        long lastItemPubdateLong = 0;

        Cursor cursor = mContext.getContentResolver().query(
                ITEM_CONTENT_URI, projection, null, null, order);

        if ( cursor.moveToFirst() ) {
            int pubdateIndex = cursor.getColumnIndex(
                    ContractClass.Item.PUBDATE_LONG);

            lastItemPubdateLong = cursor.getLong(pubdateIndex);
        }

        cursor.close();

        return lastItemPubdateLong;
    }

    private long getLastItemId() {
        String projection[] = { ContractClass.Item.ID };
        String order = ContractClass.Item.ID + " DESC";
        long lastItemPubdateLong = 0;

        Cursor cursor = mContext.getContentResolver().query(
                ITEM_CONTENT_URI, projection, null, null, order);

        if ( cursor.moveToFirst() ) {
            int pubdateIndex = cursor.getColumnIndex(ContractClass.Item.ID);
            lastItemPubdateLong = cursor.getLong(pubdateIndex);
        }

        cursor.close();

        return lastItemPubdateLong;
    }

    private List<Item> filterItemList(
            List<Item> newItemList,
            long lastPubdateLong,
            long lastItemId) {

        List<Item> filteredNewItemList = new ArrayList<>();

        for ( Item item : newItemList ) {
            if ( item.getPubDateLong() > lastPubdateLong ) {
                item.setId(++lastItemId);
                filteredNewItemList.add(item);
            }
        }

        return filteredNewItemList;
    }

    private void insertNewItems(List<Item> newItemList, long channelId) {
        ContentValues contentValues = new ContentValues();
        Collections.sort(newItemList);

        for ( Item item : newItemList ) {
            contentValues.put(
                    ContractClass.Item.ID, item.getId());

            contentValues.put(
                    ContractClass.Item.CHANNEL_ID, channelId);

            contentValues.put(
                    ContractClass.Item.TITLE, item.getTitle());

            contentValues.put(
                    ContractClass.Item.LINK, item.getLink());

            contentValues.put(
                    ContractClass.Item.DESCRIPTION, item.getContent());

            contentValues.put(
                    ContractClass.Item.PUBDATE, item.getPubDate());

            contentValues.put(
                    ContractClass.Item.PUBDATE_LONG, item.getPubDateLong());

            if ( item.getThumbNailUrl() != null ) {
                contentValues.put(
                        ContractClass.Item.THUMBNAIL, item.getThumbNailUrl());
            }

            mContentResolver.insert(ITEM_CONTENT_URI, contentValues);

            contentValues.clear();
        }
    }
}
