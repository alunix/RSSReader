package com.igordotsenko.dotsenkorssreader.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.igordotsenko.dotsenkorssreader.ItemContentActivity;
import com.igordotsenko.dotsenkorssreader.R;
import com.igordotsenko.dotsenkorssreader.entities.Item;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ocpsoft.pretty.time.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemListRVAdapter extends RecyclerView.Adapter<ItemListRVAdapter.ItemViewHolder> {
    private Context context;
    private List<Item> items;
    private ImageLoader imageLoader;
    private DisplayImageOptions displayImageOptions;
    private String parentChannelTitle;

    public ItemListRVAdapter(Context context, List<Item> items, String channelTitle) {
        this.context = context;
        this.items = new ArrayList<>(items);
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
    public void onBindViewHolder(ItemViewHolder viewHodler, int position) {
        //Thumbnail downloading, saving it in memory and disk cache
        String thumbnailUrl = items.get(position).getThumbNailURL();
        if ( thumbnailUrl != null) {
            imageLoader.displayImage(thumbnailUrl, viewHodler.itemThumbnail, displayImageOptions);
        }

        //Format pubdate to readable
        PrettyTime dateFormatter = new PrettyTime();
        Date pubdate = new Date(items.get(position).getPubdateLong());

        //Content setting
        viewHodler.itemTitle.setText(items.get(position).getTitle());
        viewHodler.itemPubdate.setText(dateFormatter.format(pubdate));

        //Seting OnClickListeners
        viewHodler.itemThumbnail.setOnClickListener(new ItemOnClickListener(position));
        viewHodler.itemTitle.setOnClickListener(new ItemOnClickListener(position));
        viewHodler.itemPubdate.setOnClickListener(new ItemOnClickListener(position));
        viewHodler.navigationImage.setOnClickListener(new ItemOnClickListener(position));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItemsList(List<Item> items) {
        this.items = new ArrayList(items);
    }
    //This method can be called from different threads: when refreshing by pull and autorefreshinsh by service
    public synchronized void addItems(List<Item> items) {
        for ( int i = items.size()-1; i >= 0; i-- ) {
            //Check if item has been added from another thread
            if ( !this.items.contains(items.get(i)) ) {
                this.items.add(0, items.get(i));
            }
        }
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
    }

    private class ItemOnClickListener implements View.OnClickListener {
        private final int position;

        public  ItemOnClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            //Starting ItemContentActivity
            Intent intent = new Intent(context, ItemContentActivity.class);
            intent.putExtra(Item.TITLE, parentChannelTitle);
            intent.putExtra(Item.THUMBNAIL, items.get(position).getThumbNailURL());
            intent.putExtra(Item.SUBTITLE, items.get(position).getTitle());
            intent.putExtra(Item.PUBDATE, items.get(position).getPubdate());
            intent.putExtra(Item.DESCRIPTION, items.get(position).getContent());
            context.startActivity(intent);
        }
    }
}
