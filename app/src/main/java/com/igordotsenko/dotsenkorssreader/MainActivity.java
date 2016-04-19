package com.igordotsenko.dotsenkorssreader;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageButton;

import com.igordotsenko.dotsenkorssreader.adapters.ChannelListRVAdapter;
import com.igordotsenko.dotsenkorssreader.entities.Channel;
import com.igordotsenko.dotsenkorssreader.entities.DBHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String QUERY_TEXT = "query text";
    private static final int LOADER_CHANNEL_LIST = 1;
    private static final int LOADER_CHANNEL_LIST_REFRESH = 2;
    public static final String LOG_TAG = "rss_reader_log";
    public static final String DB_NAME = "rss_reader.db";
    public static final int DB_VERSION = 1;
    public static final String ACCOUNT_TYPE = "dummy.com";
    public static final String ACCOUNT = "dummyaccount";


    public static DBHandler dbHelper;
    public static Account account;

    private static String AUTHORITY = ReaderContentProvider.ReaderRawData.AUTHORITY;

    private DialogFragment dialogFragment;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private ImageButton addChannelButton;
    private ChannelListRVAdapter rvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Create DBHelper and establish database connection (connection permanently kept inside of DBHandler)
        try {
            dbHelper = new DBHandler(MainActivity.this, DB_NAME, null, DB_VERSION);
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
//        channelList = dbHelper.selectAllChannels();
        rvAdapter = new ChannelListRVAdapter(this);
        recyclerView.setAdapter(rvAdapter);

        //Start Loader
        this.getLoaderManager().initLoader(LOADER_CHANNEL_LIST, null, this).forceLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchView.clearFocus();
        recyclerView.requestFocus();
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
        Bundle bundle = new Bundle();
        bundle.putString(QUERY_TEXT, queryText);
        this.getLoaderManager().restartLoader(LOADER_CHANNEL_LIST_REFRESH, bundle, this).forceLoad();
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String order = ReaderContentProvider.ReaderRawData.CHANNEL_ID + " ASC";

        switch (id) {
            case LOADER_CHANNEL_LIST:
                return new CursorLoader(this, ReaderContentProvider.ReaderRawData.CHANNEL_CONTENT_URI, null, null, null, order);
            case LOADER_CHANNEL_LIST_REFRESH:
                String selection = ReaderContentProvider.ReaderRawData.CHANNEL_TITLE
                        + " LIKE '%" + args.getString(QUERY_TEXT) + "%'";
                return new CursorLoader(this, ReaderContentProvider.ReaderRawData.CHANNEL_CONTENT_URI, null, selection, null, order);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if ( loader.getId() == LOADER_CHANNEL_LIST || loader.getId() == LOADER_CHANNEL_LIST_REFRESH ) {
                this.rvAdapter.swapCursor(data);
            }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if ( loader.getId() == LOADER_CHANNEL_LIST ) {
            this.rvAdapter.swapCursor(null);
        }
    }
}