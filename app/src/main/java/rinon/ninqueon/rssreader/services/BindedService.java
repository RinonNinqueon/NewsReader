package rinon.ninqueon.rssreader.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Logger;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.database.DataBaseHelper;
import rinon.ninqueon.rssreader.utils.DownloadHelper;
import rinon.ninqueon.rssreader.utils.ErrorCodes;
import rinon.ninqueon.rssreader.utils.ProgressCodes;
import rinon.ninqueon.rssreader.xmlfeed.Feed;
import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;

/**
 * Created by Rinon Ninqueon on 07.04.2017.
 */

public class BindedService extends Service
{
    private final static String LOGGER_TAG                  = BindedService.class.getName();
    private final static Logger logger                      = Logger.getLogger(LOGGER_TAG);

    public static final String ACTION_UPDATE_DATA           = "rinon.ninqueon.rssreader.services.action.UPDATE_DATA";
    public static final String ACTION_ITEMS_READ            = "rinon.ninqueon.rssreader.services.action.ITEMS_READ";
    private static final String ACTION_ITEMS_DELETE_ALL      = "rinon.ninqueon.rssreader.services.action.ITEMS_DELETE_ALL";
    public static final String ACTION_CHANNEL_ITEMS_READ    = "rinon.ninqueon.rssreader.services.action.CHANNEL_ITEMS_READ";

    public static final String ACTION_CHANNEL_ADD           = "rinon.ninqueon.rssreader.services.action.CHANNEL_ADD";
    public static final String ACTION_CHANNEL_MODIFY        = "rinon.ninqueon.rssreader.services.action.CHANNEL_MODIFY";
    public static final String ACTION_CHANNELS_READ         = "rinon.ninqueon.rssreader.services.action.CHANNELS_READ";
    public static final String ACTION_CHANNEL_DELETE        = "rinon.ninqueon.rssreader.services.action.CHANNEL_DELETE";
    public static final String ACTION_CHANNELS_UPDATE       = "rinon.ninqueon.rssreader.services.action.CHANNELS_UPDATE";

    public static final String ACTION_LOAD_URL              = "rinon.ninqueon.rssreader.services.action.LOAD_URL";

    public static final String ACTION_DOWNLOAD_STATUS       = "rinon.ninqueon.rssreader.services.action.DOWNLOAD";
    public static final String ACTION_ERROR                 = "rinon.ninqueon.rssreader.services.action.ERROR";
    public static final String ACTION_CRITICAL_ERROR        = "rinon.ninqueon.rssreader.services.action.CRITICAL_ERROR";

    public static final String ACTION_CHANNELS_IMPORT       = "rinon.ninqueon.rssreader.services.action.CHANNELS_IMPORT";
    public static final String ACTION_CHANNELS_EXPORT       = "rinon.ninqueon.rssreader.services.action.CHANNELS_EXPORT";

    private static final String ACTION_UPDATE_IN_BACKGROUND = "rinon.ninqueon.rssreader.services.action.UPDATE_IN_BACKGROUND";

    private final static String EXTRA_ITEM_CHANNEL_LINK     = "EXTRA_ITEM_CHANNEL_LINK";
    private final static String EXTRA_ITEM_CHANNEL_ID       = "EXTRA_ITEM_CHANNEL_ID";

    private final static String EXTRA_READ_OFFSET           = "EXTRA_READ_OFFSET";
    private final static String EXTRA_READ_LIMIT            = "EXTRA_READ_LIMIT";
    public final static String EXTRA_PROGRESS               = "EXTRA_PROGRESS";
    public final static String EXTRA_ERROR_ID               = "EXTRA_ERROR_ID";
    public final static String EXTRA_PROGRESS_PERCENT       = "EXTRA_PROGRESS_PERCENT";

    private final static int NO_ENTRIES                     = 0;
    private final static int MAX_PERCENT                    = 100;
    private final static int BIND_TIMEOUT_MS                = 3000;

    private boolean isBinded;
    private boolean isStarted;
    private final Object lock = new Object();
    private final IBinder localBinder;
    private volatile Looper serviceLooper;
    private volatile ServiceHandler serviceHandler;
    private final String serviceName;

    private FeedEntry feedEntriesResult[] = null;
    private Feed feedResult = null;

    private final class ServiceHandler extends Handler
    {
        ServiceHandler(final Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(final Message msg)
        {
            onHandleIntent((Intent)msg.obj);
            //stopSelf(msg.arg1);
        }
    }

