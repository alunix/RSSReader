package com.igordotsenko.dotsenkorssreader.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.igordotsenko.dotsenkorssreader.R;
import com.igordotsenko.dotsenkorssreader.entities.Channel;

public class ChannelListRVAdapter
        extends RecyclerViewCursorAdapter<ChannelListRVAdapter.ChannelViewHolder>{

    public interface OnItemSelectListener {
        void onItemSelected(Channel selectedChannel);
    }

    private static OnItemSelectListener sOnItemSelectListener;

    public ChannelListRVAdapter(OnItemSelectListener onItemSelectListener) {
        this.sOnItemSelectListener = onItemSelectListener;
    }

    @Override
    public ChannelViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.cardview_channel,viewGroup, false);

        return new ChannelViewHolder(v);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(ChannelListRVAdapter.ChannelViewHolder holder, Cursor cursor) {
        holder.bindData(cursor);
    }

    public static class ChannelViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private Channel mChannel;
        private TextView mChannelTitleTextView;
        private RelativeLayout mLayout;

        ChannelViewHolder(View view) {
            super(view);

            mLayout = (RelativeLayout) view.findViewById(R.id.channel_card_layout);
            mChannelTitleTextView = (TextView) view.findViewById(R.id.channel_title);
            setOnClickListeners();
        }

        public void bindData(final Cursor cursor) {
            mChannel = new Channel(cursor);
            mChannelTitleTextView.setText(mChannel.getTitle());
        }

        @Override
        public void onClick(View v) {
            //Open ItemListFragment
            sOnItemSelectListener.onItemSelected(mChannel);
        }

        private void setOnClickListeners() {
            mLayout.setOnClickListener(this);
            mChannelTitleTextView.setOnClickListener(this);
        }
    }
}
