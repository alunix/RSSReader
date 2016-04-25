package com.igordotsenko.dotsenkorssreader;

import android.app.ProgressDialog;
import android.os.AsyncTask;
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
        implements ChannelListRVAdapter.OnItemSelectListener,
        AddChannelFragment.DownloadChannelTaskListener {


    public static final String LOG_TAG = "rss_reader_log";

    private ChannelListFragment mChannelListFragment;
    private ItemListFragment mItemListFragment;
    private long mCurrentChannelId;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ReaderSyncAdapter.initializeSyncAdapter(this);

        initializeImageLoader();

        restoreFragments();

        if ( isInSinglePaneMode() ) {
            if ( mItemListFragment != null &&  mItemListFragment.isAdded() ) {
                mItemListFragment.closeFragment();
                showItemListFragment();
                return;
            }
            showChannelListFragment();
            return;
        }

        showChannelListFragment();
        showItemListFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleProgressDialog();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeProgressDialog();
    }

    @Override
    public void onItemSelected(Channel selectedChannel) {
        if ( isInSinglePaneMode() ) {
            Log.d(LOG_TAG, "onItemSelected: isInSinglePaneMode");

            if ( mItemListFragment == null ) {
                mItemListFragment = new ItemListFragment();
            }
            mItemListFragment.setLastSelectedChannel(selectedChannel);
            openItemListFragmentLarge();
            return;
        }

        if ( mCurrentChannelId != selectedChannel.getId() ) {
            mCurrentChannelId = selectedChannel.getId();
            mItemListFragment.setLastSelectedChannel(selectedChannel);
            replaceItemListFragment();
        }
    }

    @Override
    public void  onDownloadFeedStarted() {
        Log.d(LOG_TAG, "" + getClass().getSimpleName() + ": onDownloadFeedStarted: started");
        showProgressDialog();
        Log.d(LOG_TAG, "" + getClass().getSimpleName() + ": onDownloadFeedStarted: finished");
    }

    @Override
    public void onDownloadFeedFinished() {
        Log.d(LOG_TAG, "" + getClass().getSimpleName() + ": onDownloadFeedFinished: started");
        removeProgressDialog();
        mChannelListFragment.getAddChannelFragment().dismiss();
        mChannelListFragment.setAddChannelFragment(null);
        Log.d(LOG_TAG, "" + getClass().getSimpleName() + ": onDownloadFeedFinished: finished");
    }

    private void initializeImageLoader() {
        ImageLoaderConfiguration imageLoaderConfiguration =
                new ImageLoaderConfiguration.Builder(MainActivity.this)
                        .memoryCacheSize(2 * 1024 * 1024)
                        .diskCacheSize(50 * 1024 * 1024)
                        .build();
        ImageLoader.getInstance().init(imageLoaderConfiguration);
    }

    private void restoreFragments() {
        mChannelListFragment = (ChannelListFragment) getSupportFragmentManager()
                .findFragmentByTag(ChannelListFragment.FRAGMENT_TAG);

        mItemListFragment = (ItemListFragment) getSupportFragmentManager()
                .findFragmentByTag(ItemListFragment.FRAGMENT_TAG);

        getSupportFragmentManager().findFragmentByTag(AddChannelFragment.FRAGMENT_TAG);
    }

    private void showChannelListFragment() {
        // Check if ItemListFragment was opened in single pane mode before. If yes - remove it
        if ( mItemListFragment != null && mItemListFragment.isAdded() && !isInSinglePaneMode()) {
            mItemListFragment.closeFragment();
        }

        if ( mChannelListFragment == null ) {
            mChannelListFragment = new ChannelListFragment();
            openChannelListFragment();
        }
    }

    private void showItemListFragment() {
        if ( mItemListFragment != null ) {
            if ( mItemListFragment.getLastSelectedChannel().getId() != 0 ) {
                // If channel was selected - show its items
                onItemSelected(mItemListFragment.getLastSelectedChannel());
            }
            return;
        }
        // If channel was not selected or user exit last selected channel - show non-selected channel
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

    private void showProgressDialog() {
        Log.d(LOG_TAG, "" + getClass().getSimpleName() + ": showProgressDialog: started");
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(
                AddChannelFragment.DownloadNewChannelTask.PROGRESS_DIALOG_MESSAGE);
        mProgressDialog.show();
        Log.d(LOG_TAG, "" + getClass().getSimpleName() + ": showProgressDialog: fininshed");
    }

    private void removeProgressDialog() {
        if ( mProgressDialog != null && mProgressDialog.isShowing() ) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void handleProgressDialog() {
        Log.d(LOG_TAG, "" + getClass().getSimpleName() + ": handleProgressDialog: started");

        if ( mChannelListFragment.getAddChannelFragment() != null && newChannelIsDowloading()) {
            showProgressDialog();
        }
        Log.d(LOG_TAG, "" + getClass().getSimpleName() + ": handleProgressDialog: finished");
    }

    private boolean newChannelIsDowloading() {
        AddChannelFragment addChannelFragment = mChannelListFragment.getAddChannelFragment();
        Log.d(LOG_TAG, "" + getClass().getSimpleName() + ": newChannelIsDowloading: addChannelFragment = " + addChannelFragment);
        Log.d(LOG_TAG, "" + getClass().getSimpleName() + ": newChannelIsDowloading: DownloadTask = " + addChannelFragment.getDownloadNewChannelTask());
        if ( addChannelFragment == null
                || addChannelFragment.getDownloadNewChannelTask() == null ) {
            return false;
        }

        return  mChannelListFragment.getAddChannelFragment().getDownloadNewChannelTask().getStatus()
                == AsyncTask.Status.RUNNING;
    }
}