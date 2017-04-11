package rinon.ninqueon.rssreader.xmlfeed;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by Rinon Ninqueon on 03.04.2017.
 */

public final class Feed implements Parcelable
{
    private final long id;
    private final String title;
    private final String description;
    private final String link;
    private final Date pubDate;

    private final ArrayList<FeedEntry> items;

    private final static int DEFAULT_ID     = -1;

    public Feed(final String title, final String description, final String link, final long date, ArrayList<FeedEntry> items)
    {
        this.id = DEFAULT_ID;
        this.title = title;
        this.description = description;
        this.link = link;
        this.pubDate = new Date(date);
        this.items = new ArrayList<>();
        this.items.addAll(items);       //не наследуем сам список
    }

    public Feed(final Feed feed, FeedEntry items[])
    {
        this.id = feed.getId();
        this.title = feed.getTitle();
        this.description = feed.getDescription();
        this.link = feed.getLink();
        this.pubDate = new Date(feed.getPubDate().getTime());
        this.items = new ArrayList<>();
        Collections.addAll(this.items, items);
    }

    public long getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public String getLink()
    {
        return link;
    }

    public Date getPubDate()
    {
        return pubDate;
    }

    public ArrayList<FeedEntry> getItems()
    {
        return items;
    }

    private Feed(final Parcel in)
    {
        id = in.readLong();
        title = in.readString();
        description = in.readString();
        link = in.readString();
        pubDate = new Date(in.readLong());
        items = new ArrayList<>();
    }

    public static final Creator<Feed> CREATOR = new Creator<Feed>()
    {
        @Override
        public Feed createFromParcel(final Parcel in)
        {
            return new Feed(in);
        }

        @Override
        public Feed[] newArray(final int size)
        {
            return new Feed[size];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags)
    {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(link);
        dest.writeLong(pubDate.getTime());
    }
}
