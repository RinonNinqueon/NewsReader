package rinon.ninqueon.rssreader.services;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Rinon Ninqueon on 11.04.2017.
 */

final class BroadcastMessagesWorker
{
    static void sendBroadcastMessage(final Context context, final Bundle args, final String action)
    {
        if (action == null)
        {
            return;
        }

        final Intent transmitIntent = new Intent();
        transmitIntent.setAction(action);
        if (args != null)
        {
            transmitIntent.putExtras(args);
        }

        LocalBroadcastManager.getInstance(context).sendBroadcast(transmitIntent);
    }

    static void sendProgressMessage(final Context context, final int progress, final int percent)
    {
        final Bundle args = new Bundle();
        args.putInt(BindedService.EXTRA_PROGRESS, progress);
        args.putInt(BindedService.EXTRA_PROGRESS_PERCENT, percent);

        sendBroadcastMessage(context, args, BindedService.ACTION_DOWNLOAD_STATUS);
    }

    static void sendErrorMessage(final Context context, final int errorId)
    {
        final Bundle args = new Bundle();
        args.putInt(BindedService.EXTRA_ERROR_ID, errorId);
        sendBroadcastMessage(context, args, BindedService.ACTION_ERROR);
    }

    static void sendCriticalErrorMessage(final Context context, final int errorId)
    {
        final Bundle args = new Bundle();
        args.putInt(BindedService.EXTRA_ERROR_ID, errorId);

        sendBroadcastMessage(context, args, BindedService.ACTION_CRITICAL_ERROR);
    }
}
