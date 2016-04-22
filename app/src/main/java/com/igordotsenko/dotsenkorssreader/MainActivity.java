package com.igordotsenko.dotsenkorssreader;

import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageButton;

import com.igordotsenko.dotsenkorssreader.adapters.ChannelListRVAdapter;
import com.igordotsenko.dotsenkorssreader.syncadapter.ReaderSyncAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import static com.igordotsenko.dotsenkorssreader.ReaderContentProvider.ContractClass;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = "rss_reader_log";


    private ChannelListFragment mChannelListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ReaderSyncAdapter.initializeSyncAdapter(this);

        initializeImageLoader();

        mChannelListFragment = (ChannelListFragment) getSupportFragmentManager()
                .findFragmentByTag(ChannelListFragment.FRAGMENT_TAG);

        if (mChannelListFragment == null) {
            mChannelListFragment = new ChannelListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.channelList_fragment_container,
                            mChannelListFragment,
                            ChannelListFragment.FRAGMENT_TAG)
                    .commit();
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
}