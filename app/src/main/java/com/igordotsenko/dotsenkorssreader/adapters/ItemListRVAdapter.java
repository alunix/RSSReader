package com.igordotsenko.dotsenkorssreader.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.igordotsenko.dotsenkorssreader.ItemContentActivity;
import com.igordotsenko.dotsenkorssreader.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ocpsoft.pretty.time.PrettyTime;

import java.util.Date;

import static com.igordotsenko.dotsenkorssreader.ReaderContentProvider.ContractClass;

public class ItemListRVAdapter extends RecyclerViewCursorAdapter<ItemListRVAdapter.ItemViewHolder> {
    private static Context sContext;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mDisplayImageOptions;
    private String mParentChannelTitle;

    public ItemListRVAdapter(Context context, String channelTitle) {
        this.sContext = context;
        this.mImageLoader = ImageLoader.getInstance();
        this.mDisplayImageOptions = new DisplayImageOptions
                .Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        this.mParentChannelTitle = channelTitle;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_item, parent, false);

        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final Cursor cursor) {
        holder.bindData(cursor);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        private String mThumbnailUrl;
        private String mItemTitle;
        private String mPubDate;
        private String mDescription;

        private ImageView mItemThumbnailImageView;
        private TextView mItemTitleTextView;
        private TextView mItemPubdateTextView;
        private ImageView mNavigationImageView;

        ItemViewHolder(View view) {
            super(view);

            mItemThumbnailImageView = (ImageView) view.findViewById(R.id.item_thumbnail);
            mItemTitleTextView = (TextView) view.findViewById(R.id.item_title);
            mItemPubdateTextView = (TextView) view.findViewById(R.id.item_pubdate);
            mNavigationImageView = (ImageView) view.findViewById(R.id.item_navigate_right_image);
            setOnClickListeners();
        }

        public void bindData(final Cursor cursor) {
            initializeFieldFromCursor(cursor);
            showThumbnail();

            // Format pubdate to readable and set up content
            mItemPubdateTextView.setText(new PrettyTime().format(new Date(mPubDate)));
            mItemTitleTextView.setText(mItemTitle);
        }

        @Override
        public void onClick(View v) {
            //Starting ItemContentActivity
            Intent intent = new Intent(sContext, ItemContentActivity.class);
            intent.putExtra(ContractClass.Item.TITLE, mParentChannelTitle);
            intent.putExtra(ContractClass.Item.THUMBNAIL, mThumbnailUrl);
            intent.putExtra(ContractClass.Item.SUBTITLE, mItemTitle);
            intent.putExtra(ContractClass.Item.PUBDATE, mPubDate);
            intent.putExtra(ContractClass.Item.DESCRIPTION, mDescription);
            sContext.startActivity(intent);
        }

        private void setOnClickListeners() {
            mItemThumbnailImageView.setOnClickListener(this);
            mItemTitleTextView.setOnClickListener(this);
            mItemPubdateTextView.setOnClickListener(this);
            mNavigationImageView.setOnClickListener(this);
        }

        private void initializeFieldFromCursor(Cursor cursor) {
            mThumbnailUrl = cursor.getString(cursor.getColumnIndex(ContractClass.Item.THUMBNAIL));
            mItemTitle = cursor.getString(cursor.getColumnIndex(ContractClass.Item.TITLE));
            mPubDate = cursor.getString(cursor.getColumnIndex(ContractClass.Item.PUBDATE));
            mDescription = cursor.getString(cursor.getColumnIndex(ContractClass.Item.DESCRIPTION));
        }

        private void showThumbnail() {
            if (mThumbnailUrl != null) {
                mImageLoader.displayImage(
                        mThumbnailUrl, this.mItemThumbnailImageView, mDisplayImageOptions);
            }
        }
    }
}
