package com.igordotsenko.dotsenkorssreader;

import android.app.Dialog;
import android.content.Context;
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
import com.igordotsenko.dotsenkorssreader.entities.DBHandler;
import com.igordotsenko.dotsenkorssreader.entities.Parser;

import java.io.IOException;

public class AddChannelFragment extends DialogFragment  {

    public interface DownloadChannelTaskListener {
        void onDownloadFeedStarted();
        void onDownloadFeedFinished();
    }

    public static final String FRAGMENT_TAG = "add_channel_fragment_tag";
    public static final String PROGRESS_DIALOG_MESSAGE =
            ReaderApplication.sAppContext.getResources().getString(R.string.adding_feed_message);

    private final String NO_URL_MESSAGE = "Enter url";
    private final String FEED_EXIST_MESSAGE = "Feed has been added already";
    private final String INTERNET_UNAVAILABLE_MESSAGE = "Internet connection is not available";

    private DownloadChannelTaskListener mDownloadChannelTaskListener;
    private DownloadNewChannelTask mDownloadNewChannelTask;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mDownloadChannelTaskListener = (DownloadChannelTaskListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();

        // Work around bug: http://code.google.com/p/android/issues/detail?id=17423
        if ((dialog != null) && getRetainInstance())
            dialog.setDismissMessage(null);

        super.onDestroyView();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(MainActivity.LOG_TAG, "" + getClass().getSimpleName() + " onCreateView: started");
        View layout = inflater.inflate(R.layout.fragment_add_channel, null);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        final TextView addChannelTextView =
                (TextView) layout.findViewById(R.id.add_channel_url_edit_view);

        //Implementing addChannel TextView
        addChannelTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager cm =
                        (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
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
                if ( DBHandler.channelIsAlreadyAdded(url, getContext()) ) {
                    Toast.makeText(getContext(), FEED_EXIST_MESSAGE, Toast.LENGTH_SHORT).show();
                    addChannelTextView.setText("");
                    return;
                }

                //Check if internet connection is active
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getContext()
                                .getSystemService(Context.CONNECTIVITY_SERVICE);

                if ( connectivityManager.getActiveNetworkInfo() == null ) {
                    Toast.makeText(
                            getContext(), INTERNET_UNAVAILABLE_MESSAGE, Toast.LENGTH_SHORT).show();

                    return;
                }

                //Add feed
                mDownloadNewChannelTask = new DownloadNewChannelTask();
                mDownloadNewChannelTask.execute(url);
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

    @Override
    public void onDetach() {
        super.onDetach();
        mDownloadChannelTaskListener = null;
    }

    public DownloadNewChannelTask getDownloadNewChannelTask() {
        return mDownloadNewChannelTask;
    }

    class DownloadNewChannelTask extends AsyncTask<String, Void, String> {
        private final String ERROR_MESSAGE = "Cannot add feed";
        private final String SUCCESS_MESSAGE = "Feed added";
        private final String FEED_EXIST_MESSAGE = "Feed has been added already";

        private Context context;

        public DownloadNewChannelTask() {
            context = getActivity();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if ( mDownloadChannelTaskListener != null ) {
                mDownloadChannelTaskListener.onDownloadFeedStarted();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            Parser parser = new Parser();
            Channel newChannel;
            String channelUrl = params[0];

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
                if ( DBHandler.channelIsAlreadyAdded(newChannel, context) ) {
                    return FEED_EXIST_MESSAGE;
                }

                //Saving channel (returns the same channel with id set) and item in db.
                newChannel = DBHandler.insertIntoChannel(newChannel, context.getContentResolver());
                DBHandler.insertIntoItem(newChannel.getItems(), newChannel.getId(), context);

                //Update recyclerview
                return SUCCESS_MESSAGE;
            }

            //If some unknown error arise
            return ERROR_MESSAGE;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if ( mDownloadChannelTaskListener != null ) {
                mDownloadChannelTaskListener.onDownloadFeedFinished();
            }
            context = null;
        }
    }
}
