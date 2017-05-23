package rinon.ninqueon.rssreader.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.services.SharedPreferencesHelper;

/**
 * Created by Rinon Ninqueon on 25.02.2017.
 */

public final class DownloadHelper
{
    private final static String LOGGER_TAG                  = DownloadHelper.class.getName();
    private final static Logger logger                      = Logger.getLogger(LOGGER_TAG);

    private String charset;
    private HttpURLConnection connection;
    private InputStream stream;

    private final static int READ_TIMEOUT           = 3000;
    private final static int CONNECT_TIMEOUT        = 3000;
    private final static String REQUEST_METHOD      = "GET";
    private final static String DEFAULT_CHARSET     = "UTF-8";
    private final static String HEADER_LOCATION     = "Location";
    private final static String validMIMETypes[]    = {"text/html",
                                                       "text/xml",
                                                       "text/rss+xml",
                                                       "text/atom+xml",
                                                       "application/xml",
                                                       "application/rss+xml",
                                                       "application/atom+xml"};

    public DownloadHelper()
    {
        connection = null;
        stream = null;
        charset = DEFAULT_CHARSET;
    }

    public InputStream openInputStream(final String urlString) throws IOException
    {
        if (urlString == null)
        {
            return null;
        }

        final URL url = new URL(urlString);

        logger.info("Loading: " + urlString);

        connection = (HttpURLConnection) url.openConnection();

        connection.setInstanceFollowRedirects(true);

        connection.setReadTimeout(READ_TIMEOUT);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setRequestMethod(REQUEST_METHOD);
        connection.setDoInput(true);
        connection.connect();

        int responseCode = connection.getResponseCode();
        logger.info("Response code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                || responseCode == HttpURLConnection.HTTP_SEE_OTHER)
        {
            closeInputStream();
            String newUrlString = connection.getHeaderField(HEADER_LOCATION);
            final URL newUrl = new URL(newUrlString);

            logger.info("Redirecting: " + newUrlString);

            connection = (HttpURLConnection) newUrl.openConnection();

            connection.setInstanceFollowRedirects(true);

            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setRequestMethod(REQUEST_METHOD);
            connection.setDoInput(true);
            connection.connect();

            responseCode = connection.getResponseCode();
        }
        if (responseCode != HttpsURLConnection.HTTP_OK)
        {
            throw new IOException("HTTP error code: " + responseCode);
        }

        String mimeType = connection.getContentType();
        if (!isValidMIMEType(mimeType))
        {
            throw new IOException("Wrong MIME type: " + mimeType);
        }

        logger.info("MIME type: " + mimeType);

        charset = connection.getContentEncoding();
        if (charset == null)
        {
            this.setCharset(mimeType);
        }
        if (charset == null)
        {
            charset = DEFAULT_CHARSET;
        }

        stream = connection.getInputStream();

        return stream;
    }

    public void closeInputStream() throws IOException
    {
        try
        {
            if (stream != null)
            {
                logger.info("Closing stream");
                stream.close();
            }
        }
        finally
        {
            if (connection != null)
            {
                logger.info("Closing connection");
                connection.disconnect();
            }
        }
    }

    public String getCharset()
    {
        return charset;
    }


    private boolean isValidMIMEType(final String contentType)
    {
        if (contentType == null)
        {
            return false;
        }

        final String strings[] = contentType.split(";");

        if (strings.length == 0)
        {
            return false;
        }

        String mimeType = strings[0];

        for (final String validType : validMIMETypes)
        {
            if (validType.equals(mimeType))
            {
                return true;
            }
        }

        return false;
    }

    private void setCharset(final String contentType)
    {
        if (contentType == null)
        {
            return;
        }

        final String strings[] = contentType.split(";");
        if (strings.length < 2)
        {
            return;
        }

        final String encodingStrings[] = strings[1].split("=");
        if (encodingStrings.length < 2)
        {
            return;
        }

        this.charset = encodingStrings[1];
    }

    public static boolean isOnline(final Context context)
    {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null)
        {
            return false;
        }

        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return !(networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE));
    }

    public static boolean canDownloadInThisNet(final Context context)
    {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null)
        {
            return false;
        }

        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        final boolean settings_update_on_wifi = SharedPreferencesHelper.getSharedBooleanId(context, R.string.settings_update_on_wifi, R.bool.settings_update_on_wifi_default);

        if (networkInfo!= null && networkInfo.isConnected())
        {
            if (networkInfo.getType() != ConnectivityManager.TYPE_WIFI)
            {
                return (!settings_update_on_wifi);
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }
}
