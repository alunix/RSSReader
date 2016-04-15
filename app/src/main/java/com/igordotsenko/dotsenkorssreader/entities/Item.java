package com.igordotsenko.dotsenkorssreader.entities;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@XStreamAlias("item")
public class Item implements Comparable<Item> {
    public static final String TABLE = "item";
    public static final String ID = "_ID";
    public static final String CHANNEL_ID = "item_channel_id";
    public static final String TITLE = "item_title";
    public static final String LINK = "item_link";
    public static final String DESCRIPTION = "item_description";
    public static final String PUBDATE = "item_pubdate";
    public static final String PUBDATE_LONG = "item_pubdate_long";
    public static final String THUMBNAIL = "item_thumbnail_url";
    public static final String SUBTITLE = "item_subtitle";

	private long id;
    private long channel;

    @XStreamAlias("title")
	private String title;

    @XStreamAlias("link")
    private String link;

    @XStreamAlias("description")
    private String description;

    @XStreamAlias("pubDate")
    private String pubdate;

    private long pubdateLong;
    private String thumbNailURL;

    public Item(String title, String link, String description, String pubdate, long pubdateLong, String thumbNailURL) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.pubdate = pubdate;
        this.pubdateLong = pubdateLong;
        this.thumbNailURL = thumbNailURL;
    }

    public void finishInitialization() {
        try {
            pubdateToLong();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //Parsing html parts for image link and item decritpion
        parseDescription();
    }

    public long getID() {
        return id;
    }

    public void setID(long id) { this.id = id; }

    public void setChannel(long channel) {
        this.channel = channel;
    }

    public String getTitle() {
    	return title;
    }

    public String getLink() {
    	return link;
    }

    public String getContent() {
    	return description;
    }

    public String getPubdate(){
    	return pubdate;
    }

    public long getPubdateLong() {
    	return pubdateLong;
    }

    public String getThumbNailURL() {
    	return thumbNailURL;
    }

    @Override
    public int compareTo(Item another) {
        if ( pubdateLong < another.pubdateLong ) {
            return 1;
        }
        if ( pubdateLong > another.pubdateLong ) {
            return -1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj == null ) return false;

        if ( !(obj instanceof Item) ) return false;

        Item another = (Item) obj;

        return id == another.id || ( channel == another.channel &&  link.equals(another.link));
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", description='" + description + '\'' +
                ", pubdate='" + pubdate + '\'' +
                ", pubdateLong=" + pubdateLong +
                ", thumbNailURL='" + thumbNailURL + '\'' +
                '}';
    }

    private void pubdateToLong() throws ParseException {
        if ( pubdate != null ) {
            DateFormat forLongFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            DateFormat forStringFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy hh:mm a", Locale.ENGLISH);
            pubdateLong = forLongFormat.parse(pubdate).getTime();
            pubdate = forStringFormat.format(new Date(pubdateLong));
        }
    }

    private void parseDescription() {
        if ( description != null ) {
            Document doc = Jsoup.parse(description);
            Element link = doc.select("img").first();
            if ( link != null) {
                thumbNailURL = link.attr("src");
            }
            description = doc.body().text();
        }
    }
}
