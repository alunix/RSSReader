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

import com.igordotsenko.dotsenkorssreader.ItemListActivity;
import com.igordotsenko.dotsenkorssreader.entities.Channel;
import com.igordotsenko.dotsenkorssreader.entities.DBHandler;
import com.igordotsenko.dotsenkorssreader.entities.Item;
import com.igordotsenko.dotsenkorssreader.entities.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.igordotsenko.dotsenkorssreader.ReaderContentProvider.ContractClass;

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
        ids = DBHandler.getChannelIds(mContext);


        //Try to update feeds
        for ( int channelId : ids ) {
            try {
                Log.i(ItemListActivity.ITEM_LIST_TAG, "try to update channel: " + channelId);
                DBHandler.updateChannel(channelId, parser, mContentResolver);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }
}
