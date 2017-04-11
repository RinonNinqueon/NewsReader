package rinon.ninqueon.rssreader.view.newsScreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import java.util.logging.Logger;

import rinon.ninqueon.rssreader.services.BindedService;

/**
 * Created by Rinon Ninqueon on 03.03.2017.
 */

final class NewsBroadcastReceiver extends BroadcastReceiver
{
    private final NewsController newsController;
    private final Context context;
    private final static String LOGGER_TAG                  = NewsBroadcastReceiver.class.getName();
    private final static Logger logger                      = Logger.getLogger(LOGGER_TAG);

    NewsBroadcastReceiver(final Context context,
                          final NewsController newsController)
    {
        this.newsController = newsController;
        this.context = context;
    }

    final void registerBroadcastReceiver()
    {
        final IntentFilter filter = new IntentFilter(BindedService.ACTION_UPDATE_DATA);
        filter.addAction(BindedService.ACTION_ITEMS_READ);
        filter.addAction(BindedService.ACTION_DOWNLOAD_STATUS);
        filter.addAction(BindedService.ACTION_ERROR);
        filter.addAction(BindedService.ACTION_CRITICAL_ERROR);
        LocalBroadcastManager.getInstance(context).registerReceiver(this, filter);
    }

    final void unregisterBroadcastReceiver()
    {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        logger.info("onReceive " + intent);
        if (intent == null)
        {
            return;
        }

        if (intent.getAction().equals(BindedService.ACTION_UPDATE_DATA))
        {
            newsController.startReadItems();
        }
        if (intent.getAction().equals(BindedService.ACTION_ITEMS_READ))
        {
            newsController.addItemsFromService();
        }
        if (intent.getAction().equals(BindedService.ACTION_DOWNLOAD_STATUS))
        {
            final Bundle args = intent.getExtras();
            final int progress = args.getInt(BindedService.EXTRA_PROGRESS);
            final int progressPercent = args.getInt(BindedService.EXTRA_PROGRESS_PERCENT);

            newsController.updateProgressBar(progress, progressPercent);
        }
        if (intent.getAction().equals(BindedService.ACTION_ERROR))
        {
            final Bundle args = intent.getExtras();
            final int errorCode = args.getInt(BindedService.EXTRA_ERROR_ID);
            newsController.displayErrorToast(errorCode);
        }
        if (intent.getAction().equals(BindedService.ACTION_CRITICAL_ERROR))
        {
            final Bundle args = intent.getExtras();
            final int errorCode = args.getInt(BindedService.EXTRA_ERROR_ID);
            newsController.showErrorDialog(errorCode);
        }
    }
}
