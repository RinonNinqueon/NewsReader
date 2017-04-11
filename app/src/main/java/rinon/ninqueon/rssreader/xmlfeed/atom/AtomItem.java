package rinon.ninqueon.rssreader.xmlfeed.atom;

/**
 * Created by Rinon Ninqueon on 03.04.2017.
 */

public final class AtomItem
{
    private final String title;
    private final String summary;
    private final String content;
    private final String link;
    private final long updated;

    AtomItem(final String title,
             final String summary,
             final String content,
             final String link,
             final long updated)
    {
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.link = link;
        this.updated = updated;
    }

    public String getTitle()
    {
        return title;
    }

    public String getSummary()
    {
        return summary;
    }

    public String getContent()
    {
        return content;
    }

    public String getLink()
    {
        return link;
    }

    public long getUpdated()
    {
        return updated;
    }
}
