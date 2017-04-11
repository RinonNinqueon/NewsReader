package rinon.ninqueon.rssreader.xmlfeed;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;

import rinon.ninqueon.rssreader.xmlfeed.atom.AtomFeed;
import rinon.ninqueon.rssreader.xmlfeed.atom.AtomItem;
import rinon.ninqueon.rssreader.xmlfeed.atom.AtomParser;
import rinon.ninqueon.rssreader.xmlfeed.rss.RSSChannel;
import rinon.ninqueon.rssreader.xmlfeed.rss.RSSItem;
import rinon.ninqueon.rssreader.xmlfeed.rss.RSSParser;

/**
 * Created by Rinon Ninqueon on 03.04.2017.
 */

public final class XMLParser
{
    private final static String TAG_FEED                    = "feed";
    private final static String TAG_RSS                     = "rss";

    private final static String ATTRIBUTE_VERSION           = "version";

    private final static String SUPPORTABLE_RSS_VERSION     = "2.0";

    public static Feed parseXML(final InputStream inputStream, final String inputEncoding, final long channelId) throws XmlPullParserException, IOException, ParseException
    {
        final XmlPullParserFactory xmlFactory = XmlPullParserFactory.newInstance();
        xmlFactory.setNamespaceAware(true);

        final XmlPullParser xmlParser = xmlFactory.newPullParser();
        xmlParser.setInput(inputStream, inputEncoding);

        return parseXML(xmlParser, channelId);
    }

    private static Feed parseXML(final XmlPullParser xmlParser, final long channelId) throws XmlPullParserException, IOException, ParseException
    {
        int eventType = xmlParser.getEventType();

        if (eventType != XmlPullParser.START_DOCUMENT)
        {
            throw new ParseException("Invalid XML File", xmlParser.getLineNumber());
        }

        xmlParser.nextTag();

        final String xmlTag = xmlParser.getName();

        if (xmlTag.equals(TAG_FEED))
        {
            xmlParser.nextTag();
            final AtomFeed atomFeed = AtomParser.parseAtom(xmlParser);
            final ArrayList<FeedEntry> entries = new ArrayList<>();

            for (final AtomItem entry : atomFeed.getItems())
            {
                String description = entry.getContent();
                if (description == null)
                {
                    description = entry.getSummary();
                }

                final FeedEntry feedEntry = new FeedEntry(
                        entry.getTitle(),
                        description,
                        entry.getLink(),
                        entry.getUpdated(),
                        channelId);
                entries.add(feedEntry);
            }
            return new Feed(
                    atomFeed.getTitle(),
                    atomFeed.getSubtitle(),
                    atomFeed.getLink(),
                    atomFeed.getUpdated(),
                    entries);
        }
        else if (xmlTag.equals(TAG_RSS))
        {
            String rssVersion = xmlParser.getAttributeValue(null, ATTRIBUTE_VERSION);

            if (!rssVersion.equals(SUPPORTABLE_RSS_VERSION))
            {
                throw new ParseException("Unsupportable RSS Version", xmlParser.getLineNumber());
            }
            xmlParser.nextTag();
            final RSSChannel rssChannel = RSSParser.parseRSS(xmlParser);
            final ArrayList<FeedEntry> entries = new ArrayList<>();

            for (final RSSItem entry : rssChannel.getItems())
            {
                final FeedEntry feedEntry = new FeedEntry(
                        entry.getTitle(),
                        entry.getDescription(),
                        entry.getLink(),
                        entry.getPubDate(),
                        channelId);
                entries.add(feedEntry);

            }
            return new Feed(
                    rssChannel.getTitle(),
                    rssChannel.getDescription(),
                    rssChannel.getLink(),
                    rssChannel.getPubDate(),
                    entries);
        }
        else
        {
            throw new ParseException("Unsupportable XML Document", xmlParser.getLineNumber());
        }
    }
}
