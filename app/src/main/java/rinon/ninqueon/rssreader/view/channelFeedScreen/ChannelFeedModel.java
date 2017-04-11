package rinon.ninqueon.rssreader.view.channelFeedScreen;

import java.util.ArrayList;
import java.util.Collections;

import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;

/**
 * Created by Rinon Ninqueon on 23.03.2017.
 */

final class ChannelFeedModel
{
    private final ArrayList<FeedEntry> feedEntries;
    private final long channelId;
    private final String title;

    ChannelFeedModel(final long id, final String title)
    {
        feedEntries = new ArrayList<>();
        channelId = id;
        this.title = title;
    }

    final void addItems(final FeedEntry feedEntries[])
    {
        if (feedEntries == null)
        {
            return;
        }

        Collections.addAll(this.feedEntries, feedEntries);
    }

    final void clearItems()
    {
        feedEntries.clear();
    }

    final int getListSize()
    {
        return feedEntries.size();
    }

    final FeedEntry getItem(final int position)
    {
        if (position < 0 || position >= getListSize())
        {
            return null;
        }

        return feedEntries.get(position);
    }

    final long getChannelId()
    {
        return channelId;
    }

    final ArrayList<FeedEntry> getFeedEntries()
    {
        return feedEntries;
    }

    final String getTitle()
    {
        return title;
    }

    final boolean isEmpty()
    {
        return feedEntries.isEmpty();
    }
}
