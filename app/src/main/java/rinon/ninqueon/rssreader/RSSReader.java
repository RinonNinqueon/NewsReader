package rinon.ninqueon.rssreader;

import android.app.Application;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import rinon.ninqueon.rssreader.utils.LogcatHandler;

/**
 * Created by Rinon Ninqueon on 23.03.2017.
 */

public final class RSSReader extends Application
{
    public RSSReader()
    {
        final Logger logger = Logger.getLogger("rinon.ninqueon.rssreader");

        if (BuildConfig.DEBUG)
        {
            logger.setLevel(Level.CONFIG);
            logger.setUseParentHandlers(false);
            final Handler handler = new LogcatHandler();
            logger.addHandler(handler);
        }
        else
        {
            logger.setLevel(Level.OFF);
        }
    }
}
