package rinon.ninqueon.rssreader.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Logger;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.utils.DownloadHelper;
import rinon.ninqueon.rssreader.utils.ErrorCodes;
import rinon.ninqueon.rssreader.xmlfeed.Feed;
import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public final class BackgroundUpdateIntentService extends IntentService
{
    private final static String LOGGER_TAG                  = BackgroundUpdateIntentService.class.getName();
    private final static Logger logger                      = Logger.getLogger(LOGGER_TAG);

    private static final String ACTION_UPDATE_IN_BACKGROUND = "rinon.ninqueon.rssreader.services.action.UPDATE_IN_BACKGROUND";

    private final static int NO_ENTRIES                     = 0;

    public BackgroundUpdateIntentService()
    {
        super("BackgroundUpdateIntentService");
    }

    public static Intent getUpdateInBackgroundIntent(final Context context)
    {
        final Intent intent = new Intent(context, BackgroundUpdateIntentService.class);
        intent.setAction(BackgroundUpdateIntentService.ACTION_UPDATE_IN_BACKGROUND);
        return intent;
    }

    @Override
    protected void onHandleIntent(final Intent intent)
    {
        logger.info("onHandleIntent " + intent);
        if (intent != null)
        {
            final String action = intent.getAction();
            logger.config(action);
             if (BackgroundUpdateIntentService.ACTION_UPDATE_IN_BACKGROUND.equals(action))
            {
                final boolean canUpdateInBackground = SharedPreferencesHelper.getSharedBooleanId(this, R.string.settings_update_enable, R.bool.settings_update_enable_default);
                if (canUpdateInBackground)
                {
                    NotificationsHelper.buildNotificationUpdating(this);
                    int insertedItemsCount = actionUpdate();
                    if (insertedItemsCount < 0)
                    {
                        NotificationsHelper.clearNotification(this);
                    }
                }
                else
                {
                    AlarmHelper.disableAlarm(this);
                }
            }
        }
        else
        {
            logger.config("No Intent!");
        }
    }

    private int actionUpdate()
    {
        if (!checkConnection())
        {
            return NO_ENTRIES;
        }

        FeedEntry readChannels[];
        try
        {
            readChannels = ServiceDatabaseWork.readAllChannels(this);
        }
        catch (SQLException e)
        {
            logger.severe(e.getMessage());
            return NO_ENTRIES;
        }

        if (readChannels == null)
        {
            return NO_ENTRIES;
        }

        final ArrayList<FeedEntry> result = new ArrayList<>();

        for (final FeedEntry channel : readChannels)
        {
            if (channel == null)
            {
                continue;
            }

            final String url = channel.getChannelLink();
            if (url == null)
            {
                continue;
            }

            ArrayList<FeedEntry> entries = null;
            FeedEntry channelEntry = null;

            try
            {
                final Feed feed = ServiceParseXMLWork.parseXMLStream(url, channel.getId());

                entries = feed.getItems();
                channelEntry = new FeedEntry(
                        channel.getId(),
                        feed.getTitle(),
                        feed.getDescription(),
                        feed.getLink(),
                        feed.getPubDate().getTime(),
                        url, ErrorCodes.ERROR_NO_ERROR);

                updateChannelError(channel.getId(), ErrorCodes.ERROR_NO_ERROR);
            }
            catch (final ParseException e)
            {
                logger.severe(e.getMessage());
                updateChannelError(channel.getId(), ErrorCodes.ERROR_DOCUMENT_TYPE);
            }
            catch (final XmlPullParserException | IOException e)
            {
                logger.severe(e.getMessage());
                updateChannelError(channel.getId(), ErrorCodes.ERROR_PARSE);
            }
            finally
            {
                if (entries != null)
                {
                    result.addAll(entries);
                }

                if (channelEntry != null)
                {
                    try
                    {
                        ServiceDatabaseWork.updateChannel(this, channelEntry, false);
                    }
                    catch (final SQLException e)
                    {
                        logger.severe(e.getMessage());
                    }
                }
            }
        }

        FeedEntry resultArray[] = new FeedEntry[result.size()];
        resultArray = result.toArray(resultArray);

        final int insertedItemsCount = ServiceDatabaseWork.writeItems(this, resultArray);

        if (insertedItemsCount == 1)
        {
            showNotification(insertedItemsCount, resultArray[0]);
        }
        else
        {
            showNotification(insertedItemsCount, null);
        }

        final int settings_feed_save_periodString = SharedPreferencesHelper.getSharedIntegerId(this, R.string.settings_feed_save_period, R.integer.settings_feed_save_period_default);
        if (settings_feed_save_periodString > 0)
        {
            try
            {
                ServiceDatabaseWork.deleteItemsOld(this, settings_feed_save_periodString);
            }
            catch (final SQLException e)
            {
                logger.severe(e.getMessage());
            }
        }

        return insertedItemsCount;
    }

    private void updateChannelError(final long channelId, final int errorCode)
    {
        try
        {
            ServiceDatabaseWork.updateChannelError(this, channelId, errorCode);
        }
        catch (final SQLException e)
        {
            logger.severe(e.getMessage());
        }
    }

    private void showNotification(final int insertedItemsCount, final FeedEntry feedEntry)
    {
        final int previousCount = NotificationsHelper.loadState(this) + insertedItemsCount;

        if (previousCount > 1)
        {
            NotificationsHelper.buildNotificationManyEntries(this, previousCount);
        }
        else if (previousCount == 1)
        {
            if (feedEntry != null)
            {
                NotificationsHelper.buildNotificationFeedEntry(this, feedEntry);
            }
            else
            {
                NotificationsHelper.buildNotificationManyEntries(this, previousCount);
            }
        }
        else
        {
            NotificationsHelper.clearNotification(this);
        }
    }

    private boolean checkConnection()
    {
        if (!DownloadHelper.isOnline(this))
        {
            return false;
        }

        return DownloadHelper.canDownloadInThisNet(this);

    }
}
