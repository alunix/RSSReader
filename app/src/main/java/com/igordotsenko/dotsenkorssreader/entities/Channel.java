package com.igordotsenko.dotsenkorssreader.entities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@XStreamAlias("channel")
public class Channel {
	private long mId;

    @XStreamAlias("title")
	private String mTitle;

    @XStreamAlias("link")
    private String mLink;

    @XStreamAlias("lastBuildDate")
    private String mLastBuildDate;

    private long mLastBuildDateLong;

    @XStreamImplicit
    private List<Item> mItems;

    public Channel(String title, String link, String lastBuildDate) {
    	this.mTitle = title;
        this.mLink = link;
        this.mLastBuildDate = lastBuildDate;
        lastBuildDateToLong();
        this.mItems = new ArrayList<Item>();
    }

    public void finishItemsInitializtion() {
        for ( Item item : mItems) {
            //Converting items' pubdate to long, parsing items' description
            item.finishInitialization();
            lastBuildDateToLong();
        }
    }

    public long getId() {
    	return mId;
    }

    public String getTitle() {
    	return mTitle;
    }

    public String getLink() {
    	return mLink;
    }

    public String getLastBuildDate() {
    	return mLastBuildDate;
    }

    public long getLastBuildDateLong() {
    	return mLastBuildDateLong;
    }

    public List<Item> getItems() {
        return mItems;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public void setLink(String link) {
        this.mLink = link;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "ID=" + mId +
                ", title='" + mTitle + '\'' +
                ", link='" + mLink + '\'' +
                ", lastBuildDate='" + mLastBuildDate + '\'' +
                ", lastBuildDateLong=" + mLastBuildDateLong +
                '}';
    }

    private void lastBuildDateToLong() {
        if ( mLastBuildDate != null ) {
            DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            try {
                mLastBuildDateLong = format.parse(mLastBuildDate).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}