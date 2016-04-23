package com.igordotsenko.dotsenkorssreader;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.igordotsenko.dotsenkorssreader.adapters.ChannelListRVAdapter;
import com.igordotsenko.dotsenkorssreader.entities.Channel;
import com.igordotsenko.dotsenkorssreader.syncadapter.ReaderSyncAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MainActivity extends AppCompatActivity
        implements ChannelListRVAdapter.OnItemSelectListener {


    public static final String LOG_TAG = "rss_reader_log";

    private ChannelListFragment mChannelListFragment;
    private ItemListFragment mItemListFragment;
    private long mCurrentChannelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ReaderSyncAdapter.initializeSyncAdapter(this);

        initializeImageLoader();

        showChannelListFragment();

        if ( !isInSinglePaneMode() ) {
            showItemListFragment();
        }

    }

    @Override
    public void onItemSelected(Channel selectedChannel) {
        if ( isInSinglePaneMode() ) {
            Log.d(LOG_TAG, "onItemSelected: isInSinglePaneMode");

            if ( mItemListFragment == null ) {
                mItemListFragment = new ItemListFragment();
            }
            mChannelListFragment.setLastSelectedChannel(selectedChannel);
            mItemListFragment.setLastSelectedChannel(selectedChannel);
            openItemListFragmentLarge();
            return;
        }

        if ( mCurrentChannelId != selectedChannel.getId() ) {
            mCurrentChannelId = selectedChannel.getId();
            mChannelListFragment.setLastSelectedChannel(selectedChannel);
            mItemListFragment.setLastSelectedChannel(selectedChannel);
            replaceItemListFragment();
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

    private void showChannelListFragment() {
        mChannelListFragment = (ChannelListFragment) getSupportFragmentManager()
                .findFragmentByTag(ChannelListFragment.FRAGMENT_TAG);

        if ( mChannelListFragment == null ) {
            mChannelListFragment = new ChannelListFragment();
            openChannelListFragment();
        }
    }

    private void showItemListFragment() {
        mItemListFragment = (ItemListFragment) getSupportFragmentManager()
                .findFragmentByTag(ItemListFragment.FRAGMENT_TAG);

        if ( mItemListFragment != null  ) {
            onItemSelected(mChannelListFragment.getLastSelectedChannel());
            return;
        }
        mItemListFragment = new ItemListFragment();
        openItemListFragmentSmall();
    }

    private boolean isInSinglePaneMode() {
        return findViewById(R.id.itemList_fragment_container) == null;
    }

    private void openChannelListFragment() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.channelList_fragment_container,
                        mChannelListFragment,
                        ChannelListFragment.FRAGMENT_TAG)
                .commit();
    }

    private void openItemListFragmentSmall() {
        startReplaceFragmentTransaction(
                R.id.itemList_fragment_container, mItemListFragment, ItemListFragment.FRAGMENT_TAG)
                .commit();
    }

    private void openItemListFragmentLarge() {
        Log.d(LOG_TAG, "openItemListFragmentLarge: started");
        startReplaceFragmentTransaction(
                R.id.channelList_fragment_container, mItemListFragment, ItemListFragment.FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
        getSupportFragmentManager().executePendingTransactions();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Log.d(LOG_TAG, "openItemListFragmentLarge: finished");
    }

    private void replaceItemListFragment() {
        Log.d(LOG_TAG, "replaceItemListFragment: started");
        startReplaceFragmentTransaction(
                R.id.itemList_fragment_container, mItemListFragment, ItemListFragment.FRAGMENT_TAG)
                .commit();
        getSupportFragmentManager().executePendingTransactions();
        mItemListFragment.onReplace();
        Log.d(LOG_TAG, "replaceItemListFragment: finished");
    }

    private FragmentTransaction startReplaceFragmentTransaction(
            int contentViewId, Fragment fragment, String tag) {
        return getSupportFragmentManager().beginTransaction().replace(contentViewId, fragment, tag);
    }
}