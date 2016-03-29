package com.igordotsenko.dotsenkorssreader.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.igordotsenko.dotsenkorssreader.ItemListActivity;
import com.igordotsenko.dotsenkorssreader.R;
import com.igordotsenko.dotsenkorssreader.entities.Channel;

import java.util.ArrayList;
import java.util.List;

public class ChannelListRVAdapter extends RecyclerView.Adapter<ChannelListRVAdapter.ChannelViewHolder>{
    private List<Channel> channelList;
    private Context context;

    public ChannelListRVAdapter(Context context, List<Channel> channelList) {
        this.context = context;
        this.channelList = new ArrayList<>(channelList);
    }

    @Override
    public ChannelViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_channel,viewGroup, false);
        return new ChannelViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ChannelViewHolder viewHolder, final int position) {
        viewHolder.channelTitle.setText(channelList.get(position).getTitle());

        //Setting OnClickListeners
        viewHolder.layout.setOnClickListener(new ChannelOnClickListener(position));
        viewHolder.channelTitle.setOnClickListener(new ChannelOnClickListener(position));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return channelList.size();
    }

    public void setChannelList(List<Channel> channelList) {
        this.channelList = new ArrayList(channelList);
    }

    public void addChannel(Channel channel) {
        channelList.add(channel);
    }

    public static class ChannelViewHolder extends RecyclerView.ViewHolder {
        TextView channelTitle;
        RelativeLayout layout;

        ChannelViewHolder(View view) {
            super(view);
            layout = (RelativeLayout) view.findViewById(R.id.channel_card_layout);
            channelTitle = (TextView) view.findViewById(R.id.channel_title);
        }
    }

    private class ChannelOnClickListener implements View.OnClickListener {
        private final int position;

        public  ChannelOnClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            //Start ItemListActivity
            Intent intent = new Intent(context, ItemListActivity.class);
            intent.putExtra(Channel.ID, channelList.get(position).getID());
            intent.putExtra(Channel.TITLE, channelList.get(position).getTitle());
            context.startActivity(intent);
        }
    }
}
