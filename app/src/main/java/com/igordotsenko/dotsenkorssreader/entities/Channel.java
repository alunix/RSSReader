package com.igordotsenko.dotsenkorssreader.entities;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
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
    public static final String TABLE = "channel";
    public static final String ID = "id";
    public static final String TITLE = "channel_title";
    public static final String LINK = "channel_link";
    public static final String LAST_BUILD_DATE = "channel_last_build_date_long";
    public static final String LAST_BUILD_DATE_LONG = "channel_last_build_date_long";

	private long id;

    @XStreamAlias("title")
	private String title;

    @XStreamAlias("link")
    private String link;

    @XStreamAlias("lastBuildDate")
    private String lastBuildDate;

    private long lastBuildDateLong;

    @XStreamImplicit
    private List<Item> items;

    public Channel(String title, String link, String lastBuildDate) {
    	this.title = title;
        this.link = link;
        this.lastBuildDate = lastBuildDate;
        lastBuildDateToLong();
        this.items = new ArrayList<Item>();
    }

    public Channel(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public Channel(Channel channel) {
        this.title = channel.title;
        this.link = channel.link;
        this.lastBuildDate = channel.lastBuildDate;
        this.lastBuildDateLong = channel.lastBuildDateLong;
        this.items = new ArrayList<>(channel.items);
    }

    public void finishItemsInitializtion() {
        for ( Item item : items ) {
            //Converting items' pubdate to long, parsing items' description
            item.finishInitialization();
            lastBuildDateToLong();
        }
    }

    public long getID() {
    	return id;
    }

    public String getTitle() {
    	return title;
    }

    public String getLink() {
    	return link;
    }

    public String getLastBuildDate() {
    	return lastBuildDate;
    }

    public long getLastBuildDateLong() {
    	return lastBuildDateLong;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "ID=" + id +
                ", title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", lastBuildDate='" + lastBuildDate + '\'' +
                ", lastBuildDateLong=" + lastBuildDateLong +
                '}';
    }

    private void lastBuildDateToLong() {
        if ( lastBuildDate != null ) {
            DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            try {
                lastBuildDateLong = format.parse(lastBuildDate).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}