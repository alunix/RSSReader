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
        establishConnection();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

	public Channel insertIntoChannel(Channel channel) {
        Log.i(MainActivity.LOG_TAG, "insertIntoChannel started");
        int id = getLastChannelId() + 1;
        Log.i(MainActivity.LOG_TAG, "insertIntoChannel started: new id = " + id);
        channel.setId(id);

        ContentValues cv = new ContentValues();

        cv.put(Channel.ID, id);
        cv.put(Channel.TITLE, channel.getTitle());
        cv.put(Channel.LINK, channel.getLink());
        cv.put(Channel.LAST_BUILD_DATE, channel.getLastBuildDate());
        cv.put(Channel.LAST_BUILD_DATE_LONG, channel.getLastBuildDateLong());

        Log.i(MainActivity.LOG_TAG, "insertIntoChannel started: new id =inserting: "
                        + "id = " + id
                        + "title = " + channel.getTitle()
                        + "link = " + channel.getLink()
                        + "last build date = " + channel.getLastBuildDate()
                        + "last build date long = " + channel.getLastBuildDateLong()
        );

        long isertedChannelsCount = databaseConnection.insert(Channel.TABLE, null, cv);
        Log.i(MainActivity.LOG_TAG, "insertIntoChannel started: channels inserted" + isertedChannelsCount);

        return channel;
    }
    // TODO
    public static void updateChannelBuildDate(Channel newChannel, long channelId) {
//        if ( newChannel.getLastBuildDate() != null ) {
//            new Update(Channel.class).set(Channel.LAST_BUILD_DATE + " = ?", newChannel.getLastBuildDate())
//                    .where(Channel.ID + " = " + channelId).execute();
//
//            new Update(Channel.class).set(Channel.LAST_BUILD_DATE_LONG + " = ?", newChannel.getLastBuildDateLong())
//                    .where(Channel.ID + " = " + channelId).execute();
//        }
    }

	public List<Channel> selectAllChannels() {
        Log.i(MainActivity.LOG_TAG, "selectAllChannels started");
        List<Channel> channelList = new ArrayList<>();
        String orderBy = "id ASC";

        Cursor cursor = databaseConnection.query(Channel.TABLE, null, null, null, null, null, orderBy);
        Log.i(MainActivity.LOG_TAG, "Cursor retrieved");

        if ( cursor.moveToFirst() ) {
            Log.i(MainActivity.LOG_TAG, "Cursor is not empty");
            int idIndex = cursor.getColumnIndex(Channel.ID);
            int titleIndex = cursor.getColumnIndex(Channel.TITLE);

            Log.i(MainActivity.LOG_TAG, "Start filling channel list");

            do {
                Log.i(MainActivity.LOG_TAG, "id = " + cursor.getInt(idIndex) + " title: " + cursor.getString(titleIndex));
                channelList.add(new Channel(cursor.getInt(idIndex), cursor.getString(titleIndex)));
            } while ( cursor.moveToNext() );


        }
        cursor.close();
        Log.i(MainActivity.LOG_TAG, "selectAllChannels finished");
        return channelList;
    }
    // TODO
    public static Channel selectChannelById(long id) {
//        return new Select().from(Channel.class).where(Channel.ID + " = ?", id).executeSingle();
        return null;
    }


    public boolean channelIsAlreadyAdded(String url) {
        Log.i(MainActivity.LOG_TAG, "channelIsAlreadyAdded(url): started");
        String selection = Channel.LINK + " = ?";
        String[] selectionArgs = { url };

        Cursor cursor = databaseConnection.query(Channel.TABLE, null, selection, selectionArgs, null, null, null);
        Log.i(MainActivity.LOG_TAG, "channelIsAlreadyAdded(url): cursor retrieved");

        // If records exists - cursor has more than 0 rows
        boolean recordExists = cursor.getCount() > 0;
        Log.i(MainActivity.LOG_TAG, "channelIsAlreadyAdded(url): recordExists = " + recordExists);
        cursor.close();

        return recordExists;
    }

    public boolean channelIsAlreadyAdded(Channel checkedChannel) {
        Log.i(MainActivity.LOG_TAG, "channelIsAlreadyAdded(channel): started");

        String selection = Channel.TITLE + " = ?";
        String[] selectionArgs = { checkedChannel.getTitle() };

        Cursor cursor = databaseConnection.query(Channel.TABLE, null, selection, selectionArgs, null, null, null);
        Log.i(MainActivity.LOG_TAG, "channelIsAlreadyAdded(channel): cursor retrieved");

        // If records exists - cursor has more than 0 rows
        boolean recordExists = cursor.getCount() > 0;
        Log.i(MainActivity.LOG_TAG, "channelIsAlreadyAdded(channel): recordExists = " + recordExists);
        cursor.close();

        return recordExists;
    }

	public void insertIntoItem(List<Item> items, long channelId) {
        Log.i(MainActivity.LOG_TAG, "insertIntoItem started");
        String sqlString = "INSERT INTO " + Item.TABLE
                + "("
                + Item.CHANNEL_ID + ", "
                + Item.TITLE + ", "
                + Item.LINK + ", "
                + Item.DESCRIPTION + ", "
                + Item.PUBDATE + ", "
                + Item.PUBDATE_LONG + ", "
                + Item.THUMBNAIL
                + ") VALUES( ?, ?, ?, ?, ?, ?, ?)";

        Log.i(MainActivity.LOG_TAG, "insertIntoItem started: sqlString: " + sqlString);

        SQLiteStatement sqlStatement = databaseConnection.compileStatement(sqlString);
        Log.i(MainActivity.LOG_TAG, "insertIntoItem started: sqlStatment compiled");
        databaseConnection.beginTransaction();
        Log.i(MainActivity.LOG_TAG, "insertIntoItem started: transaction began");

        for ( Item item : items ) {
            Log.i(MainActivity.LOG_TAG, "insertIntoItem started: inserting item: set id: " + channelId);
            Log.i(MainActivity.LOG_TAG, item.toString());

            sqlStatement.bindLong(1, channelId);
            sqlStatement.bindString(2, item.getTitle());
            sqlStatement.bindString(3, item.getLink());
            sqlStatement.bindString(4, item.getContent());
            sqlStatement.bindString(5, item.getPubdate());
            sqlStatement.bindLong(6, item.getPubdateLong());
            sqlStatement.bindString(7, item.getThumbNailURL());
            sqlStatement.execute();
            sqlStatement.clearBindings();
        }

        databaseConnection.setTransactionSuccessful();
        Log.i(MainActivity.LOG_TAG, "insertIntoItem started: transaction set successful");
        databaseConnection.endTransaction();
        Log.i(MainActivity.LOG_TAG, "insertIntoItem started: method finished");

    }
    // TODO
	public static List<Item> selectItemsById(long channelId) {
//        return new Select().from(Item.class).where(Item.CHANNEL_ID + " = ?", channelId).orderBy(Item.PUBDATE_LONG + " DESC").execute();
    return null;
    }
    //TODO
    public static Item selectNewestItem(long channelId) {
       // return new Select().from(Item.class).where(Item.CHANNEL_ID + " = ?", channelId).orderBy(Item.PUBDATE_LONG + " DESC").executeSingle();
        return null;
    }
    //TODO
    public int lastIdInItem() {
//        Item item = new Select().from(Item.class).orderBy(Item.ID + " DESC").executeSingle();
//        if ( item == null ) {
//            return 0;
//        }
//        return item.getID();

//        String query = "SELECT MAX(?) AS ? FROM ?";
//        String[] queryArgs = { Item.ID, Item.ID, Item.TABLE };
//        Log.i(MainActivity.LOG_TAG, "getLastItemId: query: " + query);
//        Cursor cursor = databaseConnection.rawQuery(query, queryArgs);
//
//        int idIndex = cursor.getColumnIndex(Item.ID);
//
//        cursor.moveToFirst();
//        int id = cursor.getInt(idIndex);
//        Log.i(MainActivity.LOG_TAG, "getLastChannelId: last id = " + id);
//        cursor.close();
//
//        return id;
        return 0;
    }

    private int getLastChannelId() {
        String query = "SELECT MAX(" + Channel.ID + ") AS " + Channel.ID + " FROM " + Channel.TABLE;
        Log.i(MainActivity.LOG_TAG, "getLastChannelId: query: " + query);
        Cursor cursor = databaseConnection.rawQuery(query, null);

        int idIndex = cursor.getColumnIndex(Channel.ID);

        cursor.moveToFirst();
        int id = cursor.getInt(idIndex);
        Log.i(MainActivity.LOG_TAG, "getLastChannelId: last id = " + id);
        cursor.close();

        return id;
    }

    private void establishConnection() throws IOException {
        Log.i(MainActivity.LOG_TAG, "establishConnetcion: method starts");
        try {
            databaseConnection = SQLiteDatabase.openDatabase(db_path + db_name, null, SQLiteDatabase.OPEN_READWRITE);
            Log.i(MainActivity.LOG_TAG, "establishConnetcion: connetion established at first attempt");
        } catch ( SQLiteException e ) {
            Log.i(MainActivity.LOG_TAG, "establishConnetcion: db does not exist");
            createDatabase();
            databaseConnection = SQLiteDatabase.openDatabase(db_path + db_name, null, SQLiteDatabase.OPEN_READWRITE);
            Log.i(MainActivity.LOG_TAG, "establishConnetcion: connetion established at second attempt");
        }
        if ( databaseConnection == null ) {
            Log.i(MainActivity.LOG_TAG, "establishConnetcion: no, connetcion was not established...");
            throw new SQLiteException("DB Connection was not established");

        }
    }

    private void createDatabase() throws IOException {
        Log.i(MainActivity.LOG_TAG, "createDatabase: method starts");
        // Create empty database
        getReadableDatabase();
        Log.i(MainActivity.LOG_TAG, "createDatabase: empty db creater");

        // Copy existing database from assets to empty database
        InputStream is = context.getAssets().open(db_name);
        OutputStream os = new FileOutputStream(db_path + db_name);
        Log.i(MainActivity.LOG_TAG, "createDatabase: inputs opened");

        byte[] buffer = new byte[1024];
        int bytesRead;

        Log.i(MainActivity.LOG_TAG, "createDatabase: start coping");
        try {
            for ( ; (bytesRead = is.read(buffer)) > 0; ) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Log.i(MainActivity.LOG_TAG, "createDatabase: exception during copy arised");
            throw new IOException("Error during file coping");
        } finally {
            is.close();
            os.flush();
            os.close();
        }
        Log.i(MainActivity.LOG_TAG, "createDatabase: finished");
    }
}