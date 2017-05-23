package rinon.ninqueon.rssreader.utils;

import android.webkit.URLUtil;

/**
 * Created by Rinon Ninqueon on 30.03.2017.
 */

public class UrlFormatter
{
    private final static String STARTS_CHANGE_TO_HTTP[] = {
            "htp",
            "htt",
            "hpt",
            "hpp"
    };

    private final static String STARTS_WITH_HTTP        = "http";

    public static boolean isUrlValid(final String url)
    {
        return (URLUtil.isValidUrl(url) && (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)));
    }

    public static String formatUrl(final String url)
    {
        String result = url;
        for (final String prefix : STARTS_CHANGE_TO_HTTP)
        {
            if (result.length() <= prefix.length())
            {
                continue;
            }

            if (result.toLowerCase().startsWith(prefix))
            {
                final char nextCharacter = result.charAt(prefix.length());
                final char nextNextCharacter = result.charAt(prefix.length() + 1);

                if (nextCharacter == ':' || (nextCharacter == '/' && nextNextCharacter == '/'))
                {
                    result = STARTS_WITH_HTTP + result.substring(prefix.length());
                }
            }
        }

        int protocolFullPosition = result.indexOf("://");
        int protocolSlashPosition = result.indexOf("//");
        int protocolColonPosition = result.indexOf(":/");

        if (protocolFullPosition >= 0)
        {
            return result;
        }

        if (protocolSlashPosition >= 0)
        {
            StringBuilder stringBuilder = new StringBuilder(result);
            stringBuilder = stringBuilder.insert(protocolSlashPosition, ':');
            return stringBuilder.toString();
        }

        if (protocolColonPosition >= 0)
        {
            StringBuilder stringBuilder = new StringBuilder(result);
            stringBuilder = stringBuilder.insert(protocolColonPosition + 1, '/');
            return stringBuilder.toString();
        }

        //No protocol "://"
        result = STARTS_WITH_HTTP + "://" + result;

        return result;
    }
}
