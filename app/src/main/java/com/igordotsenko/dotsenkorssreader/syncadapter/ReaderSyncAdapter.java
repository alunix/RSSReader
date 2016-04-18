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

    ContentResolver contentResolver;

    public ReaderSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        this.contentResolver = context.getContentResolver();
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(SA_LOG, "onPerformSync started");

        Cursor channelContentCuresor = null;
        try {
            channelContentCuresor = provider.query(CHANNEL_CONTENT_URI, null, null, null, null, null);
            logCursor(channelContentCuresor);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.i(SA_LOG, "onPerformSync finished");
    }

    private void logCursor(Cursor cursor) {
        Log.i(SA_LOG, "logCursor started");
        String[] columns = cursor.getColumnNames();
        int columnCount = cursor.getColumnCount();
        int[] columnIndexes = new int[columnCount];

        for ( String column : columns ) {
            Log.i(SA_LOG, String.format("%20s ", column));
        }

        if ( cursor.moveToFirst() ) {
            for ( int i = 0; i < columnCount; i++ ) {
                columnIndexes[i] = cursor.getColumnIndex(columns[i]);
            }

            do {
                for (int index : columnIndexes) {
                    Log.i(SA_LOG, String.format("%20s ", cursor.getString(index)));
                }
                Log.i(SA_LOG, "\n");
            } while ( cursor.moveToNext() );
        }
        Log.i(SA_LOG, "logCursor finished");
    }
}
