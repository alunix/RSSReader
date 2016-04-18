package com.igordotsenko.dotsenkorssreader.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.igordotsenko.dotsenkorssreader.ReaderApplication;

import java.util.Objects;

public class SyncService extends Service {
    private static final Object syncAdapterLock = new Object();
    private static ReaderSyncAdapter syncAdapter;

    public SyncService() {
        synchronized (syncAdapterLock) {
            Log.i(ReaderSyncAdapter.SA_LOG, "SyncService started");
            if ( syncAdapter == null )
                syncAdapter = new ReaderSyncAdapter(ReaderApplication.appContext, true);
            Log.i(ReaderSyncAdapter.SA_LOG, "SyncService finished");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(ReaderSyncAdapter.SA_LOG, "SyncService onBind");
        return syncAdapter.getSyncAdapterBinder();
    }
}
