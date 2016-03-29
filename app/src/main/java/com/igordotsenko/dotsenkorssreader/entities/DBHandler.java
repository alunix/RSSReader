package com.igordotsenko.dotsenkorssreader.entities;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;

import java.util.List;

public class DBHandler {

	public static Channel insertIntoChannel(Channel channel) {
        //Need copy items before insertion to avoid Active Android internal error
        Channel newChannel = new Channel(channel);
        newChannel.save();
        return new Select().from(Channel.class).where(Channel.TITLE + " = ?", newChannel.getTitle()).executeSingle();
    }
    public static void updateChannelBuildDate(Channel newChannel, long channelId) {
        if ( newChannel.getLastBuildDate() != null ) {
            new Update(Channel.class).set(Channel.LAST_BUILD_DATE + " = ?", newChannel.getLastBuildDate())
                    .where(Channel.ID + " = " + channelId).execute();

            new Update(Channel.class).set(Channel.LAST_BUILD_DATE_LONG + " = ?", newChannel.getLastBuildDateLong())
                    .where(Channel.ID + " = " + channelId).execute();
        }
    }
	public static void updateChannelBuildDate(Channel[] channels) {
        ActiveAndroid.beginTransaction();
        try {
            for ( Channel newChannel : channels) {
                Channel previousChannel = new Select().from(Channel.class)
                        .where(Channel.LINK + " = ?", newChannel.getLink()).executeSingle();

                new Update(Channel.class).set(Channel.LAST_BUILD_DATE + " = ?", newChannel.getLastBuildDate())
                        .where(Channel.ID + " = " + previousChannel.getID()).execute();

                new Update(Channel.class).set(Channel.LAST_BUILD_DATE_LONG + " = ?", newChannel.getLastBuildDateLong())
                        .where(Channel.ID + " = " + previousChannel.getID()).execute();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }
	public static List<Channel> selectAllChannels() {
        return new Select().from(Channel.class).execute();
    }
    public static Channel selectChannelById(long id) {
        return new Select().from(Channel.class).where(Channel.ID + " = ?", id).executeSingle();
    }
    public static boolean channelIsAlreadyAdded(String url) {
        Channel channel = new Select().from(Channel.class).where(Channel.LINK + " = ?", url).executeSingle();
        return channel != null;
    }
    public static boolean channelIsAlreadyAdded(Channel checkedChannel) {
        Channel channel  = new Select().from(Channel.class)
                .where(Channel.TITLE + " = ?", checkedChannel.getTitle()).executeSingle();

        return channel != null;
    }

	public static void insertIntoItem(List<Item> items, long channelId) {
        ActiveAndroid.beginTransaction();
        try {
            for ( Item i : items ) {
                //Need copy items before insertion to avoid Active Android internal error
                Item itemCopy = new Item(i);
                itemCopy.setChannel(channelId);
                itemCopy.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

	public static List<Item> selectItemsById(long channelId) {
        return new Select().from(Item.class).where(Item.CHANNEL_ID + " = ?", channelId).orderBy(Item.PUBDATE_LONG + " DESC").execute();
    }

    public static Item selectNewestItem(long channelId) {
        return new Select().from(Item.class).where(Item.CHANNEL_ID + " = ?", channelId).orderBy(Item.PUBDATE_LONG + " DESC").executeSingle();
    }

    public static long lastIdInItem() {
        Item item = new Select().from(Item.class).orderBy(Item.ID + " DESC").executeSingle();
        if ( item == null ) {
            return 0;
        }
        return item.getID();
    }
}