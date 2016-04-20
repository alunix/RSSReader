package com.igordotsenko.dotsenkorssreader.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {
    private ReaderAccountAuthenticator readerAccountAuthenticator;

    public AuthenticatorService() {
        readerAccountAuthenticator = new ReaderAccountAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return readerAccountAuthenticator.getIBinder();
    }
}
