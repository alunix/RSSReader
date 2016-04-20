package com.igordotsenko.dotsenkorssreader.entities;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.IOException;

@XStreamAlias("rss")
public class RSS {
    @XStreamAlias("channel")
    private Channel mChannel;

    public RSS() {}

    public RSS(Channel channel) {
        this.mChannel = channel;
    }

    public Channel getChannel() {
        return mChannel;
    }

    public void finishInitialization() throws IOException {
        // Converting last channel's pubdate to long, finalization on item creation
        mChannel.finishItemsInitializtion();
    }
}
