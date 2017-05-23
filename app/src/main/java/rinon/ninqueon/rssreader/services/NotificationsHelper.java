package rinon.ninqueon.rssreader.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.NotificationCompat;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;
import rinon.ninqueon.rssreader.view.drawer.DrawerActivity;
import rinon.ninqueon.rssreader.view.feedEntryScreen.FeedEntryActivity;

/**
 * Created by Rinon Ninqueon on 06.03.2017.
 */

public final class NotificationsHelper
{
    private final static String TAG                         = "NotificationsHelper";
    private final static String APP_PREFERENCES             = TAG + ".State";
    private final static String EXTRA_NOTIFICATION           = "EXTRA_NOTIFICATION";
    private final static String EXTRA_NOTIFICATION_COUNT     = "EXTRA_NOTIFICATION_COUNT";
    private final static int NOTIFICATION_ID                 = 754;
    private final static int DEFAULT_NOTIFICATION_COUNT     = 0;

    public static void clearNotification(final Context context)
    {
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        saveState(context, 0);
    }

    private static boolean isNotificationsAllowed(final Context context)
    {
        return SharedPreferencesHelper.getSharedBooleanId(context, R.string.settings_notification_enable, R.bool.settings_notification_enable_default);
    }

    static void buildNotificationUpdating(final Context context)
    {
        if (!isNotificationsAllowed(context))
        {
            return;
        }

        final String title = context.getResources().getString(R.string.app_name);
        final String content = context.getResources().getString(R.string.notification_updating);
        final long when = System.currentTimeMillis();
        final Intent intent = new Intent();

        addNotification(context, title, content, when, intent);
    }


    static void buildNotificationFeedEntry(final Context context, final FeedEntry feedEntry)
    {
        if (feedEntry == null)
        {
            return;
        }

        if (!isNotificationsAllowed(context))
        {
            return;
        }

        final int notificationCount = loadState(context);
        if (notificationCount > 0)
        {
            buildNotificationManyEntries(context, 1);
            return;
        }

        final Intent intent = FeedEntryActivity.getStartIntent(context, feedEntry);
        intent.putExtra(EXTRA_NOTIFICATION, NOTIFICATION_ID);

        String title = feedEntry.getChannelTitle();
        if (title == null)
        {
            title = context.getResources().getString(R.string.app_name);
        }

        String content = feedEntry.getTitle();
        if (content == null)
        {
            content = context.getResources().getString(R.string.notification_content) + " 1";
        }

        final long when = feedEntry.getPubDate().getTime();

        saveState(context, 1);

        addNotification(context, title, content, when, intent);
    }

    static void buildNotificationManyEntries(final Context context, final int newEntriesCount)
    {
        if (!isNotificationsAllowed(context))
        {
            return;
        }

        final String title = context.getResources().getString(R.string.notification_title);

        int notificationCount = loadState(context);
        if (notificationCount > 0)
        {
            notificationCount += newEntriesCount;
        }
        else
        {
            notificationCount = newEntriesCount;
        }

        final String content = context.getResources().getString(R.string.notification_content) + " " + notificationCount;

        final long when = System.currentTimeMillis();
        final Intent intent = new Intent(context, DrawerActivity.class);
        intent.putExtra(EXTRA_NOTIFICATION, NOTIFICATION_ID);

        saveState(context, newEntriesCount);

        addNotification(context, title, content, when, intent);
    }

    private static void addNotification(final Context context,
                                        final String title,
                                        final String content,
                                        final long when,
                                        final Intent intent)
    {
        final PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentInfo(content);
        notificationBuilder.setWhen(when);
        notificationBuilder.setSmallIcon(R.drawable.ic_notofication_icon);

        notificationBuilder.setContentIntent(notifyPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private static void saveState(final Context context, final int notificationCount)
    {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(EXTRA_NOTIFICATION_COUNT, notificationCount);
        editor.apply();
    }

    static int loadState(final Context context)
    {
        int notificationCount = DEFAULT_NOTIFICATION_COUNT;
        final SharedPreferences sharedPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(EXTRA_NOTIFICATION_COUNT))
        {
            notificationCount = sharedPreferences.getInt(EXTRA_NOTIFICATION_COUNT, DEFAULT_NOTIFICATION_COUNT);
        }

        return notificationCount;
    }

    public static boolean isFromNotification(final Intent intent)
    {
        if (intent == null)
        {
            return false;
        }

        return (intent.getIntExtra(NotificationsHelper.EXTRA_NOTIFICATION, 0) == NotificationsHelper.NOTIFICATION_ID);
    }
}
