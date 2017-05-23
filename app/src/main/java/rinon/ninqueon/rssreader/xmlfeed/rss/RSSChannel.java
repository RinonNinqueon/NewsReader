package rinon.ninqueon.rssreader.xmlfeed.rss;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rinon Ninqueon on 20.02.2017.
 */

public final class RSSChannel
{
    private final String title;
    private final String description;
    private final String link;
    private final long pubDate;
    private final ArrayList<RSSItem> items;

    RSSChannel(final String title, final String description, final String link, final long pubDate, final ArrayList<RSSItem> items)
    {
        this.title = title;
        this.description = description;
        this.link = link;
        this.pubDate = pubDate;

        this.items = new ArrayList<>();
        this.items.addAll(items);       //не наследуем сам список
    }

    public final String getTitle()
    {
        return title;
    }

    public final String getDescription()
    {
        return description;
    }

    public final String getLink()
    {
        return link;
    }

    public final long getPubDate()
    {
        return pubDate;
    }

    public final List<RSSItem> getItems()
    {
        return this.items;
    }
}
