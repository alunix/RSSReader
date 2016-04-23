package com.igordotsenko.dotsenkorssreader;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import static com.igordotsenko.dotsenkorssreader.ReaderContentProvider.ContractClass;
import com.igordotsenko.dotsenkorssreader.adapters.ItemListRVAdapter;
import com.igordotsenko.dotsenkorssreader.entities.Channel;
import com.igordotsenko.dotsenkorssreader.entities.DBHandler;
import com.igordotsenko.dotsenkorssreader.syncadapter.ReaderSyncAdapter;

public class ItemListFragment extends Fragment
        implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ITEM_LIST = 1;
    private static final int LOADER_ITEM_LIST_REFRESH = 2;
    private static final String QUERY_TEXT = "query text";
    public static final String FRAGMENT_TAG = "item_list_fragment_tag";

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;
    private ImageButton mBackButton;
    private TextView mWelcomeMessage;
    private ItemListRVAdapter mRvAdapter;
    private Channel mSelectedChannel = new Channel();
    //TODO remove
//    private long mCurrentChannelId;
//    private String mSelectedChannelTitle;


    public ItemListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(MainActivity.LOG_TAG, "" + getClass().getName() + "onCreateView: started");
        View layout = inflater.inflate(R.layout.fragement_item_list, container, false);

        //SwipeRefreshLayout initialization
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.item_list_swiperefresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ReaderSyncAdapter.startSync();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        //SearchView initialization
        mSearchView = (SearchView) layout.findViewById(R.id.item_list_search_view);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setIconifiedByDefault(false);

        //RecyclerView initialization
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.item_list_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        //BackButton initialization
        mBackButton = (ImageButton) layout.findViewById(R.id.item_list_back_button);
        if ( mBackButton != null ) {
            mBackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeFragment();
                }
            });
        }


        mWelcomeMessage = (TextView) layout.findViewById(R.id.item_list_empty_view);

        //Setting adapter on mRecyclerView
        mRvAdapter = new ItemListRVAdapter(getContext(), mSelectedChannel);
        mRecyclerView.setAdapter(mRvAdapter);


        Log.d(MainActivity.LOG_TAG, "" + getClass().getName() + "onCreateView: finished");
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(MainActivity.LOG_TAG, "" + getClass().getName() + "onActivityCreated: started");

        //Start Loader
        this.getLoaderManager().initLoader(LOADER_ITEM_LIST, null, this).forceLoad();
        Log.d(MainActivity.LOG_TAG, "" + getClass().getName() + "onActivityCreated: finished");
    }

    @Override
    public void onResume() {
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
        // TODO CHANGE BACK!
        String[] selectionArgs = { "" + mSelectedChannel.getId()};
//        String[] selectionArgs = { "" + 0};
        String order = ContractClass.Item.PUBDATE_LONG + " DESC";

        switch (id) {
            case LOADER_ITEM_LIST:
                selection = ContractClass.Item.CHANNEL_ID + " = ?";
                return new CursorLoader(getContext(), ContractClass.ITEM_CONTENT_URI,
                        null, selection, selectionArgs, order);
            case LOADER_ITEM_LIST_REFRESH:
                selection = ContractClass.Item.CHANNEL_ID + " = ? AND "
                        + ContractClass.Item.TITLE
                        + " LIKE '%" + args.getString(QUERY_TEXT) + "%'";

                return new CursorLoader(
                        getContext(), ContractClass.ITEM_CONTENT_URI,
                        null, selection, selectionArgs, order);
        }
        return null;
    }

    public void onReplace() {
        this.getLoaderManager().restartLoader(LOADER_ITEM_LIST, null, this).forceLoad();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if ( loader.getId() == LOADER_ITEM_LIST || loader.getId() == LOADER_ITEM_LIST_REFRESH ) {
            handleWelcomeMessage(data);
            this.mRvAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if ( loader.getId() == LOADER_ITEM_LIST || loader.getId() == LOADER_ITEM_LIST_REFRESH) {
            this.mRvAdapter.swapCursor(null);
        }
    }

    // TODO remove
//    public void finishInitialization(long selectedChannelId) {
//        mCurrentChannelId = selectedChannelId;
//        mSelectedChannelTitle = DBHandler.getChannelTitle(selectedChannelId, getContext().getContentResolver());
//
//    }

//    public long getSelectedChannelId() {
//        return mCurrentChannelId;
//    }

//    public void setSelectedChannelTitle() {
//        mSelectedChannelTitle = selectedChannelTitle;
//        mSelectedChannelTitle = DBHandler.getChannelTitle(selectedChannelId, getContext().getContentResolver());
//    }

    public void setSelectedChannel(Channel channel) {
        mSelectedChannel = channel;
//        mRvAdapter.setParentChannell(channel);
    }

    private void setWelcomeMessageVisible() {
        mRecyclerView.setVisibility(View.GONE);
        mWelcomeMessage.setVisibility(View.VISIBLE);
        Log.d(MainActivity.LOG_TAG, getClass().getSimpleName() + ": setWelcomeMessageVisible: finished");
    }

    private void setRecyclerViewVisible() {
        if ( mRecyclerView.getVisibility() != View.VISIBLE ) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mWelcomeMessage.setVisibility(View.GONE);
            Log.d(MainActivity.LOG_TAG, getClass().getSimpleName() + ": setRecyclerViewVisible: finished");
        }
    }

    private void handleWelcomeMessage(Cursor data) {
        if ( data.getCount() == 0 ) {

            setWelcomeMessageVisible();
            return;
        }
        setRecyclerViewVisible();
    }

    private void closeFragment() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().remove(this).commit();
        fragmentManager.popBackStackImmediate();
    }
}
