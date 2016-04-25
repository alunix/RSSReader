package com.igordotsenko.dotsenkorssreader;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.igordotsenko.dotsenkorssreader.adapters.ChannelListRVAdapter;

public class ChannelListFragment extends Fragment
        implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final String FRAGMENT_TAG = "channel_list_fragment_tag";

    private static final String QUERY_TEXT = "query text";
    private static final int LOADER_CHANNEL_LIST = 1;
    private static final int LOADER_CHANNEL_LIST_REFRESH = 2;

    private AddChannelFragment mAddChannelFragment;
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;
    private ImageButton mAddChannelButton;
    private ChannelListRVAdapter mRvAdapter;

    public ChannelListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_channel_list, container, false);

        //SearchView initialization
        mSearchView = (SearchView) layout.findViewById(R.id.channel_list_search_view);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setIconifiedByDefault(false);

        //AddChannelButton initialization
        mAddChannelButton = (ImageButton) layout.findViewById(R.id.channel_list_add_button);
        mAddChannelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddChannelFragment = new AddChannelFragment();
                mAddChannelFragment.show(getFragmentManager(), AddChannelFragment.FRAGMENT_TAG);
            }
        });

        //RecyclerView initialization
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.channel_list_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        // Retrieve channel list from database and set adapter
        mRvAdapter =
                new ChannelListRVAdapter((ChannelListRVAdapter.OnItemSelectListener) getActivity());
        mRecyclerView.setAdapter(mRvAdapter);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Start Loader
        if (getLoaderManager().getLoader(LOADER_CHANNEL_LIST) == null)
            getLoaderManager().initLoader(LOADER_CHANNEL_LIST, null, this).forceLoad();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSearchView.clearFocus();
        mRecyclerView.requestFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mAddChannelFragment = null;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String queryText) {
        Bundle bundle = new Bundle();
        bundle.putString(QUERY_TEXT, queryText);
        this.getLoaderManager()
                .restartLoader(LOADER_CHANNEL_LIST_REFRESH, bundle, this).forceLoad();

        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String order = ReaderContentProvider.ContractClass.Channel.ID + " ASC";

        switch (id) {
            case LOADER_CHANNEL_LIST:
                return new CursorLoader(
                        getContext(), ReaderContentProvider.ContractClass.CHANNEL_CONTENT_URI,
                        null, null, null, order);

            case LOADER_CHANNEL_LIST_REFRESH:
                String selection = ReaderContentProvider.ContractClass.Channel.TITLE
                        + " LIKE '%" + args.getString(QUERY_TEXT) + "%'";

                return new CursorLoader(
                        getContext(), ReaderContentProvider.ContractClass.CHANNEL_CONTENT_URI,
                        null, selection, null, order);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if ( loader.getId() == LOADER_CHANNEL_LIST
                || loader.getId() == LOADER_CHANNEL_LIST_REFRESH ) {
            this.mRvAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if ( loader.getId() == LOADER_CHANNEL_LIST ) {
            this.mRvAdapter.swapCursor(null);
        }
    }

    public AddChannelFragment getAddChannelFragment() {
        return mAddChannelFragment;
    }

    public void setAddChannelFragment(AddChannelFragment addChannelFragment) {
        this.mAddChannelFragment = addChannelFragment;
    }
}
