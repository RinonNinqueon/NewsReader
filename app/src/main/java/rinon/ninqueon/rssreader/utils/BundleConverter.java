package rinon.ninqueon.rssreader.utils;

import android.os.Bundle;

import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;

/**
 * Created by Rinon Ninqueon on 06.03.2017.
 */

public final class BundleConverter
{
    private final static String EXTRA_ITEM                  = "EXTRA_ITEM";
    private final static String EXTRA_ITEMS_COUNT           = "EXTRA_ITEMS_COUNT";

    public static FeedEntry[] bundleToFeedEntries(final Bundle args)
    {
        if (args == null)
        {
            return null;
        }

        final int size = args.getInt(EXTRA_ITEMS_COUNT);
        final FeedEntry feedEntries[] = new FeedEntry[size];

        for (int i = 0; i < size; i++)
        {
            final String preKey = EXTRA_ITEM + i;
            final FeedEntry entry = args.getParcelable(preKey);
            feedEntries[i] = entry;
        }

        return feedEntries;
    }

    public static Bundle feedEntryToBundle(final FeedEntry... feedEntries)
    {
        if (feedEntries == null)
        {
            return null;
        }

        final Bundle args = new Bundle();

        args.putInt(EXTRA_ITEMS_COUNT, feedEntries.length);

        for (int i = 0; i < feedEntries.length; i++)
        {
            final FeedEntry entry = feedEntries[i];
            final String preKey = EXTRA_ITEM + i;
            args.putParcelable(preKey, entry);
        }

        return args;
    }
}
