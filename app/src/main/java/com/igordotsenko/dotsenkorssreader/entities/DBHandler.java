package com.igordotsenko.dotsenkorssreader.entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.igordotsenko.dotsenkorssreader.MainActivity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {
    private String db_path;
    private String db_name;
    private Context context;
    private SQLiteDatabase databaseConnection;

    public DBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) throws IOException {
        super(context, name, factory, version);

        this.context = context;
        this.db_path = context.getFilesDir().getPath();
        this.db_name = name;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public SQLiteDatabase getDatabaseConnection() throws IOException {
        if ( databaseConnection == null ) {
            establishConnection();
        }
        return databaseConnection;
    }

    private void establishConnection() throws IOException {
        try {
            databaseConnection = SQLiteDatabase.openDatabase(db_path + db_name, null, SQLiteDatabase.OPEN_READWRITE);
        } catch ( SQLiteException e ) {
            Log.i(MainActivity.LOG_TAG, "establishConnetcion: db does not exist");
            createDatabase();
            databaseConnection = SQLiteDatabase.openDatabase(db_path + db_name, null, SQLiteDatabase.OPEN_READWRITE);
        }
        if ( databaseConnection == null ) {
            throw new SQLiteException("DB Connection was not established");
        }
    }

    private void createDatabase() throws IOException {
        // Create empty database
        getReadableDatabase();

        // Copy existing database from assets to empty database
        InputStream is = context.getAssets().open(db_name);
        OutputStream os = new FileOutputStream(db_path + db_name);

        byte[] buffer = new byte[1024];
        int bytesRead;

        try {
            for ( ; (bytesRead = is.read(buffer)) > 0; ) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new IOException("Error during file coping");
        } finally {
            is.close();
            os.flush();
            os.close();
        }
    }
}