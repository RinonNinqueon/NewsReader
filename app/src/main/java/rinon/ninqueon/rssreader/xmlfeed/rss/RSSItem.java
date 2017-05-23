package rinon.ninqueon.rssreader.xmlfeed.rss;

/**
 * Created by Rinon Ninqueon on 20.02.2017.
 */

public final class RSSItem
{
    private final String title;
    private final String link;
    private final String description;
    private final long pubDate;

    RSSItem(final String title, final String description, final String link, final long pubDate)
    {
        this.title = title;
        this.description = description;
        this.link = link;
        this.pubDate = pubDate;
    }

    public final String getTitle()
    {
        return title;
    }

    public final String getLink()
    {
        return link;
    }

    public final String getDescription()
    {
        return description;
    }

    public final long getPubDate()
    {
        return pubDate;
    }
}
