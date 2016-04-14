package com.igordotsenko.dotsenkorssreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.igordotsenko.dotsenkorssreader.adapters.ItemListRVAdapter;
import com.igordotsenko.dotsenkorssreader.entities.Channel;
import com.igordotsenko.dotsenkorssreader.entities.DBHandler;
import com.igordotsenko.dotsenkorssreader.entities.Item;
import com.igordotsenko.dotsenkorssreader.entities.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private ImageButton backButton;
    private TextView welcomeMessage;
    private List<Item> itemList;
    private ItemListRVAdapter rvAdapter;
    private long currentChannelId;
    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        currentChannelId = getIntent().getLongExtra(Channel.ID, -1);

        //Broadcast reciever initialization
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Show toast with message from RefreshService
                if ( intent.getAction().equals(RefreshService.AUTOREFRESH_MESSAGE_ACTION + currentChannelId ) ) {
                    handleAutoRefreshMessage(intent);
                }
                //Get filtered list of new items, add them to list, update recyclerview
                if ( intent.getAction().equals(RefreshService.AUTOREFRESH_RESULT_ACTION + currentChannelId) ) {
                    handleAutoRefreshResult(intent);
                }
            }
        };

        //Intent filter for interacting with RefreshService initialization
        IntentFilter filter = new IntentFilter();
        filter.addAction(RefreshService.AUTOREFRESH_RESULT_ACTION + currentChannelId);
        filter.addAction(RefreshService.AUTOREFRESH_MESSAGE_ACTION + currentChannelId);
        localBroadcastManager.registerReceiver(broadcastReceiver, filter);

        //Start RefreshSercive
        Intent intent = new Intent(ItemListActivity.this, RefreshService.class);
        startService(intent.putExtra(Channel.ID, currentChannelId));

        //SwipeRefreshLayout initialization
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.item_list_swiperefresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Handler handler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message message) {
                        super.handleMessage(message);
                        Toast.makeText(getBaseContext(), message.getData().getString(RefreshingRunnable.MESSAGE_TAG), Toast.LENGTH_SHORT).show();
                        updateItemList();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                };

                Thread thread = new Thread(new RefreshingRunnable(handler.obtainMessage()));
                thread.start();
            }
        });

        //SearchView initialization
        searchView = (SearchView) findViewById(R.id.item_list_search_view);
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);

        //RecyclerView initialization
        recyclerView = (RecyclerView) findViewById(R.id.item_list_recyclerview);
        LinearLayoutManager llm = new LinearLayoutManager(ItemListActivity.this);
        recyclerView.setLayoutManager(llm);

        //BackButton initialization
        backButton = (ImageButton) findViewById(R.id.item_list_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Welcome Message textView initializtion
        welcomeMessage = (TextView) findViewById(R.id.item_list_empty_view);

        //Retrieving channel's itemList from database
        itemList = MainActivity.dbHelper.selectItemsById(currentChannelId);

        //If itemList empty - show welcome message
        if ( itemList == null || itemList.size() == 0 ) {
            itemList = new ArrayList<>();
            setWelcomeMessageVisible();
        }

        //Setting adapter on recyclerView
        rvAdapter = new ItemListRVAdapter(ItemListActivity.this, itemList, getIntent().getStringExtra(Channel.TITLE));
        recyclerView.setAdapter(rvAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchView.clearFocus();
        recyclerView.requestFocus();
        rvAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(ItemListActivity.this, RefreshService.class));
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String queryText) {
        //Filtration of item titles and recyclerView updating
        List<Item> filteredItemsList = new ArrayList<>();
        filterByQuery(filteredItemsList, queryText);
        updateItemList(filteredItemsList);
        return false;
    }

    public void updateItemList(List<Item> itemsList) {
        rvAdapter.setItemsList(itemsList);
        updateItemList();
    }

    public void updateItemList() {
        if ( recyclerView.getVisibility() != View.VISIBLE ) {
            setRecyclerViewVisible();
        }
        rvAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(0);
    }

    public void addItemsToList(List<Item> itemsList) {
        rvAdapter.addItems(itemsList);
    }

    private void handleAutoRefreshMessage(Intent intent) {
        String message = intent.getStringExtra(RefreshService.AUTOREFRESH_MESSAGE);
        Toast.makeText(ItemListActivity.this, message, Toast.LENGTH_SHORT).show();

        //Updating pubdate
        if ( message.equals(RefreshService.UPDATE_START_MESSAGE) ) {
            rvAdapter.notifyDataSetChanged();
        }
    }

    private void handleAutoRefreshResult(Intent intent) {
        RefreshService.ItemListWrapper wrapper =
                (RefreshService.ItemListWrapper) intent.getExtras().get(RefreshService.AUTOREFRESH_WRAPPER);
        addItemsToList(wrapper.getItems());
        updateItemList();
        Toast.makeText(ItemListActivity.this, "" + wrapper.getItems().size() + " news added",
                Toast.LENGTH_SHORT).show();
    }

    private void setWelcomeMessageVisible() {
        recyclerView.setVisibility(View.GONE);
        welcomeMessage.setVisibility(View.VISIBLE);
    }

    private void setRecyclerViewVisible() {
        recyclerView.setVisibility(View.VISIBLE);
        welcomeMessage.setVisibility(View.GONE);
    }

    private void filterByQuery(List<Item> filteredItemsList, String queryText) {
        for ( Item item : itemList) {
            if ( item.getTitle().toLowerCase().contains(queryText.toLowerCase()) ) {
                filteredItemsList.add(item);
            }
        }
    }

    private class RefreshingRunnable implements Runnable {
        private final String INTERNET_UNAVAILABLE_MESSAGE = "Internet connection is not available";
        private final String SUCCESS_MESSAGE = "Feed updated";
        private final String UP_TO_DATE_MESSAGE = "Feed is up to date";
        public static final String MESSAGE_TAG = "message";

        private Message message;

        public RefreshingRunnable(Message message) {
            this.message = message;
        }

        @Override
        public void run() {
            Parser parser = new Parser();
            Channel updatedChannel;
            long currentChannelId = ItemListActivity.this.getIntent().getLongExtra(Channel.ID, -1);

            Bundle bundle = new Bundle();
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) ItemListActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

            //Check if Internet connection available
            if ( connectivityManager.getActiveNetworkInfo() == null ) {
                bundle.putString(MESSAGE_TAG, INTERNET_UNAVAILABLE_MESSAGE);
                message.setData(bundle);
                message.sendToTarget();
                return;
            }

            //Try to update feed
            try {
                updatedChannel = parser.updateExistChannel(currentChannelId);
            } catch (IOException e) {
                bundle.putString(MESSAGE_TAG, e.getMessage());
                message.setData(bundle);
                message.sendToTarget();
                return;
            }

            //If feed is up to date - null returns
            if ( updatedChannel == null) {
                bundle.putString(MESSAGE_TAG, UP_TO_DATE_MESSAGE);
                message.setData(bundle);
                message.sendToTarget();
                return;
            }

            //Update channel's last build date
            MainActivity.dbHelper.updateChannelBuildDate(updatedChannel, currentChannelId);

            //Start handling downloaded itemList
            List<Item> newItemList = updatedChannel.getItems();
            long lastPubdateLong = MainActivity.dbHelper.getLastPubdateLongInItem(currentChannelId);

            //Filter downloaded items by publication date, setting items' id
            long lastId = MainActivity.dbHelper.lastIdInItem();
            List<Item> filteredNewItemList = new ArrayList<>();
            for ( Item item : newItemList ) {
                if ( item.getPubdateLong() > lastPubdateLong ) {
                    item.setID(++lastId);
                    filteredNewItemList.add(item);
                }
            }

            //Check if there are really new items in feed
            if ( filteredNewItemList.size() == 0 ) {
                bundle.putString(MESSAGE_TAG, UP_TO_DATE_MESSAGE);
                message.setData(bundle);
                message.sendToTarget();
                return;
            }

            //Sort new itemList by pubdate and insert it into database and recyclerview
            Collections.sort(filteredNewItemList);
            MainActivity.dbHelper.insertIntoItem(filteredNewItemList, currentChannelId);
            addItemsToList(filteredNewItemList);

            //Update UI thread
            bundle.putString(MESSAGE_TAG, SUCCESS_MESSAGE + ": " + filteredNewItemList.size() + " added");
            message.setData(bundle);
            message.sendToTarget();
        }
    }
}
