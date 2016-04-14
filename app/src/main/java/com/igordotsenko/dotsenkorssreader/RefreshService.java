package com.igordotsenko.dotsenkorssreader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.igordotsenko.dotsenkorssreader.entities.Channel;
import com.igordotsenko.dotsenkorssreader.entities.DBHandler;
import com.igordotsenko.dotsenkorssreader.entities.Item;
import com.igordotsenko.dotsenkorssreader.entities.Parser;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RefreshService extends Service {
    public  static final String REFRESH_TAG = "Autorefresh";
    public static final String AUTOREFRESH_RESULT_ACTION = "auto refresh action";
    public static final String AUTOREFRESH_MESSAGE_ACTION = "auto refresh message";
    public static final String AUTOREFRESH_WRAPPER = "auto refresh wrapper";
    public static final String AUTOREFRESH_MESSAGE = "auto refresh message";
    public static final String UPDATE_START_MESSAGE = "Start updating...";

    private final String UP_TO_DATE_MESSAGE = "Feed is up to date";
    private final String INTERNET_UNAVAILABLE_MESSAGE = "Internet connetction is not available";


    private LocalBroadcastManager localBroadcastManager;
    private Timer timer;
    private TimerTask timerTask;
    private long currentChannelId;

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 0, 2*60*1000);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        currentChannelId = intent.getLongExtra(Channel.ID, -1);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        stopSelf();
    }

    private void initializeTimerTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                sendMessage(UPDATE_START_MESSAGE);
                Parser parser = new Parser();
                Channel updatedChannel;

                ConnectivityManager connectivityManager
                        = (ConnectivityManager) RefreshService.this.getSystemService(Context.CONNECTIVITY_SERVICE);

                //Check if Internet connection available
                if ( connectivityManager.getActiveNetworkInfo() == null ) {
                    sendMessage(INTERNET_UNAVAILABLE_MESSAGE);
                    return;
                }

                //Try to update feed
                try {
                    updatedChannel = parser.updateExistChannel(currentChannelId);
                } catch (IOException e) {
                    sendMessage(e.getMessage());
                    return;
                }

                //If feed is up to date - null returns
                if ( updatedChannel == null) {
                    sendMessage(UP_TO_DATE_MESSAGE);
                    return;
                }

                //Update channel's last build date
                DBHandler.updateChannelBuildDate(updatedChannel, currentChannelId);

                //Start handling downloaded itemList
                List<Item> newItemList = updatedChannel.getItems();
                Item databaseNewestItem = DBHandler.selectNewestItem(currentChannelId);
                long lastPubdateLong = 0;

                //If last database item's pubdate is not specified - null returns
                if ( databaseNewestItem != null ) {
                    lastPubdateLong = databaseNewestItem.getPubdateLong();
                }

                //Filter downloaded items by publication date, setting items' id
                long lastId = MainActivity.dbHelper.lastIdInItem();
                List<Item> filteredNewItemList = new ArrayList<>();

                //Check if there are really new items in feed
                for ( Item item : newItemList ) {
                    if ( item.getPubdateLong() > lastPubdateLong ) {
                        item.setID(++lastId);
                        filteredNewItemList.add(item);
                    }
                }

                if ( filteredNewItemList.size() == 0 ) {
                    sendMessage(UP_TO_DATE_MESSAGE);
                    return;
                }

                //Sort new itemList by pubdate and insert it into database
                Collections.sort(filteredNewItemList);
                MainActivity.dbHelper.insertIntoItem(filteredNewItemList, currentChannelId);

                //Send new itemList to ItemListActivity
                ItemListWrapper wrapper = new ItemListWrapper(filteredNewItemList);
                Intent intent = new Intent(AUTOREFRESH_RESULT_ACTION + currentChannelId);
                intent.putExtra(AUTOREFRESH_WRAPPER, wrapper);
                localBroadcastManager.sendBroadcast(intent);
            }
        };
    }

    public void sendMessage(String message) {
        Intent intent = new Intent(AUTOREFRESH_MESSAGE_ACTION + currentChannelId);
        intent.putExtra(AUTOREFRESH_MESSAGE, message);
        localBroadcastManager.sendBroadcast(intent);
    }

    public class ItemListWrapper implements Serializable {
        private List<Item> items;

        public ItemListWrapper(List<Item> items) {
            this.items = new ArrayList<>(items);
        }

        public List<Item> getItems() {
            return items;
        }
    }
}
