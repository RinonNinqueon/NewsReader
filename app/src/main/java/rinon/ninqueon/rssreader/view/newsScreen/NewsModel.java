package rinon.ninqueon.rssreader.view.newsScreen;

import java.util.ArrayList;
import java.util.Collections;

import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;

/**
 * Created by Rinon Ninqueon on 21.03.2017.
 */

final class NewsModel
{
    private final ArrayList<FeedEntry> feedEntries;

    NewsModel()
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

    public ArrayList<FeedEntry> getFeedEntries()
    {
        return feedEntries;
    }

    final boolean isEmpty()
    {
        return feedEntries.isEmpty();
    }
}
