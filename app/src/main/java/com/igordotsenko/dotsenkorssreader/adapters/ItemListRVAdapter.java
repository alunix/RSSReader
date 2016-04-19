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
import com.igordotsenko.dotsenkorssreader.ReaderContentProvider;
import com.igordotsenko.dotsenkorssreader.entities.Item;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ocpsoft.pretty.time.PrettyTime;

import java.util.Date;


public class ItemListRVAdapter extends RecyclerViewCursorAdapter<ItemListRVAdapter.ItemViewHolder> {
    private Context context;
    private ImageLoader imageLoader;
    private DisplayImageOptions displayImageOptions;
    private String parentChannelTitle;

    public ItemListRVAdapter(Context context, String channelTitle) {
        this.context = context;
        this.imageLoader = ImageLoader.getInstance();
        this.displayImageOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();
        this.parentChannelTitle = channelTitle;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final Cursor cursor) {
        holder.bindData(cursor);

        //Setting OnClickListeners
        holder.itemThumbnail.setOnClickListener(new ItemOnClickListener(cursor));
        holder.itemTitle.setOnClickListener(new ItemOnClickListener(cursor));
        holder.itemPubdate.setOnClickListener(new ItemOnClickListener(cursor));
        holder.navigationImage.setOnClickListener(new ItemOnClickListener(cursor));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemThumbnail;
        TextView itemTitle;
        TextView itemPubdate;
        ImageView navigationImage;

        ItemViewHolder(View view) {
            super(view);
            itemThumbnail = (ImageView) view.findViewById(R.id.item_thumbnail);
            itemTitle = (TextView) view.findViewById(R.id.item_title);
            itemPubdate = (TextView) view.findViewById(R.id.item_pubdate);
            navigationImage = (ImageView) view.findViewById(R.id.item_navigate_right_image);
        }

        public void bindData(final Cursor cursor) {
            final String thumbnailUrl = cursor.getString(cursor.getColumnIndex(ReaderContentProvider.ReaderRawData.ITEM_THUMBNAIL));

            if (thumbnailUrl != null) {
                imageLoader.displayImage(thumbnailUrl, this.itemThumbnail, displayImageOptions);
            }

            //Format pubdate to readable
            PrettyTime dateFormatter = new PrettyTime();
            Date pubdate = new Date(cursor.getString(cursor.getColumnIndex(ReaderContentProvider.ReaderRawData.ITEM_PUBDATE)));

            //Content setting
            this.itemTitle.setText(cursor.getString(cursor.getColumnIndex(ReaderContentProvider.ReaderRawData.ITEM_TITLE)));
            this.itemPubdate.setText(dateFormatter.format(pubdate));
        }
    }

    private class ItemOnClickListener implements View.OnClickListener {
        private String thumbnailURL;
        private String subtitle;
        private String pubdate;
        private String description;

        public  ItemOnClickListener(Cursor cursor) {
            this.thumbnailURL = cursor.getString(cursor.getColumnIndex(ReaderContentProvider.ReaderRawData.ITEM_THUMBNAIL));
            this.subtitle = cursor.getString(cursor.getColumnIndex(ReaderContentProvider.ReaderRawData.ITEM_TITLE));
            this.pubdate = cursor.getString(cursor.getColumnIndex(ReaderContentProvider.ReaderRawData.ITEM_PUBDATE));
            this.description = cursor.getString(cursor.getColumnIndex(ReaderContentProvider.ReaderRawData.ITEM_DESCRIPTION));
        }

        @Override
        public void onClick(View v) {
            //Starting ItemContentActivity
            Intent intent = new Intent(context, ItemContentActivity.class);
            intent.putExtra(Item.TITLE, parentChannelTitle);
            intent.putExtra(Item.THUMBNAIL, thumbnailURL);
            intent.putExtra(Item.SUBTITLE, subtitle);
            intent.putExtra(Item.PUBDATE, pubdate);
            intent.putExtra(Item.DESCRIPTION, description);
            context.startActivity(intent);
        }
    }
}
