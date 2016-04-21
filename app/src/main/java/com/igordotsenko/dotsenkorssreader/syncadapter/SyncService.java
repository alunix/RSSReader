package com.igordotsenko.dotsenkorssreader.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.igordotsenko.dotsenkorssreader.ReaderApplication;

public class SyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static ReaderSyncAdapter sSyncAdapter;

    public SyncService() {
        synchronized (sSyncAdapterLock) {
            if ( sSyncAdapter == null )
                sSyncAdapter = new ReaderSyncAdapter(ReaderApplication.sAppContext, true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
