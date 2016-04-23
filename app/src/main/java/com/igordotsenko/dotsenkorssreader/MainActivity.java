package com.igordotsenko.dotsenkorssreader;

import android.os.Bundle;
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


        //TODO solve issue with title
//        if ( !isInSinglePaneMode() ) {
//            onItemSelected(1, "BBC NEWS");
//        }
//    }

    //TODO remove
//    @Override
//    public void onItemSelected(long selectedChannelId, String channelTitle) {
//        if ( isInSinglePaneMode() ) {
//            Log.d(LOG_TAG, "onItemSelected: isInSinglePaneMode");
//            ItemListFragment itemListFragment =
//                    createItemListFragment(selectedChannelId, channelTitle);
//            openItemListFragment(itemListFragment);
//            return;
//        }

    @Override
    public void onItemSelected(Channel selectedChannel) {
        if ( isInSinglePaneMode() ) {
            Log.d(LOG_TAG, "onItemSelected: isInSinglePaneMode");
//            ItemListFragment itemListFragment =
//                    createItemListFragment(selectedChannel);
            if ( mItemListFragment == null ) {
                mItemListFragment = new ItemListFragment();
            }
            mChannelListFragment.setLastSelectedChannel(selectedChannel);
            mItemListFragment.setSelectedChannel(selectedChannel);
            openItemListFragment();
            return;
        }

        if ( mCurrentChannelId != selectedChannel.getId() ) {
            mCurrentChannelId = selectedChannel.getId();
//            ItemListFragment itemListFragment =
//                    createItemListFragment(selectedChannel);
            mChannelListFragment.setLastSelectedChannel(selectedChannel);
            mItemListFragment.setSelectedChannel(selectedChannel);
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
            Log.d(LOG_TAG, "mChannelListFragment == null");
            mChannelListFragment = new ChannelListFragment();
            openChannelListFragment();
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.channelList_fragment_container,
//                            mChannelListFragment,
//                            ChannelListFragment.FRAGMENT_TAG)
//                    .commit();
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
        setItemListFragment();
    }

    private boolean isInSinglePaneMode() {
        return findViewById(R.id.itemList_fragment_container) == null;
    }

    // TODO try to injunct this method with next
    private void openChannelListFragment() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.channelList_fragment_container,
                        mChannelListFragment,
                        ChannelListFragment.FRAGMENT_TAG)
                .commit();
    }

    private void setItemListFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.itemList_fragment_container, mItemListFragment, ItemListFragment.FRAGMENT_TAG)
                .commit();
    }

    private void openItemListFragment() {
        Log.d(LOG_TAG, "openItemListFragment: started");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.channelList_fragment_container, mItemListFragment, ItemListFragment.FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
        Log.d(LOG_TAG, "openItemListFragment: finished");
    }

    private void replaceItemListFragment() {
        Log.d(LOG_TAG, "replaceItemListFragment: started");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.itemList_fragment_container, mItemListFragment, ItemListFragment.FRAGMENT_TAG)
                .commit();
        mItemListFragment.onReplace();
        Log.d(LOG_TAG, "replaceItemListFragment: finished");
    }

//    private ItemListFragment createItemListFragment(Channel channel) {
//        ItemListFragment itemListFragment = new ItemListFragment();
//        itemListFragment.setSelectedChannel(channel);
//        //TODO remove
////        itemListFragment.finishInitialization(selectedChannelId);
////        itemListFragment.setSelectedChannelTitle(channelTitle);
//
//        return itemListFragment;
//    }
}