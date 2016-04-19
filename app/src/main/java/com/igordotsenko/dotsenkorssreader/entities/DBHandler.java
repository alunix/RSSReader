package com.igordotsenko.dotsenkorssreader.entities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.IOException;

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
        databaseConnection = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createChannelTable = "CREATE TABLE " + Channel.TABLE + "("
                + Channel.ID + " INTEGER PRIMARY KEY, "
                + Channel.TITLE + " TEXT NOT NULL, "
                + Channel.LINK + " TEXT NOT NULL, "
                + Channel.LAST_BUILD_DATE + " TEXT,"
                + Channel.LAST_BUILD_DATE_LONG + " INTEGER, "
                + "unique(channel_link));";

        String createItemTable = "CREATE TABLE " + Item.TABLE + "("
                + Item.ID + " INTEGER PRIMARY KEY, "
                + Item.CHANNEL_ID + " INTEGER NOT NULL, "
                + Item.TITLE + " TEXT NOT NULL, "
                + Item.LINK + " TEXT NOT NULL, "
                + Item.DESCRIPTION + " TEXT NOT NULL, "
                + Item.PUBDATE + " TEXT, "
                + Item.PUBDATE_LONG + " INTEGER, "
                + Item.THUMBNAIL + " TEXT, "
                + "FOREIGN KEY("+ Item.CHANNEL_ID + ") REFERENCES " + Channel.TABLE + "(" + Channel.ID +"));";

        String inserIntoChannel = "INSERT INTO " + Channel.TABLE
                + "(" + Channel.ID + ", " + Channel.TITLE + ", " + Channel.LINK + ") "
                + "VALUES (1, \"BBC NEWS\", \"http://feeds.bbci.co.uk/news/rss.xml\");";

        db.execSQL(createChannelTable);
        db.execSQL(createItemTable);
        db.execSQL(inserIntoChannel);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public SQLiteDatabase getDatabaseConnection() throws IOException {
        return databaseConnection;
    }

}