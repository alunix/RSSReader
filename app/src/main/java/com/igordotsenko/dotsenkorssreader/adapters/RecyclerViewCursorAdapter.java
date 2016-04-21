package com.igordotsenko.dotsenkorssreader.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

public abstract class RecyclerViewCursorAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private Cursor mCursor;

    public void swapCursor(final Cursor cursor) {
        this.mCursor = cursor;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.mCursor != null ? this.mCursor.getCount() : 0;
    }

    public Cursor getItem(final int position) {
        if (this.mCursor != null && !this.mCursor.isClosed()) {
            this.mCursor.moveToPosition(position);
        }

        return this.mCursor;
    }

    public Cursor getmCursor() {
        return this.mCursor;
    }

    @Override
    public final void onBindViewHolder(final VH holder, final int position) {
        final Cursor cursor = this.getItem(position);
        if ( cursor != null ) {
            this.onBindViewHolder(holder, cursor);
        }
    }

    public abstract void onBindViewHolder(final VH holder, final Cursor cursor);
}