package rinon.ninqueon.rssreader.xmlfeed.atom;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Created by Rinon Ninqueon on 03.04.2017.
 */

public final class AtomParser
{
    private final static String TAG_ENTRY                   = "entry";

    private final static String TAG_TITLE                   = "title";
    private final static String TAG_SUBTITLE                = "subtitle";
    private final static String TAG_SUMMARY                 = "summary";
    private final static String TAG_CONTENT                 = "content";
    private final static String TAG_LINK                    = "link";
    private final static String TAG_UPDATED                 = "updated";

    private final static String ATTRIBUTE_HREF              = "href";
    private final static String ATTRIBUTE_REL               = "rel";
    private final static String ATTRIBUTE_ALTERNATIVE       = "alternate";

    private final static String DATE_PATTERNS[]             = {"yyyy-MM-dd'T'HH:mm:ss'Z'",
                                                               "yyyy-MM-dd'T'HH:mm:ssZZZZZ",
                                                               "yyyy-MM-dd'T'HH:mm:ssz",
                                                               "yyyy-MM-dd'T'HH:mm:ssZ"};

    public static AtomFeed parseAtom(final XmlPullParser xmlParser) throws XmlPullParserException, IOException, ParseException
    {
        int eventType = xmlParser.getEventType();

        final ArrayList<AtomItem> rssItems = new ArrayList<>();
        String title = null;
        String subtitle = null;
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
                case TAG_SUBTITLE:
                    subtitle = readTagText(xmlParser, TAG_SUBTITLE);
                    break;
                case TAG_LINK:
                    link = readLink(xmlParser, TAG_LINK, link);
                    break;
                case TAG_UPDATED:
                    String dateString = readTagText(xmlParser, TAG_UPDATED);
                    date = parseStringDate(dateString);
                    break;
                case TAG_ENTRY:
                    final AtomItem rssItem = readEntry(xmlParser);
                    rssItems.add(rssItem);
                    break;
                default:
                    readTagDummy(xmlParser);
                    break;
            }

            eventType = xmlParser.nextTag();
        }

        return new AtomFeed(title, subtitle, link, date, rssItems);
    }

    private static Date formatDataISO8601(final String dateString) throws ParseException
    {
        for (final String pattern : DATE_PATTERNS)
        {
            Date date = null;
            try
            {
                date = formatDataISO8601(dateString, pattern);
            }
            catch (ParseException e)
            {
                Logger.getLogger(AtomParser.class.getName()).severe("Can parse '" + dateString + "' with '" + pattern + "'");
            }

            if (date != null)
            {
                return date;
            }
        }

        throw new ParseException("Wrong Date", 0);

//        return new Date();
    }

    private static Date formatDataISO8601(final String dateString, final String pattern) throws ParseException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);

        return dateFormat.parse(dateString);
    }

    private static String readTagText(final XmlPullParser xmlParser, final String tag) throws IOException, XmlPullParserException
    {
        xmlParser.require(XmlPullParser.START_TAG, null, tag);
        String text = readText(xmlParser);
        xmlParser.require(XmlPullParser.END_TAG, null, tag);
        return text;
    }

    private static String readLink(final XmlPullParser xmlParser, final String tag, final String existsString) throws IOException, XmlPullParserException
    {
        xmlParser.require(XmlPullParser.START_TAG, null, tag);
        String rel = xmlParser.getAttributeValue(null, ATTRIBUTE_REL);
        String href = xmlParser.getAttributeValue(null, ATTRIBUTE_HREF);

        xmlParser.nextTag();
        xmlParser.require(XmlPullParser.END_TAG, null, tag);

        if (href == null)
        {
            return existsString;
        }
        if (rel == null)
        {
            return href;
        }
        else
        {
            if (existsString == null && rel.equals(ATTRIBUTE_ALTERNATIVE))
            {
                return href;
            }
        }

        return existsString;
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

    private static AtomItem readEntry(final XmlPullParser xmlParser) throws IOException, XmlPullParserException, ParseException
    {
        xmlParser.require(XmlPullParser.START_TAG, null, TAG_ENTRY);
        int eventType = xmlParser.nextTag();

        String title = null;
        String summary = null;
        String content = null;
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
                case TAG_SUMMARY:
                    summary = readTagText(xmlParser, TAG_SUMMARY);
                    break;
                case TAG_CONTENT:
                    content = readTagText(xmlParser, TAG_CONTENT);
                    break;
                case TAG_LINK:
                    link = readLink(xmlParser, TAG_LINK, link);
                    break;
                case TAG_UPDATED:
                    String dateString = readTagText(xmlParser, TAG_UPDATED);
                    date = parseStringDate(dateString);
                    break;
                default:
                    readTagDummy(xmlParser);
                    break;
            }

            eventType = xmlParser.nextTag();
        }

        xmlParser.require(XmlPullParser.END_TAG, null, TAG_ENTRY);

        return new AtomItem(title, summary, content, link, date);
    }

    private static long parseStringDate(String dateString) throws ParseException
    {
        Date dateDate = formatDataISO8601(dateString);
        return dateDate.getTime();
    }
}