    public BindedService()
    {
        serviceName = LOGGER_TAG;
        localBinder = new LocalBinder();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        logger.info("onCreate");

        HandlerThread thread = new HandlerThread("BindedService[" + serviceName + "]");
        thread.start();

        isBinded = false;

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public void onDestroy()
    {
        logger.info("onDestroy");
        serviceLooper.quit();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId)
    {
        sendIntentMessage(intent);
        isStarted = true;
        return START_NOT_STICKY;
    }

    private void sendIntentMessage(final Intent intent)
    {
        logger.info("sendIntentMessage");
        Message msg = serviceHandler.obtainMessage();
        msg.obj = intent;
        serviceHandler.sendMessage(msg);
    }

    private void onHandleIntent(final Intent intent)
    {
        logger.info("isBinded=" + isBinded);

        if (!isBinded && !isStarted && lock != null)
        {
            synchronized(lock)
            {
                try
                {
                    lock.wait(BIND_TIMEOUT_MS);
                }
                catch (final InterruptedException e)
                {
                    logger.severe(e.getMessage());
                }
            }
        }
        if (intent != null)
        {
            final String action = intent.getAction();
            logger.config(action);
            if (ACTION_UPDATE_DATA.equals(action))
            {
                NotificationsHelper.buildNotificationUpdating(this);
                final int insertedItemsCount = actionUpdate();
                if (insertedItemsCount < 0)
                {
                    NotificationsHelper.clearNotification(this);
                }
            }
            if (ACTION_UPDATE_IN_BACKGROUND.equals(action))
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
            if (ACTION_ITEMS_DELETE_ALL.equals(action))
            {
                actionDeleteItems();
            }
            if (ACTION_ITEMS_READ.equals(intent.getAction()))
            {
                final Bundle args = intent.getExtras();
                long start = DataBaseHelper.DEFAULT_OFFSET;
                long count = DataBaseHelper.DEFAULT_LIMIT;
                if (args != null)
                {
                    start = args.getLong(EXTRA_READ_OFFSET);
                    count = args.getLong(EXTRA_READ_LIMIT);
                }

                actionItemsRead(start, count);
            }
            if (ACTION_CHANNEL_ITEMS_READ.equals(intent.getAction()))
            {
                final Bundle args = intent.getExtras();
                long channelId = -1;
                long start = DataBaseHelper.DEFAULT_OFFSET;
                long count = DataBaseHelper.DEFAULT_LIMIT;
                if (args != null)
                {
                    channelId = args.getLong(EXTRA_ITEM_CHANNEL_ID);
                    start = args.getLong(EXTRA_READ_OFFSET);
                    count = args.getLong(EXTRA_READ_LIMIT);
                }

                if (channelId == -1)
                {
                    return;
                }

                actionItemsReadByChannelId(channelId, start, count);
            }
            if (ACTION_CHANNELS_READ.equals(intent.getAction()))
            {
                 actionChannelsRead();
            }
            if (ACTION_CHANNEL_ADD.equals(intent.getAction()))
            {
                final Bundle args = intent.getExtras();
                if (args == null)
                {
                    return;
                }

                final String url = args.getString(EXTRA_ITEM_CHANNEL_LINK);

                if (url == null)
                {
                    return;
                }

                actionChannelAdd(url);
            }
            if (ACTION_CHANNEL_DELETE.equals(intent.getAction()))
            {
                final Bundle args = intent.getExtras();
                if (args == null)
                {
                    return;
                }

                final long id = args.getLong(EXTRA_ITEM_CHANNEL_ID);
                actionChannelDelete(id);
            }
            if (ACTION_CHANNEL_MODIFY.equals(intent.getAction()))
            {
                final Bundle args = intent.getExtras();
                if (args == null)
                {
                    return;
                }

                final long id = args.getLong(EXTRA_ITEM_CHANNEL_ID);
                final String url = args.getString(EXTRA_ITEM_CHANNEL_LINK);

                if (url == null)
                {
                    return;
                }

                actionChannelModify(id, url);
            }
            if (ACTION_LOAD_URL.equals(intent.getAction()))
            {
                final Bundle args = intent.getExtras();
                if (args == null)
                {
                    return;
                }

                final String url = args.getString(EXTRA_ITEM_CHANNEL_LINK);

                if (url == null)
                {
                    return;
                }

                actionLoadURL(url);
            }
            if (ACTION_CHANNELS_EXPORT.equals(intent.getAction()))
            {
                actionChannelsExport();
            }
            if (ACTION_CHANNELS_IMPORT.equals(intent.getAction()))
            {
                actionChannelsImport();
            }
            if (ACTION_CHANNELS_UPDATE.equals(action))
            {
                actionChannelsUpdate();
            }
        }

        if (isStarted)
        {
            stopService(intent);
        }
    }

    private void actionDeleteItems()
    {
        ServiceDatabaseWork.deleteItemsAll(this);
    }

    private int actionUpdate()
    {
        if (!checkConnection())
        {
            return NO_ENTRIES;
        }

        BroadcastMessagesWorker.sendProgressMessage(this, ProgressCodes.START, -1);

        FeedEntry readChannels[];
        try
        {
            readChannels = ServiceDatabaseWork.readAllChannels(this);
        }
        catch (final SQLException e)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_DATABASE);
            logger.severe(e.getMessage());
            return NO_ENTRIES;
        }

