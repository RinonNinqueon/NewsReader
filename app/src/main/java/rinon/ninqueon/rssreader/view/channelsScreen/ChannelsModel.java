package rinon.ninqueon.rssreader.view.channelsScreen;

import java.util.ArrayList;
import java.util.Collections;

import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;

/**
 * Created by Rinon Ninqueon on 23.03.2017.
 */

final class ChannelsModel
{
    private final ArrayList<FeedEntry> feedEntries;

    ChannelsModel()
    {
        feedEntries = new ArrayList<>();
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

    private int getListSize()
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

    final boolean isEmpty()
    {
        return feedEntries.isEmpty();
    }

    public ArrayList<FeedEntry> getFeedEntries()
    {
        return feedEntries;
    }
}
