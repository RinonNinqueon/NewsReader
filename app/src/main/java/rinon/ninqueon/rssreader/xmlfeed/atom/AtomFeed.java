package rinon.ninqueon.rssreader.xmlfeed.atom;

import java.util.ArrayList;

/**
 * Created by Rinon Ninqueon on 03.04.2017.
 */

public final class AtomFeed
{
    private final String title;
    private final String subtitle;
    private final String link;
    private final long updated;
    private final ArrayList<AtomItem> items;

    AtomFeed(final String title,
                    final String subtitle,
                    final String link,
                    final long updated,
                    final ArrayList<AtomItem> items)
    {
        this.title = title;
        this.subtitle = subtitle;
        this.link = link;
        this.updated = updated;

        this.items = new ArrayList<>();
        this.items.addAll(items);       //не наследуем сам список
    }

    public String getTitle()
    {
        return title;
    }

    public String getSubtitle()
    {
        return subtitle;
    }

    public String getLink()
    {
        return link;
    }

    public long getUpdated()
    {
        return updated;
    }

    public ArrayList<AtomItem> getItems()
    {
        return items;
    }
}
