package com.igordotsenko.dotsenkorssreader.entities;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.IOException;

@XStreamAlias("rss")
public class RSS {
    @XStreamAlias("channel")
    private Channel channel;

    public RSS() {}

    public RSS(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    public void finisihInitialization() throws IOException {
        // Converting last channel's pubdate to long, finalization on item creation
        channel.finishItemsInitializtion();
    }
}
