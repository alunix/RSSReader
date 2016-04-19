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

public class ChannelListRVAdapter extends RecyclerViewCursorAdapter<ChannelListRVAdapter.ChannelViewHolder>{
    private Context context;

    public ChannelListRVAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ChannelViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_channel,viewGroup, false);
        return new ChannelViewHolder(v);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
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
