package com.igordotsenko.dotsenkorssreader.entities;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Parser {
	public Parser() {}

	public Channel parseNewChannel(String url) throws IOException {
        Channel channel = parseXML(downloadXML(url));
        channel.setLink(url);
        return channel;
    }

    public Channel updateExistChannel(long channelId) throws IOException {
        Channel currentChannel = DBHandler.selectChannelById(channelId);
        Channel newChannel = parseXML(downloadXML(currentChannel.getLink()));
        long currentChannelBuildDate = currentChannel.getLastBuildDateLong();
        long newChannelBuildDate = newChannel.getLastBuildDateLong();

        // Channel should be updated if its last build date is not specified
        // or if just downloaded feed version is newer then current
        if ( currentChannelBuildDate == 0 || currentChannelBuildDate < newChannelBuildDate ) {
            return newChannel;
        }
        return null;
    }

	private String downloadXML(String channelURL) throws IOException {
        URL url = new URL(channelURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        int responseCode = connection.getResponseCode();

        if ( responseCode != HttpURLConnection.HTTP_OK ) {
            throw new IOException("Unable connect to server");
        }

        byte[] buffer = new byte[1024];
        int bytesRead;

        InputStream is = connection.getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        for ( ; ( bytesRead = is.read(buffer) ) != -1; ) {
            os.write(buffer, 0, bytesRead);
        }

        return new String(os.toByteArray(), "UTF-8");
    }

	private Channel parseXML(String xml) throws IOException {
        XStream xs = new XStream(new DomDriver());
        Class[] classesToProcessAnnotations = { RSS.class, Channel.class, Item.class };
        RSS rss;

        //XStream configuration setting
        xs.processAnnotations(classesToProcessAnnotations);
        xs.ignoreUnknownElements();

        try {
           rss = (RSS) xs.fromXML(xml);
        } catch ( com.thoughtworks.xstream.io.StreamException e ) {
            throw new IOException("Invalid feed url");
        }

        // Converting last channel's pubdate to long, finalization on item creation
        rss.finisihInitialization();

        return rss.getChannel();
    }
}