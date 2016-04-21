package com.igordotsenko.dotsenkorssreader.entities;

import static com.igordotsenko.dotsenkorssreader.ReaderContentProvider.ContractClass;
import com.igordotsenko.dotsenkorssreader.ReaderContentProvider;
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
    public static final String createItemTable = "CREATE TABLE " + ContractClass.Item.TABLE + "("
            + ContractClass.Item.ID + " INTEGER PRIMARY KEY, "
            + ContractClass.Item.CHANNEL_ID + " INTEGER NOT NULL, "
            + ContractClass.Item.TITLE + " TEXT NOT NULL, "
            + ContractClass.Item.LINK + " TEXT NOT NULL, "
            + ContractClass.Item.DESCRIPTION + " TEXT NOT NULL, "
            + ContractClass.Item.PUBDATE + " TEXT, "
            + ContractClass.Item.PUBDATE_LONG + " INTEGER, "
            + ContractClass.Item.THUMBNAIL + " TEXT, "
            + "FOREIGN KEY("+ ContractClass.Item.CHANNEL_ID + ") REFERENCES "
            + ContractClass.Channel.TABLE + "(" + ContractClass.Channel.ID +"));";

	private long mId;
    private long mChannel;

    @XStreamAlias("title")
	private String mTitle;

    @XStreamAlias("link")
    private String mLink;

    @XStreamAlias("description")
    private String mDescription;

    @XStreamAlias("pubDate")
    private String mPubDate;

    private long mPubDateLong;
    private String mThumbnailUrl;

    public Item(
            String title,
            String link,
            String description,
            String pubdate,
            long pubdateLong,
            String thumbnailUrl) {

        this.mTitle = title;
        this.mLink = link;
        this.mDescription = description;
        this.mPubDate = pubdate;
        this.mPubDateLong = pubdateLong;
        this.mThumbnailUrl = thumbnailUrl;
    }

    public void finishInitialization() {
        try {
            pubdateToLong();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //Parsing html parts for image mLink and item decritpion
        parseDescription();
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) { this.mId = id; }

    public long getChannel() {
        return mChannel;
    }

    public void setChannel(long channel) {
        this.mChannel = channel;
    }

    public String getTitle() {
    	return mTitle;
    }

    public String getLink() {
    	return mLink;
    }

    public String getContent() {
    	return mDescription;
    }

    public String getPubDate(){
    	return mPubDate;
    }

    public long getPubDateLong() {
    	return mPubDateLong;
    }

    public String getThumbNailUrl() {
    	return mThumbnailUrl;
    }

    @Override
    public int compareTo(Item another) {
        if ( mPubDateLong < another.mPubDateLong ) {
            return 1;
        }
        if ( mPubDateLong > another.mPubDateLong ) {
            return -1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj == null ) return false;

        if ( !(obj instanceof Item) ) return false;

        Item another = (Item) obj;

        return mId == another.mId || ( mChannel == another.mChannel &&  mLink.equals(another.mLink));
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + mId +
                ", title='" + mTitle + '\'' +
                ", link='" + mLink + '\'' +
                ", description='" + mDescription + '\'' +
                ", pubdate='" + mPubDate + '\'' +
                ", pubdateLong=" + mPubDateLong +
                ", thumbNailURL='" + mThumbnailUrl + '\'' +
                '}';
    }

    private void pubdateToLong() throws ParseException {
        if ( mPubDate != null ) {
            DateFormat forLongFormat =
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

            DateFormat forStringFormat =
                    new SimpleDateFormat("EEEE, MMMM d, yyyy hh:mm a", Locale.ENGLISH);

            mPubDateLong = forLongFormat.parse(mPubDate).getTime();
            mPubDate = forStringFormat.format(new Date(mPubDateLong));
        }
    }

    private void parseDescription() {
        if ( mDescription != null ) {
            Document doc = Jsoup.parse(mDescription);
            Element link = doc.select("img").first();
            if ( link != null) {
                mThumbnailUrl = link.attr("src");
            }
            mDescription = doc.body().text();
        }
    }
}
