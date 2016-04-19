package com.igordotsenko.dotsenkorssreader.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.igordotsenko.dotsenkorssreader.ItemListActivity;
import com.igordotsenko.dotsenkorssreader.R;
import com.igordotsenko.dotsenkorssreader.ReaderContentProvider;
import com.igordotsenko.dotsenkorssreader.entities.Channel;
import com.ocpsoft.pretty.time.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChannelListRVAdapter extends RecyclerViewCursorAdapter<ChannelListRVAdapter.ChannelViewHolder>{
    private List<Channel> channelList;
    private Context context;

    public ChannelListRVAdapter(Context context, List<Channel> channelList) {
        this.context = context;
//        this.channelList = new ArrayList<>(channelList);
    }

    @Override
    public ChannelViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_channel,viewGroup, false);
        return new ChannelViewHolder(v);
    }

//    @Override
//    public void onBindViewHolder(final ChannelViewHolder holder, final Cursor cursor) {
//
//    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

//    @Override
//    public int getItemCount() {
//        return channelList.size();
//    }

//    @Override
//    public void onBindViewHolder(ItemListRVAdapter.ItemViewHolder holder, Cursor cursor) {
//
//    }

    public void setChannelList(List<Channel> channelList) {
        this.channelList = new ArrayList(channelList);
    }

    public void addChannel(Channel channel) {
        channelList.add(channel);
    }

    @Override
    public void onBindViewHolder(ChannelListRVAdapter.ChannelViewHolder holder, Cursor cursor) {
        holder.bindData(cursor);

        //Setting OnClickListeners
        holder.layout.setOnClickListener(new ChannelOnClickListener(cursor));
        holder.channelTitle.setOnClickListener(new ChannelOnClickListener(cursor));
    }


    public static class ChannelViewHolder extends RecyclerView.ViewHolder {
        TextView channelTitle;
        RelativeLayout layout;

        ChannelViewHolder(View view) {
            super(view);
            layout = (RelativeLayout) view.findViewById(R.id.channel_card_layout);
            channelTitle = (TextView) view.findViewById(R.id.channel_title);
        }

        public void bindData(final Cursor cursor) {
            channelTitle.setText(cursor.getString(cursor.getColumnIndex(ReaderContentProvider.ReaderRawData.CHANNEL_TITLE)));
        }
    }

    private class ChannelOnClickListener implements View.OnClickListener {
//        private final int position;
        private long id;
        private String title;

        public  ChannelOnClickListener(Cursor cursor) {
            this.id = cursor.getLong(cursor.getColumnIndex(ReaderContentProvider.ReaderRawData.CHANNEL_ID));
            this.title = cursor.getString(cursor.getColumnIndex(ReaderContentProvider.ReaderRawData.CHANNEL_TITLE));
        }

        @Override
        public void onClick(View v) {
            //Start ItemListActivity
            Intent intent = new Intent(context, ItemListActivity.class);
            intent.putExtra(Channel.ID, id);
            intent.putExtra(Channel.TITLE, title);
            context.startActivity(intent);
        }
    }
}
