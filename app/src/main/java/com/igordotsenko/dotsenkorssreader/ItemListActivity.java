package com.igordotsenko.dotsenkorssreader;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.igordotsenko.dotsenkorssreader.adapters.ItemListRVAdapter;
import com.igordotsenko.dotsenkorssreader.entities.Channel;
import com.igordotsenko.dotsenkorssreader.entities.Item;

import java.util.ArrayList;
import java.util.List;


public class ItemListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ITEM_LIST = 1;
    private static final int LOADER_ITEM_LIST_REFRESH = 2;
    private static final String QUERY_TEXT = "query text";
    public static final String ITEM_LIST_TAG = "item_list_tag";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private ImageButton backButton;
    private TextView welcomeMessage;
    private List<Item> itemList;
    private ItemListRVAdapter rvAdapter;
    private long currentChannelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        currentChannelId = getIntent().getLongExtra(Channel.ID, -1);

        //SwipeRefreshLayout initialization
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.item_list_swiperefresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ContentResolver.requestSync(MainActivity.account, ReaderContentProvider.ReaderRawData.AUTHORITY, Bundle.EMPTY);
                swipeRefreshLayout.setRefreshing(false);
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
        handleWelcomeMessage();

        //Setting adapter on recyclerView
        rvAdapter = new ItemListRVAdapter(ItemListActivity.this, getIntent().getStringExtra(Channel.TITLE));
        recyclerView.setAdapter(rvAdapter);

        //Start Loader
        this.getLoaderManager().initLoader(LOADER_ITEM_LIST, null, this).forceLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchView.clearFocus();
        recyclerView.requestFocus();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String queryText) {
        Bundle bundle = new Bundle();
        bundle.putString(QUERY_TEXT, queryText);
        this.getLoaderManager().restartLoader(LOADER_ITEM_LIST_REFRESH, bundle, this).forceLoad();
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection;
        String[] selectionArgs = { "" + currentChannelId };
        String order = ReaderContentProvider.ReaderRawData.ITEM_PUBDATE_LONG + " DESC";

        switch (id) {
            case LOADER_ITEM_LIST:
                selection = ReaderContentProvider.ReaderRawData.ITEM_CHANNEL_ID + " = ?";
                return new CursorLoader(this, ReaderContentProvider.ReaderRawData.ITEM_CONTENT_URI, null, selection, selectionArgs, order);
            case LOADER_ITEM_LIST_REFRESH:
                selection = ReaderContentProvider.ReaderRawData.ITEM_CHANNEL_ID + " = ? AND "
                        + ReaderContentProvider.ReaderRawData.ITEM_TITLE
                        + " LIKE '%" + args.getString(QUERY_TEXT) + "%'";
                return new CursorLoader(this, ReaderContentProvider.ReaderRawData.ITEM_CONTENT_URI, null, selection, selectionArgs, order);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if ( loader.getId() == LOADER_ITEM_LIST || loader.getId() == LOADER_ITEM_LIST_REFRESH ) {
            this.rvAdapter.swapCursor(data);

            if ( data.getCount() > 0 ) {
                setRecyclerViewVisible();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if ( loader.getId() == LOADER_ITEM_LIST || loader.getId() == LOADER_ITEM_LIST_REFRESH) {
            this.rvAdapter.swapCursor(null);
        }
    }

    private void setWelcomeMessageVisible() {
        recyclerView.setVisibility(View.GONE);
        welcomeMessage.setVisibility(View.VISIBLE);
    }

    private void setRecyclerViewVisible() {
        recyclerView.setVisibility(View.VISIBLE);
        welcomeMessage.setVisibility(View.GONE);
    }

    private void handleWelcomeMessage() {
        String selection = Item.CHANNEL_ID + " = ?";
        String[] selectionArgs = { "" + currentChannelId };

        Cursor cursor = getContentResolver().query(ReaderContentProvider.ReaderRawData.ITEM_CONTENT_URI, null, selection, selectionArgs, null);

        if ( cursor.getCount() == 0 ) {
            setWelcomeMessageVisible();
        }

        cursor.close();
    }
}
