package com.igordotsenko.dotsenkorssreader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.igordotsenko.dotsenkorssreader.adapters.ChannelListRVAdapter;
import com.igordotsenko.dotsenkorssreader.syncadapter.ReaderSyncAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MainActivity extends AppCompatActivity
        implements ChannelListRVAdapter.OnItemSelectListener {


    public static final String LOG_TAG = "rss_reader_log";

    private ChannelListFragment mChannelListFragment;
    private long mCurrentChannelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ReaderSyncAdapter.initializeSyncAdapter(this);

        initializeImageLoader();

        mChannelListFragment = (ChannelListFragment) getSupportFragmentManager()
                .findFragmentByTag(ChannelListFragment.FRAGMENT_TAG);

        if ( mChannelListFragment == null ) {
            Log.d(LOG_TAG, "mChannelListFragment == null");
            mChannelListFragment = new ChannelListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.channelList_fragment_container,
                            mChannelListFragment,
                            ChannelListFragment.FRAGMENT_TAG)
                    .commit();
        }


        //TODO solve issue with title
        if ( !isInSinglePaneMode() ) {
            onItemSelected(1, "BBC NEWS");
        }
    }

    @Override
    public void onItemSelected(long selectedChannelId, String channelTitle) {
        if ( isInSinglePaneMode() ) {
            Log.d(LOG_TAG, "onItemSelected: isInSinglePaneMode");
            ItemListFragment itemListFragment =
                    createItemListFragment(selectedChannelId, channelTitle);
            openItemListFragment(itemListFragment);
            return;
        }

        if ( mCurrentChannelId != selectedChannelId ) {
            mCurrentChannelId = selectedChannelId;
            ItemListFragment itemListFragment =
                    createItemListFragment(selectedChannelId, channelTitle);
            replaceItemListFragment(itemListFragment);
        }
    }

    private void initializeImageLoader() {
        ImageLoaderConfiguration imageLoaderConfiguration =
                new ImageLoaderConfiguration.Builder(MainActivity.this)
                        .memoryCacheSize(2 * 1024 * 1024)
                        .diskCacheSize(50 * 1024 * 1024)
                        .build();
        ImageLoader.getInstance().init(imageLoaderConfiguration);
    }

    private boolean isInSinglePaneMode() {
        return findViewById(R.id.itemList_fragment_container) == null;
    }

    //TODO check for duplicate with newxt method
    private void openItemListFragment(ItemListFragment itemListFragment) {
        Log.d(LOG_TAG, "openItemListFragment: started");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.channelList_fragment_container, itemListFragment, ItemListFragment.FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
        Log.d(LOG_TAG, "openItemListFragment: finished");
    }

    private void replaceItemListFragment(ItemListFragment itemListFragment) {
        Log.d(LOG_TAG, "replaceItemListFragment: started");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.itemList_fragment_container, itemListFragment, ItemListFragment.FRAGMENT_TAG)
                .commit();
        Log.d(LOG_TAG, "replaceItemListFragment: finished");
    }

    private ItemListFragment createItemListFragment(long selectedChannelId, String channelTitle) {
        ItemListFragment itemListFragment = new ItemListFragment();
        itemListFragment.setSelectedChannelId(selectedChannelId);
        itemListFragment.setSelectedChannelTitle(channelTitle);

        return itemListFragment;
    }
}