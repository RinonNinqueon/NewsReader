package rinon.ninqueon.rssreader.utils;

import android.os.Bundle;

import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;

/**
 * Created by Rinon Ninqueon on 06.03.2017.
 */

public final class BundleConverter
{
    private final static String EXTRA_ITEM                  = "EXTRA_ITEM";

    public static FeedEntry bundleToFeedEntries(final Bundle args)
    {
        if (args == null)
        {
            return null;
        }

        return args.getParcelable(EXTRA_ITEM);
    }

    public static Bundle feedEntryToBundle(final FeedEntry feedEntry)
    {
        if (feedEntry == null)
        {
            return null;
        }

        final Bundle args = new Bundle();

        args.putParcelable(EXTRA_ITEM, feedEntry);

        return args;
    }
}
