package com.igordotsenko.dotsenkorssreader;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.igordotsenko.dotsenkorssreader.adapters.ChannelListRVAdapter;
import com.igordotsenko.dotsenkorssreader.entities.Channel;
import com.igordotsenko.dotsenkorssreader.entities.DBHandler;
import com.igordotsenko.dotsenkorssreader.syncadapter.ReaderSyncAdapter;
import com.igordotsenko.dotsenkorssreader.syncadapter.SyncService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import com.igordotsenko.dotsenkorssreader.entities.DBHandler;


public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public static final String LOG_TAG = "rss_reader_log";
    public static final String DB_NAME = "rss_reader.db";
    public static final int DB_VERSION = 1;

    public static DBHandler dbHelper;

    private static String AUTHORITY = ReaderContentProvider.ReaderRawData.AUTHORITY;
    public static final String ACCOUNT_TYPE = "dummy.com";
    public static final String ACCOUNT = "dummyaccount";
    public static final String DUMMY_ACCOUNT_NAME = "name";
    public static final String DUMMY_ACCOUNT_PASS = "name";

    private DialogFragment dialogFragment;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private ImageButton addChannelButton;
    private List<Channel> channelList;
    private ChannelListRVAdapter rvAdapter;
    private Account account;
    private ContentResolver contentResolver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Create DBHelper and establish database connection (connection permanently kept inside of DBHandler)
        try {
            dbHelper = new DBHandler(MainActivity.this, DB_NAME, null, DB_VERSION);
            dbHelper.getDatabaseConnection();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Error("Error during db connection establishing: " + e.getMessage());
        }

        // Creating account for SyncAdapter
        account = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) MainActivity.this.getSystemService(ACCOUNT_SERVICE);
        accountManager.addAccountExplicitly(account, null, null);

        ContentResolver.setIsSyncable(account, AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
        ContentResolver.addPeriodicSync(account, AUTHORITY, Bundle.EMPTY, 120);

        //Initialiazing image loader for thumbnails downloading
        ImageLoaderConfiguration imageLoaderConfiguration = new ImageLoaderConfiguration.Builder(MainActivity.this)
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .build();
        ImageLoader.getInstance().init(imageLoaderConfiguration);

        dialogFragment = new AddChannelFragment();

        //SearchView initialization
        searchView = (SearchView) findViewById(R.id.channel_list_search_view);
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);

        //AddChannelButton initialization
        addChannelButton = (ImageButton) findViewById(R.id.channel_list_add_button);
        addChannelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFragment.show(getSupportFragmentManager(), "add feed");
            }
        });

        //RecyclerView initialization
        recyclerView = (RecyclerView)findViewById(R.id.channel_list_recyclerview);
        LinearLayoutManager llm = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(llm);

        // Retrieve channel list from database and set adapter
        channelList = dbHelper.selectAllChannels();
        rvAdapter = new ChannelListRVAdapter(this, channelList);
        recyclerView.setAdapter(rvAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchView.clearFocus();
        recyclerView.requestFocus();

        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(account, AUTHORITY, bundle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String queryText) {
        //Filtration of channel by titles and recyclerView updating
        List<Channel> filteredChannelsList = new ArrayList<>();
        filterByQuery(filteredChannelsList, queryText);
        updateChannelList(filteredChannelsList);
        return false;
    }

    public void updateChannelList(List<Channel> channelsList) {
        rvAdapter.setChannelList(channelsList);
        updateChannelList();
    }

    public void updateChannelList(Channel channel) {
        rvAdapter.addChannel(channel);
        updateChannelList();
    }

    public void updateChannelList() {
        rvAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(0);
    }

    public void addToChannelList(Channel channel) {
        channelList.add(channel);
        rvAdapter.addChannel(channel);
    }

    private void filterByQuery(List<Channel> filteredChannelsList, String queryText) {
        for ( Channel ch : channelList ) {
            if ( ch.getTitle().toLowerCase().contains(queryText.toLowerCase()) ) {
                filteredChannelsList.add(ch);
            }
        }
    }
}