        if (readChannels == null)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_DATABASE);
            return NO_ENTRIES;
        }

        final ArrayList<FeedEntry> result = new ArrayList<>();
        int currentIndex = 0;

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

                final int percent = (currentIndex * MAX_PERCENT) / readChannels.length;
                BroadcastMessagesWorker.sendProgressMessage(this, ProgressCodes.XML_GET_SUCCESS, percent);
            }
            catch (final ParseException e)
            {
                BroadcastMessagesWorker.sendErrorMessage(this, ErrorCodes.ERROR_DOCUMENT_TYPE);
                logger.severe(e.getMessage());

                updateChannelError(channel.getId(), ErrorCodes.ERROR_DOCUMENT_TYPE);
            }
            catch (final XmlPullParserException e)
            {
                BroadcastMessagesWorker.sendErrorMessage(this, ErrorCodes.ERROR_PARSE);
                logger.severe(e.getMessage());

                updateChannelError(channel.getId(), ErrorCodes.ERROR_PARSE);
            }
            catch (final IOException e)
            {
                BroadcastMessagesWorker.sendErrorMessage(this, ErrorCodes.ERROR_CONNECTION_ERROR);
                logger.severe(e.getMessage());

                updateChannelError(channel.getId(), ErrorCodes.ERROR_CONNECTION_ERROR);
            }
            finally
            {
                currentIndex++;
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
                        BroadcastMessagesWorker.sendErrorMessage(this, ErrorCodes.ERROR_DATABASE);
                        logger.severe(e.getMessage());
                    }
                }
            }
        }

        FeedEntry resultArray[] = new FeedEntry[result.size()];
        resultArray = result.toArray(resultArray);

        BroadcastMessagesWorker.sendProgressMessage(this, ProgressCodes.READY_TO_WRITE_DB, -1);

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
                BroadcastMessagesWorker.sendErrorMessage(this, ErrorCodes.ERROR_DATABASE);
                logger.severe(e.getMessage());
            }
        }

        BroadcastMessagesWorker.sendProgressMessage(this, ProgressCodes.WRITE_DB_SUCCESS, -2);

        try
        {
            BroadcastMessagesWorker.sendBroadcastMessage(this, null, ACTION_UPDATE_DATA);
        }
        catch (final SQLException e)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_DATABASE);
            logger.severe(e.getMessage());
            return NO_ENTRIES;
        }

        return insertedItemsCount;
    }

    private void actionItemsRead(final long start, final long count)
    {
        FeedEntry[] feedEntries;

        try
        {
            feedEntries = ServiceDatabaseWork.readItems(this, start, count);
        }
        catch (final SQLException e)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_DATABASE);
            logger.severe(e.getMessage());
            return;
        }

        if (feedEntries == null)
        {
            return;
        }

        feedEntriesResult = feedEntries;

        BroadcastMessagesWorker.sendBroadcastMessage(this, null, ACTION_ITEMS_READ);
    }

    private void actionItemsReadByChannelId(final long channelId, final long start, final long count)
    {
        FeedEntry[] feedEntries;

        try
        {
            feedEntries = ServiceDatabaseWork.readItemsByChannelId(this, channelId, start, count);
        }
        catch (final SQLException e)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_DATABASE);
            logger.severe(e.getMessage());
            return;
        }

        if (feedEntries == null)
        {
            return;
        }

        feedEntriesResult = feedEntries;

        BroadcastMessagesWorker.sendBroadcastMessage(this, null, ACTION_CHANNEL_ITEMS_READ);
    }

    private void actionChannelsRead()
    {
        FeedEntry[] feedEntries;

        try
        {
            feedEntries = ServiceDatabaseWork.readAllChannels(this);
        }
        catch (final SQLException e)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_DATABASE);
            logger.severe(e.getMessage());
            return;
        }

        if (feedEntries == null)
        {
            return;
        }

        feedEntriesResult = feedEntries;

        BroadcastMessagesWorker.sendBroadcastMessage(this, null, ACTION_CHANNELS_READ);
    }

    private void actionChannelsExport()
    {
        FeedEntry readChannels[];
        try
        {
            readChannels = ServiceDatabaseWork.readAllChannels(this);
        }
        catch (final SQLException e)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_DATABASE);
            logger.severe(e.getMessage());
            return;
        }

        if (readChannels == null)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_DATABASE);
            return;
        }

        if (!ServiceFilesWorker.isExternalStorageWritable())
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_EXTERNAL_STORAGE_ERROR);
            return;
        }

        try
        {
            ServiceFilesWorker.writeToFile(readChannels);
        }
        catch (final IOException e)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_IO_ERROR);
            logger.severe(e.getMessage());
            return;
        }

        BroadcastMessagesWorker.sendBroadcastMessage(this, null, ACTION_CHANNELS_EXPORT);
    }

    private void actionChannelsImport()
    {

        if (!ServiceFilesWorker.isExternalStorageReadable())
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_EXTERNAL_STORAGE_ERROR);
            return;
        }

        ArrayList<String> urls;
        try
        {
            urls = ServiceFilesWorker.readFromFile();
        }
        catch (final IOException e)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_IO_ERROR);
            logger.severe(e.getMessage());
            return;
        }

        if (urls == null)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_IO_ERROR);
            return;
        }

        for (final String url : urls)
        {
            try
            {
                ServiceDatabaseWork.writeChannelUrl(this, url);
            }
            catch (final SQLException e)
            {
                BroadcastMessagesWorker.sendErrorMessage(this, ErrorCodes.ERROR_DATABASE);
                logger.severe(e.getMessage());
            }
        }

        BroadcastMessagesWorker.sendBroadcastMessage(this, null, ACTION_CHANNELS_IMPORT);
    }

    private void actionChannelDelete(final long id)
    {
        try
        {
            ServiceDatabaseWork.deleteChannel(this, id);
        }
        catch (final SQLException e)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_DATABASE);
            logger.severe(e.getMessage());
            return;
        }

        BroadcastMessagesWorker.sendBroadcastMessage(this, null, ACTION_CHANNEL_DELETE);
    }

    private void actionChannelsUpdate()
    {
        if (!checkConnection())
        {
            return;
        }

        BroadcastMessagesWorker.sendProgressMessage(this, ProgressCodes.START, -1);

        FeedEntry readChannels[];
        try
        {
            readChannels = ServiceDatabaseWork.readAllChannels(this);
        }
        catch (final SQLException e)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_DATABASE);
            logger.severe(e.getMessage());
            return;
        }

        if (readChannels == null)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_DATABASE);
            return;
        }

        int currentIndex = 0;

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

            FeedEntry channelEntry = null;

            try
            {
                final Feed feed = ServiceParseXMLWork.parseXMLStream(url, channel.getId());

                channelEntry = new FeedEntry(
                        channel.getId(),
                        feed.getTitle(),
                        feed.getDescription(),
                        feed.getLink(),
                        feed.getPubDate().getTime(),
                        url, ErrorCodes.ERROR_NO_ERROR);

                updateChannelError(channel.getId(), ErrorCodes.ERROR_NO_ERROR);

                final int percent = (currentIndex * MAX_PERCENT) / readChannels.length;
                BroadcastMessagesWorker.sendProgressMessage(this, ProgressCodes.XML_GET_SUCCESS, percent);
            }
            catch (final ParseException e)
            {
                BroadcastMessagesWorker.sendErrorMessage(this, ErrorCodes.ERROR_DOCUMENT_TYPE);
                logger.severe(e.getMessage());

                updateChannelError(channel.getId(), ErrorCodes.ERROR_DOCUMENT_TYPE);
            }
            catch (final XmlPullParserException e)
            {
                BroadcastMessagesWorker.sendErrorMessage(this, ErrorCodes.ERROR_PARSE);
                logger.severe(e.getMessage());

                updateChannelError(channel.getId(), ErrorCodes.ERROR_PARSE);
            }
            catch (final IOException e)
            {
                BroadcastMessagesWorker.sendErrorMessage(this, ErrorCodes.ERROR_CONNECTION_ERROR);
                logger.severe(e.getMessage());

                updateChannelError(channel.getId(), ErrorCodes.ERROR_CONNECTION_ERROR);
            }
            finally
            {
                currentIndex++;

                if (channelEntry != null)
                {
                    try
                    {
                        ServiceDatabaseWork.updateChannel(this, channelEntry, false);
                    }
                    catch (final SQLException e)
                    {
                        BroadcastMessagesWorker.sendErrorMessage(this, ErrorCodes.ERROR_DATABASE);
                        logger.severe(e.getMessage());
                    }
                }
            }
        }
        BroadcastMessagesWorker.sendProgressMessage(this, ProgressCodes.WRITE_DB_SUCCESS, -2);
        BroadcastMessagesWorker.sendBroadcastMessage(this, null, ACTION_CHANNELS_UPDATE);
    }

    private void actionChannelAdd(final String url)
    {
        if (url == null)
        {
            return;
        }

        try
        {
            if (ServiceDatabaseWork.isChannelExists(this, url))
            {
                BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_CHANNEL_EXISTS);
                return;
            }
        }
        catch (final SQLException e)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_DATABASE);
            logger.severe(e.getMessage());
            return;
        }

        try
        {
            ServiceDatabaseWork.writeChannelUrl(this, url);
        }
        catch (final SQLException e)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_DATABASE);
            logger.severe(e.getMessage());
            return;
        }

        BroadcastMessagesWorker.sendBroadcastMessage(this, null, ACTION_CHANNEL_ADD);
    }

    private void actionChannelModify(final long channelId, final String url)
    {
        if (url == null)
        {
            return;
        }

        if (!checkConnection())
        {
            return;
        }

        try
        {
            if (ServiceDatabaseWork.isChannelExists(this, url))
            {
                BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_CHANNEL_EXISTS);
                return;
            }
        }
        catch (final SQLException e)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_DATABASE);
            logger.severe(e.getMessage());
            return;
        }

        try
        {
            ServiceDatabaseWork.updateChannelUrlById(this, channelId, url, true);
        }
        catch (final SQLException e)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_DATABASE);
            logger.severe(e.getMessage());
            return;
        }

        BroadcastMessagesWorker.sendBroadcastMessage(this, null, ACTION_CHANNEL_MODIFY);
    }

    private void actionLoadURL(final String url)
    {
        if (url == null)
        {
            return;
        }

        if (!checkConnection())
        {
            return;
        }

        Feed feed;

        try
        {
            feed = ServiceParseXMLWork.parseXMLStream(url, 0);
        }
        catch (final ParseException e)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_DOCUMENT_TYPE);
            logger.severe(e.getMessage());
            return;
        }
        catch (final XmlPullParserException e)
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_PARSE);
            logger.severe(e.getMessage());
            return;
        }
        catch (final IOException e)
        {
            BroadcastMessagesWorker.sendErrorMessage(this, ErrorCodes.ERROR_CONNECTION_ERROR);
            logger.severe(e.getMessage());
            return;
        }

        if (feed == null)
        {
            return;
        }

        feedResult = feed;

        BroadcastMessagesWorker.sendBroadcastMessage(this, null, ACTION_LOAD_URL);
    }

    private void updateChannelError(final long channelId, final int errorCode)
    {
        try
        {
            ServiceDatabaseWork.updateChannelError(this, channelId, errorCode);
        }
        catch (final SQLException e)
        {
            BroadcastMessagesWorker.sendErrorMessage(this, ErrorCodes.ERROR_DATABASE);
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
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_NO_CONNECTION);
            return false;
        }

        if (!DownloadHelper.canDownloadInThisNet(this))
        {
            BroadcastMessagesWorker.sendCriticalErrorMessage(this, ErrorCodes.ERROR_ONLY_WIFI);
            return false;
        }

        return true;
    }

    public static Intent getUpdateIntent(final Context context)
    {
        final Intent intent = new Intent(context, BindedService.class);
        intent.setAction(ACTION_UPDATE_DATA);
        return intent;
    }

    public static Intent getItemsReadIntent(final Context context, final long start, final long count)
    {
        final Intent intent = new Intent(context, BindedService.class);
        intent.setAction(ACTION_ITEMS_READ);

        final Bundle args = new Bundle();
        args.putLong(EXTRA_READ_OFFSET, start);
        args.putLong(EXTRA_READ_LIMIT, count);
        intent.putExtras(args);
        return intent;
    }

    public static Intent getChannelsUpdateIntent(final Context context)
    {
        final Intent intent = new Intent(context, BindedService.class);
        intent.setAction(ACTION_CHANNELS_UPDATE);
        return intent;
    }

    public static Intent getChannelsImportIntent(final Context context)
    {
        final Intent intent = new Intent(context, BindedService.class);
        intent.setAction(ACTION_CHANNELS_IMPORT);
        return intent;
    }

    public static Intent getChannelsExportIntent(final Context context)
    {
        final Intent intent = new Intent(context, BindedService.class);
        intent.setAction(ACTION_CHANNELS_EXPORT);
        return intent;
    }

    public static Intent getChannelItemsReadIntent(final Context context, final long channelId, final long start, final long count)
    {
        final Intent intent = new Intent(context, BindedService.class);
        intent.setAction(ACTION_CHANNEL_ITEMS_READ);

        final Bundle args = new Bundle();
        args.putLong(EXTRA_ITEM_CHANNEL_ID, channelId);
        args.putLong(EXTRA_READ_OFFSET, start);
        args.putLong(EXTRA_READ_LIMIT, count);
        intent.putExtras(args);
        return intent;
    }

    public static Intent getChannelAddIntent(final Context context, final String url)
    {
        final Intent intent = new Intent(context, BindedService.class);
        intent.setAction(ACTION_CHANNEL_ADD);

        final Bundle args = new Bundle();
        args.putString(EXTRA_ITEM_CHANNEL_LINK, url);
        intent.putExtras(args);
        return intent;
    }

    public static Intent getChannelsReadIntent(final Context context)
    {
        final Intent intent = new Intent(context, BindedService.class);
        intent.setAction(ACTION_CHANNELS_READ);

        final Bundle args = new Bundle();
        intent.putExtras(args);
        return intent;
    }

    public static Intent getChannelDeleteIntent(final Context context, final long id)
    {
        final Intent intent = new Intent(context, BindedService.class);
        intent.setAction(ACTION_CHANNEL_DELETE);

        final Bundle args = new Bundle();
        args.putLong(EXTRA_ITEM_CHANNEL_ID, id);
        intent.putExtras(args);
        return intent;
    }

    public static Intent getLoadURLIntent(final Context context, final String url)
    {
        final Intent intent = new Intent(context, BindedService.class);
        intent.setAction(ACTION_LOAD_URL);

        final Bundle args = new Bundle();
        args.putString(EXTRA_ITEM_CHANNEL_LINK, url);
        intent.putExtras(args);
        return intent;
    }

    public static void startAddChannel(final Context context, final String url)
    {
        final Intent intent = new Intent(context, BindedService.class);
        intent.setAction(BindedService.ACTION_CHANNEL_ADD);

        final Bundle args = new Bundle();
        args.putString(BindedService.EXTRA_ITEM_CHANNEL_LINK, url);
        intent.putExtras(args);
        context.startService(intent);
    }

    public static void startDeleteItems(final Context context)
    {
        final Intent intent = new Intent(context, BindedService.class);
        intent.setAction(ACTION_ITEMS_DELETE_ALL);
        context.startService(intent);
    }

    public static void startChannelModify(final Context context, final long id, final String url)
    {
        final Intent intent = new Intent(context, BindedService.class);
        intent.setAction(ACTION_CHANNEL_MODIFY);

        final Bundle args = new Bundle();
        args.putLong(EXTRA_ITEM_CHANNEL_ID, id);
        args.putString(EXTRA_ITEM_CHANNEL_LINK, url);
        intent.putExtras(args);
        context.startService(intent);
    }

    public final FeedEntry[] getFeedEntriesResult()
    {
        return feedEntriesResult;
    }

    public final Feed getFeedResult()
    {
        return feedResult;
    }

    public final boolean dataAvailable()
    {
        return (feedEntriesResult != null || feedResult != null);
    }

    @Override
    public boolean onUnbind(final Intent intent)
    {
        logger.info("onUnbind " + intent);
        isBinded = false;
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(final Intent intent)
    {
        logger.info("onBind " + intent);
        feedResult = null;
        feedEntriesResult = null;
        sendIntentMessage(intent);
        return localBinder;
    }

    public class LocalBinder extends Binder
    {
        public BindedService getService()
        {
            logger.info("getService");
            isBinded = true;
            isStarted = false;
            synchronized(lock)
            {
                logger.info("lock.notify");
                lock.notify();
            }
            return BindedService.this;
        }
    }
}
