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

import static com.igordotsenko.dotsenkorssreader.ReaderContentProvider.ContractClass;

public class ItemListActivity extends AppCompatActivity
        implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ITEM_LIST = 1;
    private static final int LOADER_ITEM_LIST_REFRESH = 2;
    private static final String QUERY_TEXT = "query text";
    public static final String ITEM_LIST_TAG = "item_list_tag";

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;
    private ImageButton mBackButton;
    private TextView mWelcomeMessage;
    private ItemListRVAdapter mRvAdapter;
    private long mCurrentChannelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mCurrentChannelId = getIntent().getLongExtra(ContractClass.Channel.ID, -1);

        //SwipeRefreshLayout initialization
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.item_list_swiperefresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Bundle bundle = new Bundle();
                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                ContentResolver.requestSync(
                        MainActivity.sAccount,
                        ContractClass.AUTHORITY,
                        Bundle.EMPTY);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        //SearchView initialization
        mSearchView = (SearchView) findViewById(R.id.item_list_search_view);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setIconifiedByDefault(false);

        //RecyclerView initialization
        mRecyclerView = (RecyclerView) findViewById(R.id.item_list_recyclerview);
        LinearLayoutManager llm = new LinearLayoutManager(ItemListActivity.this);
        mRecyclerView.setLayoutManager(llm);

        //BackButton initialization
        mBackButton = (ImageButton) findViewById(R.id.item_list_back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Welcome Message textView initializtion
        mWelcomeMessage = (TextView) findViewById(R.id.item_list_empty_view);
        handleWelcomeMessage();

        //Setting adapter on mRecyclerView
        mRvAdapter = new ItemListRVAdapter(
                ItemListActivity.this, getIntent().getStringExtra(ContractClass.Channel.TITLE));
        mRecyclerView.setAdapter(mRvAdapter);

        //Start Loader
        this.getLoaderManager().initLoader(LOADER_ITEM_LIST, null, this).forceLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSearchView.clearFocus();
        mRecyclerView.requestFocus();
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
        String[] selectionArgs = { "" + mCurrentChannelId};
        String order = ContractClass.Item.PUBDATE_LONG + " DESC";

        switch (id) {
            case LOADER_ITEM_LIST:
                selection = ContractClass.Item.CHANNEL_ID + " = ?";

                return new CursorLoader(
                        this, ContractClass.ITEM_CONTENT_URI,
                        null, selection, selectionArgs, order);
            case LOADER_ITEM_LIST_REFRESH:
                selection = ContractClass.Item.CHANNEL_ID + " = ? AND "
                        + ContractClass.Item.TITLE
                        + " LIKE '%" + args.getString(QUERY_TEXT) + "%'";

                return new CursorLoader(
                        this, ContractClass.ITEM_CONTENT_URI,
                        null, selection, selectionArgs, order);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if ( loader.getId() == LOADER_ITEM_LIST || loader.getId() == LOADER_ITEM_LIST_REFRESH ) {
            if ( data.getCount() > 0 ) {
                setRecyclerViewVisible();
            }
            this.mRvAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if ( loader.getId() == LOADER_ITEM_LIST || loader.getId() == LOADER_ITEM_LIST_REFRESH) {
            this.mRvAdapter.swapCursor(null);
        }
    }

    private void setWelcomeMessageVisible() {
        mRecyclerView.setVisibility(View.GONE);
        mWelcomeMessage.setVisibility(View.VISIBLE);
    }

    private void setRecyclerViewVisible() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mWelcomeMessage.setVisibility(View.GONE);
    }

    private void handleWelcomeMessage() {
        String selection = ContractClass.Item.CHANNEL_ID + " = ?";
        String[] selectionArgs = { "" + mCurrentChannelId};

        Cursor cursor = getContentResolver().query(
                ContractClass.ITEM_CONTENT_URI,
                null, selection, selectionArgs, null);

        if ( cursor.getCount() == 0 ) {
            setWelcomeMessageVisible();
        } else {
            setRecyclerViewVisible();
        }

        cursor.close();
    }
}
