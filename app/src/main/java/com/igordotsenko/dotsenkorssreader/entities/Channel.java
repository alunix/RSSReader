package com.igordotsenko.dotsenkorssreader.entities;

import android.database.Cursor;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.igordotsenko.dotsenkorssreader.ReaderContentProvider.ContractClass;

@XStreamAlias("channel")
public class Channel {
    public static final String createChannelTable = "CREATE TABLE " + ContractClass.Channel.TABLE + "("
            + ContractClass.Channel.ID + " INTEGER PRIMARY KEY, "
            + ContractClass.Channel.TITLE + " TEXT NOT NULL, "
            + ContractClass.Channel.LINK + " TEXT NOT NULL, "
            + ContractClass.Channel.LAST_BUILD_DATE + " TEXT,"
            + ContractClass.Channel.LAST_BUILD_DATE_LONG + " INTEGER, "
            + "unique(channel_link));";

    public static final String inserIntoChannel = "INSERT INTO " + ContractClass.Channel.TABLE
            + "(" + ContractClass.Channel.ID + ", "
            + ContractClass.Channel.TITLE + ", "
            + ContractClass.Channel.LINK + ") "
            + "VALUES (1, \"BBC NEWS\", \"http://feeds.bbci.co.uk/news/rss.xml\");";

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

    // Needed for XStream normal work
    public Channel() {}

    public Channel(String title, String link, String lastBuildDate) {
    	this.mTitle = title;
        this.mLink = link;
        this.mLastBuildDate = lastBuildDate;
        lastBuildDateToLong();
        this.mItems = new ArrayList<Item>();
    }

    public Channel(Cursor cursor) {
        this.mId = cursor.getLong(cursor.getColumnIndex(ContractClass.Channel.ID));
        this.mTitle = cursor.getString(cursor.getColumnIndex(ContractClass.Channel.TITLE));
        this.mLink = cursor.getString(cursor.getColumnIndex(ContractClass.Channel.LINK));
        this.mLastBuildDate = cursor.getString(cursor.getColumnIndex(ContractClass
                .Channel.LAST_BUILD_DATE));

        this.mLastBuildDateLong = cursor.getLong(cursor.getColumnIndex(ContractClass
                .Channel.LAST_BUILD_DATE_LONG));
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