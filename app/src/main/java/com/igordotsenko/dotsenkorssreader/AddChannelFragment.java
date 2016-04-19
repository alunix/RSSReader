package com.igordotsenko.dotsenkorssreader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.igordotsenko.dotsenkorssreader.entities.Channel;
import com.igordotsenko.dotsenkorssreader.entities.Item;
import com.igordotsenko.dotsenkorssreader.entities.Parser;

import java.io.IOException;
import java.util.List;


public class AddChannelFragment extends DialogFragment  {
    private final String NO_URL_MESSAGE = "Enter url";
    private final String FEED_EXIST_MESSAGE = "Feed has been added already";
    private final String INTERNET_UNAVAILABLE_MESSAGE = "Internet connection is not available";

    private MainActivity activity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_add_channel, null);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        final TextView addChannelTextView = (TextView) layout.findViewById(R.id.add_channel_url_edit_view);

        //Implementing addChannel TextView
        addChannelTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                return false;
            }
        });

        //Implementing "Add" button
        layout.findViewById(R.id.add_channel_add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = addChannelTextView.getText().toString();

                //Check if user entered text
                if ( url.equals("") ) {
                    Toast.makeText(getContext(), NO_URL_MESSAGE, Toast.LENGTH_SHORT).show();
                    return;
                }

                //Check if url schema is full
                if ( !url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }

                //Check if feed has been already added
                if ( channelIsAlreadyAdded(url) ) {
                    Toast.makeText(activity, FEED_EXIST_MESSAGE, Toast.LENGTH_SHORT).show();
                    addChannelTextView.setText("");
                    dismiss();
                    return;
                }

                //Check if internet connection is active
                ConnectivityManager connectivityManager
                        = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
                if ( connectivityManager.getActiveNetworkInfo() == null ) {
                    Toast.makeText(activity, INTERNET_UNAVAILABLE_MESSAGE, Toast.LENGTH_SHORT).show();
                    return;
                }

                //Add feed
                new DownloadNewChannelTask().execute(url);
                addChannelTextView.setText("");
                dismiss();
            }
        });

        //Implementing "Cancel" button
        layout.findViewById(R.id.add_channel_cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return layout;
    }

    private class DownloadNewChannelTask extends AsyncTask<String, Void, String> {
        private final String ERROR_MESSAGE = "Cannot add feed";
        private final String SUCCESS_MESSAGE = "Feed added";
        private final String FEED_EXIST_MESSAGE = "Feed has been added already";

        private ProgressDialog progressDialog;

        public DownloadNewChannelTask() {
            progressDialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Adding feed...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            Parser parser = new Parser();
            Channel newChannel;
            String channelUrl = params[0];

            if ( channelIsAlreadyAdded(channelUrl) ) {
                return FEED_EXIST_MESSAGE;
            }

            //Start downloading and parsing
            try {
                newChannel = parser.parseNewChannel(channelUrl);
            } catch ( IOException e ) {
                if ( !e.getMessage().equals("")) {
                    return e.getMessage();
                }
                return ERROR_MESSAGE;
            }
            if ( newChannel != null ) {
                //Check if channel has been already added
                if ( channelIsAlreadyAdded(newChannel) ) {
                    return FEED_EXIST_MESSAGE;
                }

                //Saving channel (returns the same channel with id set) and item in db.
                newChannel = insertIntoChannel(newChannel);
                insertIntoItem(newChannel.getItems(), newChannel.getID());

                //Update recyclerview
//                activity.addToChannelList(newChannel);
                return SUCCESS_MESSAGE;
            }

            //If some unknown error arise
            return ERROR_MESSAGE;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
//            activity.updateChannelList();
            Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
        }
    }

    private Channel insertIntoChannel(Channel channel) {
        long id = getLastChannelId() + 1;
        channel.setId(id);

        ContentValues cv = new ContentValues();

        cv.put(Channel.ID, id);
        cv.put(Channel.TITLE, channel.getTitle());
        cv.put(Channel.LINK, channel.getLink());
        cv.put(Channel.LAST_BUILD_DATE, channel.getLastBuildDate());
        cv.put(Channel.LAST_BUILD_DATE_LONG, channel.getLastBuildDateLong());

        activity.getContentResolver().insert(ReaderContentProvider.ReaderRawData.CHANNEL_CONTENT_URI, cv);

        return channel;
    }

    private boolean channelIsAlreadyAdded(String url){
        String selection = Channel.LINK + " = ?";
        String[] selectionArgs = { url };

        Cursor cursor = activity.getContentResolver().query(ReaderContentProvider.ReaderRawData.CHANNEL_CONTENT_URI, null, selection, selectionArgs, null);

        // If records exists - cursor has more than 0 rows
        boolean recordExists = cursor.getCount() > 0;
        cursor.close();

        return recordExists;
    }

    private boolean channelIsAlreadyAdded(Channel channel){
        String selection = Channel.TITLE + " = ?";
        String[] selectionArgs = { channel.getTitle() };

        Cursor cursor = activity.getContentResolver().query(ReaderContentProvider.ReaderRawData.CHANNEL_CONTENT_URI, null, selection, selectionArgs, null);

        // If records exists - cursor has more than 0 rows
        boolean recordExists = cursor.getCount() > 0;

        cursor.close();

        return recordExists;
    }

    private long getLastChannelId() {
        String[] projection = { ReaderContentProvider.ReaderRawData.CHANNEL_ID };
        String order = ReaderContentProvider.ReaderRawData.CHANNEL_ID + " DESC";
        Cursor cursor = activity.getContentResolver().query(
                ReaderContentProvider.ReaderRawData.CHANNEL_CONTENT_URI,
                projection, null, null, order);

        int idIndex = cursor.getColumnIndex(Channel.ID);

        cursor.moveToFirst();
        long id = cursor.getLong(idIndex);

        cursor.close();

        return id;
    }

    private void insertIntoItem(List<Item> items, long channelId) {
        ContentValues[] values = new ContentValues[items.size()];

        for ( int i = 0; i < items.size(); i++ ) {
            ContentValues cv = new ContentValues();

            cv.put(Item.CHANNEL_ID, channelId);
            cv.put(Item.LINK, items.get(i).getLink());
            cv.put(Item.TITLE, items.get(i).getTitle());
            cv.put(Item.DESCRIPTION, items.get(i).getContent());
            cv.put(Item.PUBDATE, items.get(i).getPubdate());
            cv.put(Item.PUBDATE_LONG, items.get(i).getPubdateLong());

            values[i] = cv;
        }

        activity.getContentResolver().bulkInsert(ReaderContentProvider.ReaderRawData.ITEM_CONTENT_URI, values);
    }
}
