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

import static com.igordotsenko.dotsenkorssreader.ReaderContentProvider.ContractClass;

public class ChannelListRVAdapter
        extends RecyclerViewCursorAdapter<ChannelListRVAdapter.ChannelViewHolder>{

    public interface OnItemSelectListener {
        void onItemSelected(long selectedChannelId, String title);
    }

    private static OnItemSelectListener mOnItemSelectListener;

    public ChannelListRVAdapter(OnItemSelectListener onItemSelectListener) {
        this.mOnItemSelectListener = onItemSelectListener;
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

        private long mId;
        private String mTitle;
        private TextView mChannelTitleTextView;
        private RelativeLayout mLayout;

        ChannelViewHolder(View view) {
            super(view);

            mLayout = (RelativeLayout) view.findViewById(R.id.channel_card_layout);
            mChannelTitleTextView = (TextView) view.findViewById(R.id.channel_title);
            setOnClickListeners();
        }

        public void bindData(final Cursor cursor) {
            this.mId = cursor.getLong(cursor.getColumnIndex(ContractClass.Channel.ID));
            this.mTitle = cursor.getString(cursor.getColumnIndex(ContractClass.Channel.TITLE));

            mChannelTitleTextView.setText(mTitle);
        }

        @Override
        public void onClick(View v) {
            //Start ItemListActivity
//            Intent intent = new Intent(sContext, ItemListActivity.class);
//            intent.putExtra(ContractClass.Channel.ID, mId);
//            intent.putExtra(ContractClass.Channel.TITLE, mTitle);
//            sContext.startActivity(intent);
            mOnItemSelectListener.onItemSelected(mId, mTitle);
        }

        private void setOnClickListeners() {
            mLayout.setOnClickListener(this);
            mChannelTitleTextView.setOnClickListener(this);
        }
    }
}
