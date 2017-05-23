package rinon.ninqueon.rssreader.services;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import rinon.ninqueon.rssreader.utils.DownloadHelper;
import rinon.ninqueon.rssreader.xmlfeed.Feed;
import rinon.ninqueon.rssreader.xmlfeed.XMLParser;

/**
 * Created by Rinon Ninqueon on 06.03.2017.
 */

final class ServiceParseXMLWork
{
    private ServiceParseXMLWork()
    {
        throw new UnsupportedOperationException("ServiceParseXMLWork.Constructor");
    }

    static Feed parseXMLStream(final String url, final long channelId) throws ParseException, XmlPullParserException, IOException
    {
        if (url == null)
        {
            return null;
        }

        final DownloadHelper downloadHelper = new DownloadHelper();

        InputStream inputStream;
        Feed feed = null;

        try
        {
            inputStream = downloadHelper.openInputStream(url);
            feed = XMLParser.parseXML(inputStream, downloadHelper.getCharset(), channelId);
        }
        finally
        {
            downloadHelper.closeInputStream();
        }

        return feed;
    }
}
