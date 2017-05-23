package rinon.ninqueon.rssreader.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import rinon.ninqueon.rssreader.R;

/**
 * Created by Rinon Ninqueon on 06.03.2017.
 */

public final class AlarmHelper
{
    public static void setAlarm(final Context context)
    {
        final int settings_update_periodString = SharedPreferencesHelper.getSharedIntegerId(context, R.string.settings_update_period, R.integer.settings_update_period_default);
        final long delayMilliseconds = ServiceDatabaseWork.hoursToMilliseconds(settings_update_periodString);

        final PendingIntent pendingIntent = getPendingIntent(context);

        final AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayMilliseconds, delayMilliseconds, pendingIntent);
    }

    private static PendingIntent getPendingIntent(final Context context)
    {
        final Intent serviceIntent = BackgroundUpdateIntentService.getUpdateInBackgroundIntent(context);
        return PendingIntent.getService(
                context,
                0,
                serviceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    static void disableAlarm(final Context context)
    {
        final PendingIntent pendingIntent = getPendingIntent(context);

        final AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
