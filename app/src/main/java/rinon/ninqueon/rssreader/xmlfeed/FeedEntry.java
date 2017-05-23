package rinon.ninqueon.rssreader.xmlfeed;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rinon.ninqueon.rssreader.utils.ErrorCodes;

/**
 * Created by Rinon Ninqueon on 23.02.2017.
 */

public final class FeedEntry implements Parcelable
{
    private final long id;
    private final long channelId;
    private final String title;
    private final String description;
    private final String link;
    private final Date pubDate;

    private final String channelLink;
    private final String channelTitle;

    private final int error;

    public final static int DEFAULT_ID     = -1;

    /**
     * Конструктор для общего парсера
     **/
    public FeedEntry(final String title, final String description, final String link, final long date)
    {
        this(DEFAULT_ID, title, description, link, date, DEFAULT_ID, null, null, ErrorCodes.ERROR_NO_ERROR);
    }

    /**
     * Конструктор для парсера новостей
     **/
    public FeedEntry(final String title, final String description, final String link, final long date,
                     final long channelId)
    {
        this(DEFAULT_ID, title, description, link, date, channelId, null, null, ErrorCodes.ERROR_NO_ERROR);
    }

    /**
     * Конструктор для чтения из БД
     **/
    public FeedEntry(final long id, final String title, final String description, final String link, final long date,
                     final long channelId, final String channelTitle, final String channelLink, final int error)
    {
        this.id = id;
        this.title = title;
        this.description = description;
        this.link = link;
        this.pubDate = new Date(date);
        this.channelId = channelId;
        this.channelTitle = channelTitle;
        this.channelLink = channelLink;
        this.error = error;
    }

    /**
     * Конструктор для канала
     **/
    public FeedEntry(final long id, final String title, final String description, final String link, final long date,
                     final String channelLink, final int error)
    {
        this(id, title, description, link, date, id, null, channelLink, error);
    }

    private FeedEntry(final Parcel in)
    {
        id = in.readLong();
        channelId = in.readLong();
        title = in.readString();
        description = in.readString();
        link = in.readString();
        channelLink = in.readString();
        channelTitle = in.readString();
        pubDate = new Date(in.readLong());
        error = in.readInt();
    }

    public static final Creator<FeedEntry> CREATOR = new Creator<FeedEntry>()
    {
        @Override
        public FeedEntry createFromParcel(final Parcel in)
        {
            return new FeedEntry(in);
        }

        @Override
        public FeedEntry[] newArray(final int size)
        {
            return new FeedEntry[size];
        }
    };

    public final long getChannelId()
    {
        return channelId;
    }

    public final long getId()
    {
        return id;
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

    public final Date getPubDate()
    {
        return pubDate;
    }

    public final String getChannelLink()
    {
        return channelLink;
    }

    public final String getSource()
    {
        return channelLink;
    }

    public final String getChannelTitle()
    {
        return channelTitle;
    }

    public int getError()
    {
        return error;
    }

    private String formatDataRFC833(final Date dateString)
    {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss z", Locale.US);

        return dateFormat.format(dateString);
    }

    public final String getStringDate()
    {
        return formatDataRFC833(this.getPubDate());
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags)
    {
        dest.writeLong(id);
        dest.writeLong(channelId);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(link);
        dest.writeString(channelLink);
        dest.writeString(channelTitle);
        dest.writeLong(pubDate.getTime());
        dest.writeInt(error);
    }
}
