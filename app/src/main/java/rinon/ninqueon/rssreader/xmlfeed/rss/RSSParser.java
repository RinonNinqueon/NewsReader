package rinon.ninqueon.rssreader.xmlfeed.rss;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Rinon Ninqueon on 20.02.2017.
 */

public final class RSSParser
{
    private final static String TAG_CHANNEL                 = "channel";
    private final static String TAG_ITEM                    = "item";

    private final static String TAG_TITLE                   = "title";
    private final static String TAG_DESCRIPTION             = "description";
    private final static String TAG_LINK                    = "link";
    private final static String TAG_PUBDATE                 = "pubDate";

    public static RSSChannel parseRSS(final XmlPullParser xmlParser) throws XmlPullParserException, IOException, ParseException
    {
        xmlParser.require(XmlPullParser.START_TAG, null, TAG_CHANNEL);
        int eventType = xmlParser.nextTag();

        final ArrayList<RSSItem> rssItems = new ArrayList<>();
        String title = null;
        String description = null;
        String link = null;
        long date = new Date().getTime();

        while (eventType != XmlPullParser.END_TAG)
        {
            String tagName = xmlParser.getName();
            switch (tagName)
            {
                case TAG_TITLE:
                    title = readTagText(xmlParser, TAG_TITLE);
                    break;
                case TAG_DESCRIPTION:
                    description = readTagText(xmlParser, TAG_DESCRIPTION);
                    break;
                case TAG_LINK:
                    link = readTagText(xmlParser, TAG_LINK);
                    break;
                case TAG_PUBDATE:
                    String dateString = readTagText(xmlParser, TAG_PUBDATE);
                    date = parseStringDate(dateString);
                    break;
                case TAG_ITEM:
                    final RSSItem rssItem = readItem(xmlParser);
                    rssItems.add(rssItem);
                    break;
                default:
                    readTagDummy(xmlParser);
                    break;
            }

            eventType = xmlParser.nextTag();
        }

        return new RSSChannel(title, description, link, date, rssItems);
    }

    private static Date formatDataRFC833(final String dateString) throws ParseException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);

        return dateFormat.parse(dateString);
    }

    private static String readTagText(final XmlPullParser xmlParser, final String tag) throws IOException, XmlPullParserException
    {
        xmlParser.require(XmlPullParser.START_TAG, null, tag);
        String text = readText(xmlParser);
        xmlParser.require(XmlPullParser.END_TAG, null, tag);
        return text;
    }

    private static void readTagDummy(final XmlPullParser xmlParser) throws IOException, XmlPullParserException
    {
        if (xmlParser.getEventType() != XmlPullParser.START_TAG)
        {
            throw new IllegalStateException();
        }

        int depth = 1;
        while (depth != 0)
        {
            switch (xmlParser.next())
            {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private static String readText(final XmlPullParser xmlParser) throws IOException, XmlPullParserException
    {
        String result = "";
        if (xmlParser.next() == XmlPullParser.TEXT)
        {
            result = xmlParser.getText();
            xmlParser.nextTag();
        }
        return result;
    }

    private static RSSItem readItem(final XmlPullParser xmlParser) throws IOException, XmlPullParserException, ParseException
    {
        xmlParser.require(XmlPullParser.START_TAG, null, TAG_ITEM);
        int eventType = xmlParser.nextTag();

        String title = null;
        String description = null;
        String link = null;
        long date = new Date().getTime();

        while (eventType != XmlPullParser.END_TAG)
        {
            String tagName = xmlParser.getName();
            switch (tagName)
            {
                case TAG_TITLE:
                    title = readTagText(xmlParser, TAG_TITLE);
                    break;
                case TAG_DESCRIPTION:
                    description = readTagText(xmlParser, TAG_DESCRIPTION);
                    break;
                case TAG_LINK:
                    link = readTagText(xmlParser, TAG_LINK);
                    break;
                case TAG_PUBDATE:
                    String dateString = readTagText(xmlParser, TAG_PUBDATE);
                    date = parseStringDate(dateString);
                    break;
                default:
                    readTagDummy(xmlParser);
                    break;
            }

            eventType = xmlParser.nextTag();
        }

        xmlParser.require(XmlPullParser.END_TAG, null, TAG_ITEM);

        return new RSSItem(title, description, link, date);
    }

    private static long parseStringDate(String dateString) throws ParseException
    {
        Date dateDate = formatDataRFC833(dateString);
        return dateDate.getTime();
    }
}
