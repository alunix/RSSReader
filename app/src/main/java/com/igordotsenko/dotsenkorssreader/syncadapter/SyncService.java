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
            if ( syncAdapter == null )
                syncAdapter = new ReaderSyncAdapter(ReaderApplication.appContext, true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
