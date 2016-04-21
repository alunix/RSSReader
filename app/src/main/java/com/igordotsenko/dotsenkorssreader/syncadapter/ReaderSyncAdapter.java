package com.igordotsenko.dotsenkorssreader.syncadapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.igordotsenko.dotsenkorssreader.ItemListActivity;
import com.igordotsenko.dotsenkorssreader.entities.DBHandler;
import com.igordotsenko.dotsenkorssreader.entities.Parser;

import java.io.IOException;
import java.util.List;

import static com.igordotsenko.dotsenkorssreader.ReaderContentProvider.ContractClass;

public class ReaderSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String ACCOUNT_TYPE = "com.igordotsenko.dotsenkorssreader";
    public static final String ACCOUNT = "dummyaccount";
    private static final String AUTHORITY = ContractClass.AUTHORITY;
    private static final Uri CHANNEL_CONTENT_URI = Uri.parse(
            "content://" + AUTHORITY + "/" + ContractClass.Channel.TABLE);

    private static final Uri ITEM_CONTENT_URI = Uri.parse(
            "content://" + AUTHORITY + "/" + ContractClass.Item.TABLE);

    private static Account sAccount;

    private Context mContext;
    private ContentResolver mContentResolver;

    public ReaderSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
    }

    public static void initializeSyncAdapter(Context context) {
        Account account = initializeAccount(context);

        ContentResolver.setIsSyncable(account, ContractClass.AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, ContractClass.AUTHORITY, true);
        ContentResolver.addPeriodicSync(account, ContractClass.AUTHORITY, Bundle.EMPTY, 120);
    }

    public static void startSync() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(sAccount, ContractClass.AUTHORITY, bundle);
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

    private static Account initializeAccount(Context context) {
        if ( sAccount == null ) {
            sAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
            AccountManager accountManager =
                    (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
            accountManager.addAccountExplicitly(sAccount, null, null);
        }
        return sAccount;
    }
}
