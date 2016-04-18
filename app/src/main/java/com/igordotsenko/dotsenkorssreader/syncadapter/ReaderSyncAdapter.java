package com.igordotsenko.dotsenkorssreader.syncadapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.igordotsenko.dotsenkorssreader.ReaderContentProvider;

public class ReaderSyncAdapter extends AbstractThreadedSyncAdapter {
    public  static final String SA_LOG = "syncAdapter_log";

    private static final String AUTHORITY = "com.igordotsenko.dotsenkorssreader";
    private static final Uri CHANNEL_CONTENT_URI = Uri.parse(
            "content://" + AUTHORITY + "/" + ReaderContentProvider.ReaderRawData.CHANNEL_TABLE);

    private static final Uri ITEM_CONTENT_URI = Uri.parse(
            "content://" + AUTHORITY + "/" + ReaderContentProvider.ReaderRawData.ITEM_TABLE);


    public ReaderSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.i(SA_LOG, "ReaderSyncAdapter created");
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(SA_LOG, "onPerformSync started");

        Cursor cursor = getContext().getContentResolver().query(CHANNEL_CONTENT_URI, null, null, null, null, null);

        if ( cursor.moveToFirst()) {
            Log.i(SA_LOG, "cursor is not empty");
            String[] columns = cursor.getColumnNames();
            int[] columnIndexes;

            for ( int i = 0; i < columns.length; i++ ) {
                Log.i(SA_LOG, columns[i]);
            }



            for ( String column : columns ) {
                Log.i(SA_LOG, "" + cursor.getString(cursor.getColumnIndex(column)));
            }
        }

        Log.i(SA_LOG, "onPerformSync finished");
    }
}
